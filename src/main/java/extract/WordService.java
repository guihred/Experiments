package extract;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javax.imageio.ImageIO;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.*;
import org.slf4j.Logger;
import utils.ResourceFXUtils;
import utils.ex.HasLogging;
import utils.ex.RunnableEx;

public final class WordService {

    private static final Logger LOG = HasLogging.log();

    private static final int IMAGE_WIDTH = 300;

    private WordService() {
    }

    public static void getWord(Map<String, Object> mapaSubstituicao, String arquivo, File outStream) {
        getWord(mapaSubstituicao, ResourceFXUtils.toFile("models/" + arquivo), outStream);
    }

    public static void main(String[] args) {
        getWord(new HashMap<>(), "template-iris-reporte-eventos-v1.2.docx",
                ResourceFXUtils.getOutFile("docx/NotificacaoInc.docx"));
    }

    private static void changeHeader(Map<String, Object> mapaSubstituicao, XWPFDocument document1) {
        for (XWPFHeader p : document1.getHeaderList()) {
            List<XWPFParagraph> paragraphs = p.getParagraphs();
            for (XWPFParagraph paragraph : paragraphs) {

                replaceParagraph(mapaSubstituicao, paragraph);
            }
        }
    }

    private static void cleanParagraphs(XWPFTableCell cell) {
        int size = cell.getParagraphs().size();
        for (int i = 0; i < size; i++) {
            cell.removeParagraph(0);
        }

    }

    private static Object getObject(Map<String, Object> map, String cellText) {
        return map.getOrDefault(cellText, map.get(cellText.trim()));
    }

    private static void getWord(Map<String, Object> mapaSubstituicao, File arquivo, File outStream) {
        RunnableEx.run(() -> {
            try (InputStream resourceAsStream = new FileInputStream(arquivo);
                    XWPFDocument document1 = new XWPFDocument(resourceAsStream);
                    FileOutputStream stream = new FileOutputStream(outStream)) {
                changeHeader(mapaSubstituicao, document1);
                List<IBodyElement> bodyElements = document1.getBodyElements();
                replaceBodyElements(mapaSubstituicao, bodyElements);

                document1.write(stream);
            }
        });
    }

    private static boolean isNotInMap(Map<String, Object> map, String cellText) {
        return StringUtils.isBlank(cellText) || !map.containsKey(cellText) && !map.containsKey(cellText.trim());
    }

    private static void replaceBodyElements(Map<String, Object> mapaSubstituicao, List<IBodyElement> bodyElements) {
        bodyElements.stream().filter(e -> e.getElementType() == BodyElementType.PARAGRAPH)
                .forEach((IBodyElement element) -> substituirParagrafo((XWPFParagraph) element, mapaSubstituicao));
        bodyElements.stream().filter(e -> e.getElementType() == BodyElementType.TABLE)
                .forEach(tabela -> substituirTabela(tabela, mapaSubstituicao));

        Map<BodyElementType, List<IBodyElement>> collect = bodyElements.stream().filter(
                p -> p.getElementType() != BodyElementType.TABLE && p.getElementType() != BodyElementType.PARAGRAPH)
                .collect(Collectors.groupingBy(e -> e.getElementType()));
        if (!collect.isEmpty()) {
            LOG.info("{}", collect);
        }

    }

    private static void replaceCollection(XWPFRun xwpfRun, Collection<?> object) {
        for (Object ob0 : object) {
            if (ob0 instanceof String) {
                setText(xwpfRun, (String) ob0);
            }
            if (ob0 instanceof Image) {
                replaceImage(xwpfRun, (Image) ob0);
            }
        }
    }

    private static void replaceImage(XWPFRun createRun, Image object) {
        RunnableEx.run(() -> {
            String imgFile = object.hashCode() + ".png";
            File outFile = File.createTempFile("png", imgFile);
            ImageIO.write(SwingFXUtils.fromFXImage(object, null), "PNG", outFile);
            try (FileInputStream pictureData = new FileInputStream(outFile)) {
                createRun.addPicture(pictureData, Document.PICTURE_TYPE_PNG, imgFile, Units.toEMU(IMAGE_WIDTH),
                        Units.toEMU(IMAGE_WIDTH / object.getWidth() * object.getHeight()));
            }
        });
    }

