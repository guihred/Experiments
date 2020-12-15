package extract;

import java.awt.Rectangle;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.scene.image.Image;
import org.apache.poi.sl.usermodel.PictureData;
import org.apache.poi.xddf.usermodel.text.XDDFTextParagraph;
import org.apache.poi.xslf.usermodel.*;
import org.slf4j.Logger;
import utils.DateFormatUtils;
import utils.ExtractUtils;
import utils.ImageFXUtils;
import utils.ResourceFXUtils;
import utils.ex.HasLogging;
import utils.ex.RunnableEx;
import utils.ex.SupplierEx;

public final class PPTService {

    private static final Logger LOG = HasLogging.log();

    private PPTService() {
    }

    public static void getPowerPoint(Map<String, Object> mapaSubstituicao, String arquivo, File outStream) {
        getPowerPoint(mapaSubstituicao, ResourceFXUtils.toFile(arquivo), outStream);
    }

    public static void getPowerPointImages(File arquivo) {
        RunnableEx.run(() -> {
            try (XMLSlideShow a = new XMLSlideShow(new FileInputStream(arquivo))) {
                List<XSLFPictureData> pictureData = a.getPictureData();
                for (XSLFPictureData data : pictureData) {
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
        getPowerPoint(mapaSubstituicao, "modeloRelatorioRadarEventos.pptx", outFile);
    }

    private static void addImage(XSLFSlide slide, XMLSlideShow ppt, List<?> list) {
        if (list.isEmpty()) {
            return;
        }
        Object ob = list.remove(0);
        if (!(ob instanceof Image)) {
            return;
        }
        Image image = (Image) ob;
        byte[] pictureData = SupplierEx.get(() -> ImageFXUtils.toByteArray(image));
        XSLFPictureData pd = ppt.addPicture(pictureData, PictureData.PictureType.PNG);
        XSLFPictureShape picture = slide.createPicture(pd);
        int width = 600;
        int height = (int) (image.getHeight() * width / image.getWidth());
        picture.setAnchor(new Rectangle(60, 120, width, Math.min(300, height)));
    }

    private static void getPowerPoint(Map<String, Object> replacementMap, File arquivo, File outStream) {
        RunnableEx.run(() -> {
            try (InputStream resourceAsStream = new FileInputStream(arquivo);
                    XMLSlideShow document1 = new XMLSlideShow(resourceAsStream);
                    FileOutputStream stream = new FileOutputStream(outStream)) {
                for (XSLFSlide slide : document1.getSlides()) {
                    replaceSlide(replacementMap, slide, document1);
                }
                document1.write(stream);
            }
        });
    }

    private static void recordPicture(XSLFPictureData data) {
        RunnableEx.run(() -> {
            File outFile = ResourceFXUtils.getOutFile("ppt/" + data.getFileName());
            InputStream inputStream = data.getInputStream();
            ExtractUtils.copy(inputStream, outFile);
        });
    }

    private static void replaceSlide(Map<String, Object> replacementMap, XSLFSlide slide, XMLSlideShow ppt) {
        List<XSLFShape> shapes = slide.getShapes();
        for (int i = 0; i < shapes.size(); i++) {
            XSLFShape shape = shapes.get(i);
            if (shape instanceof XSLFTextBox) {
                XSLFTextBox textBox = (XSLFTextBox) shape;
                List<XDDFTextParagraph> paragraphs = textBox.getTextBody().getParagraphs();
                for (XDDFTextParagraph paragraph : paragraphs) {
                    String text = paragraph.getText();
                    Object object = replacementMap.getOrDefault(text, replacementMap.get(text.trim()));
                    if (object instanceof List<?>) {
                        List<?> object2 = (List<?>) object;
                        addImage(slide, ppt, object2);
                    } else if (object instanceof String) {
                        LOG.info("\"{}\" replaced to \"{}\"", text, object);
                        paragraph.setText((String) object);
                    } else {
                        LOG.info("{}", text);
                    }
                }
            }
        }
    }

}
