package extract;

import java.awt.Rectangle;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.scene.image.Image;
import ml.data.DataframeBuilder;
import ml.data.DataframeML;
import ml.data.DataframeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.sl.usermodel.PictureData;
import org.apache.poi.xddf.usermodel.text.XDDFTextParagraph;
import org.apache.poi.xslf.usermodel.*;
import org.slf4j.Logger;
import utils.*;
import utils.ex.HasLogging;
import utils.ex.RunnableEx;
import utils.ex.SupplierEx;

public final class PPTService {

    private static final Logger LOG = HasLogging.log();

    private PPTService() {
    }

    public static void getPowerPoint(Map<String, Object> replacementMap, String file, File outStream) {
        getPowerPoint(replacementMap, ResourceFXUtils.toFile("models/" + file), outStream);
    }

    public static void getPowerPointImages(File arquivo) {
        RunnableEx.run(() -> {
            try (XMLSlideShow a = new XMLSlideShow(new FileInputStream(arquivo))) {
                for (XSLFPictureData data : a.getPictureData()) {
                    recordPicture(data);
                }
            }
        });
    }

    public static void main(String[] args) {
        Map<String, Object> mapaSubstituicao = new HashMap<>();
        mapaSubstituicao.put("DIA: DD/MM/AAAA", String.format("DIA: %s", DateFormatUtils.currentDate()));
        mapaSubstituicao.put("Hora: HH/MM h", String.format("Hora: %s", DateFormatUtils.currentHour()));
        mapaSubstituicao.put("Julho 2020", DateFormatUtils.currentTime("MMMM yyyy"));
        File outFile = ResourceFXUtils.getOutFile("pptx/result.pptx");
        getPowerPoint(mapaSubstituicao, "Radar_Eventos_v01122020REL1.pptx", outFile);
    }

    private static void addImage(XSLFSlide slide, XMLSlideShow ppt, List<?> list) {
        if (list.isEmpty()) {
            return;
        }
        Object ob = list.get(0);
        if (ob instanceof File) {
            addTable(slide, ob);
            return;
        }
        Image image = (Image) list.remove(0);
        byte[] pictureData = SupplierEx.get(() -> ImageFXUtils.toByteArray(image));
        XSLFPictureData pd = ppt.addPicture(pictureData, PictureData.PictureType.PNG);
        XSLFPictureShape picture = slide.createPicture(pd);
        final int width = 600;
        int height = (int) (image.getHeight() * width / image.getWidth());
        final int offsetX = 60;
        final int offsetY = 120;
        final int maxHeight = 300;
        picture.setAnchor(new Rectangle(offsetX, offsetY, width, Math.min(maxHeight, height)));
    }

    private static void addTable(XSLFSlide slide, Object ob) {
        DataframeML dataframe = DataframeBuilder.build((File) ob);
        String dataframeStr = DataframeUtils.toString(dataframe);
        LOG.info(dataframeStr);
        XSLFTable table = getShapeStream(slide, XSLFTable.class).findFirst().orElseGet(() -> {
            XSLFTable createTable = slide.createTable(1, 2);
            List<String> cols = dataframe.cols();
            for (int i = 0; i < 2 && i < cols.size(); i++) {
                XSLFTableCell cell = createTable.getCell(0, i);
                cell.setText(cols.get(i));
            }
            return createTable;
        });
        int numberOfColumns = table.getNumberOfColumns();
        for (int i = 0; i < numberOfColumns; i++) {
            XSLFTableCell cell = table.getCell(0, i);
            String text = cell.getText();
            List<XSLFTableRow> rows = table.getRows();
            int size = rows.size();
            String columnName =
                    dataframe.cols().stream().filter(text::equalsIgnoreCase).findFirst().orElse(text.toLowerCase());
            for (int j = 0; j < dataframe.getSize(); j++) {
                Object at = dataframe.getAt(columnName, j);
                XSLFTableRow row = j + 1 >= size ? table.addRow() : table.getRows().get(j + 1);
                XSLFTableCell xslfTableCell = i < row.getCells().size() ? row.getCells().get(i) : row.addCell();
                xslfTableCell.setText(StringSigaUtils.toStringSpecial(at));
            }
        }
    }

    private static void getPowerPoint(Map<String, Object> replacementMap, File arquivo, File outStream) {
        RunnableEx.run(() -> {
            try (InputStream resourceAsStream = new FileInputStream(arquivo);
                    XMLSlideShow document1 = new XMLSlideShow(resourceAsStream)) {
                for (XSLFSlide slide : document1.getSlides()) {
                    replaceSlide(replacementMap, slide, document1);
                }
                try (FileOutputStream stream = new FileOutputStream(outStream)) {
                    document1.write(stream);
                }
            }
        });
    }

    private static <T> Stream<T> getShapeStream(XSLFSlide slide, Class<T> class1) {
        return slide.getShapes().stream().filter(class1::isInstance).map(class1::cast);
    }

    private static void recordPicture(XSLFPictureData data) {
        RunnableEx.run(() -> {
            File outFile = ResourceFXUtils.getOutFile("ppt/" + data.getFileName());
            InputStream inputStream = data.getInputStream();
            ExtractUtils.copy(inputStream, outFile);
        });
    }

    private static void replaceSlide(Map<String, Object> replacementMap, XSLFSlide slide, XMLSlideShow ppt) {
        List<XSLFTextBox> shapeStream = getShapeStream(slide, XSLFTextBox.class).collect(Collectors.toList());
        for (XSLFTextBox textBox : shapeStream) {
            List<XDDFTextParagraph> paragraphs = textBox.getTextBody().getParagraphs();
            for (XDDFTextParagraph paragraph : paragraphs) {
                String text = paragraph.getText();
                Object object = replacementMap.getOrDefault(text, replacementMap.get(text.trim()));
                if (object instanceof List<?>) {
                    addImage(slide, ppt, (List<?>) object);
                } else if (object instanceof String) {
                    LOG.info("\"{}\" replaced to \"{}\"", text, object);
                    paragraph.setText((String) object);
                } else if (StringUtils.isNotBlank(text)) {
                    LOG.info("{}", text);
                }
            }
        }
    }

}