    private static void replaceParagraph(Map<String, Object> mapaSubstituicao, XWPFParagraph paragraph) {
        String text = paragraph.getText();
        if (StringUtils.isNotBlank(text)) {
            LOG.info(text);
        }

        if (mapaSubstituicao.containsKey(text) || mapaSubstituicao.containsKey(text.trim())) {
            Object object = getObject(mapaSubstituicao, text);
            substituirParagrafo(paragraph, Objects.toString(object, ""));
        }
    }

    private static void setText(XWPFParagraph paragraph, String string) {
        List<XWPFRun> runs = paragraph.getRuns();
        XWPFRun xwpfRun = runs.isEmpty() ? paragraph.createRun() : runs.get(0);
        setText(xwpfRun, string);
        paragraph.setSpacingAfter(0);
    }

    private static void setText(XWPFRun xwpfRun, String string) {
        xwpfRun.setText(string, 0);
    }

    private static void substituirCell(XWPFTableCell cell, Object object) {
        List<XWPFParagraph> paragraphs = cell.getParagraphs();
        int size = paragraphs.size();

        XWPFParagraph paragraph = size > 0 ? cell.getParagraphs().get(0) : cell.addParagraph();
        if (object instanceof String) {
            setText(paragraph, (String) object);
        }
        if (object instanceof Image) {
            replaceImage(paragraph.createRun(), (Image) object);
        }
        if (object instanceof Collection) {
            Collection<?> object2 = (Collection<?>) object;
            if (!object2.isEmpty() && object2.iterator().next() instanceof String) {
                cleanParagraphs(cell);
            }
            for (Object ob0 : object2) {
                if (ob0 instanceof String) {
                    paragraph = cell.addParagraph();
                    setText(paragraph, (String) ob0);
                }
                if (ob0 instanceof Image) {
                    replaceImage(paragraph.createRun(), (Image) ob0);
                }
            }
        }

    }

    private static void substituirParagrafo(XWPFParagraph paragraph, Map<String, Object> mapaSubstituicao) {
        boolean repl = false;
        List<XWPFRun> runs = paragraph.getRuns();
        for (int i = 0; i < runs.size(); i++) {
            XWPFRun xwpfRun = runs.get(i);
            String text = xwpfRun.text();
            // LOG.info(text);
            if (isNotInMap(mapaSubstituicao, text)) {
                continue;
            }
            Object object = getObject(mapaSubstituicao, text);
            if (object instanceof String) {
                setText(xwpfRun, object.toString());
                repl = true;
            }
            if (object instanceof Collection) {
                replaceCollection(xwpfRun, (Collection<?>) object);
                repl = true;
            }
        }
        if (!repl) {
            replaceParagraph(mapaSubstituicao, paragraph);
        }
    }

    private static void substituirParagrafo(XWPFParagraph paragraph, String string) {
        setText(paragraph, string);
        int size = paragraph.getRuns().size();
        for (int j = 1; j < size; j++) {
            paragraph.removeRun(1);
        }

    }

    private static void substituirTabela(IBodyElement element, Map<String, Object> map) {
        XWPFTable tabela = (XWPFTable) element;
        int numberOfRows = tabela.getNumberOfRows();

        for (int i = 0; i < numberOfRows; i++) {
            XWPFTableRow row = tabela.getRow(i);
            List<XWPFTableCell> tableCells = row.getTableCells();
            for (int j = 0; j < tableCells.size(); j++) {
                XWPFTableCell cell = row.getCell(j);
                String cellText = cell.getText();
                if (isNotInMap(map, cellText)) {
                    replaceBodyElements(map, cell.getBodyElements());
                    continue;
                }
                LOG.info(cellText);
                Object object = getObject(map, cellText);
                if (object != null) {
                    substituirCell(cell, object);
                }
            }
        }
    }

}
