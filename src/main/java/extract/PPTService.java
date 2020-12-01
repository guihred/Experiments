package extract;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.poi.xslf.usermodel.*;
import org.slf4j.Logger;
import utils.DateFormatUtils;
import utils.ExtractUtils;
import utils.ResourceFXUtils;
import utils.ex.HasLogging;
import utils.ex.RunnableEx;

public final class PPTService {

    private static final Logger LOG = HasLogging.log();

    private PPTService() {
    }

    public static void getPowerPoint(Map<String, Object> mapaSubstituicao, String arquivo, File outStream) {
        getPowerPoint(mapaSubstituicao, ResourceFXUtils.toFile(arquivo), outStream);
    }

    public static void getPowerPointImages(String arquivo) {
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
        mapaSubstituicao.put(
                "DATAPREV/DIT/SUOP/DESO/DMPR\r\n\r\n\r\nDIA: DD/MM/AAAA\r\n\r\n\r\nHora: HH/MM h  ",
                String.format("DATAPREV/DIT/SUOP/DESO/DMPR\r\n\r\n\r\nDIA: %s\r\n\r\n\r\nHora: %s h  ",
                        DateFormatUtils.currentDate(), DateFormatUtils.currentHour()));
        getPowerPoint(mapaSubstituicao, "modeloRelatorioRadarEventos.pptx", ResourceFXUtils.getOutFile("result.pptx"));
    }

    private static void getPowerPoint(Map<String, Object> replacementMap, File arquivo, File outStream) {
        RunnableEx.run(() -> {
            try (InputStream resourceAsStream = new FileInputStream(arquivo);
                    XMLSlideShow document1 = new XMLSlideShow(resourceAsStream);
                    FileOutputStream stream = new FileOutputStream(outStream)) {
                for (XSLFSlide slide : document1.getSlides()) {
                    List<XSLFShape> shapes = slide.getShapes();
                    for (XSLFShape shape : shapes) {
                        if (shape instanceof XSLFTextBox) {
                            XSLFTextBox textBox = (XSLFTextBox) shape;
                            String text = textBox.getText();
                            LOG.info("{} ", text);
                            Object object = replacementMap.get(text);
                            if (object instanceof String) {
                                LOG.info("replaced to {}", object);
                                textBox.setText((String) object);
                            }
                        }
                    }
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

}
