package extract;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javax.imageio.ImageIO;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTHyperlink;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTR;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTText;
import org.slf4j.Logger;
import utils.ResourceFXUtils;
import utils.ex.HasLogging;
import utils.ex.RunnableEx;

public final class WordService {

    private static final Logger LOG = HasLogging.log();

    private static final int IMAGE_WIDTH = 300;

    private WordService() {
    }

    public static void addLink(XWPFParagraph paragraph, Object object) {
        substituirParagrafo(paragraph, "Email: ");
        removerLinks(paragraph);
        String id = paragraph.getDocument().getPackagePart()
                .addExternalRelationship(object.toString(), XWPFRelation.HYPERLINK.getRelation()).getId();
        // Append the link and bind it to the relationship
        CTHyperlink cLink = paragraph.getCTP().addNewHyperlink();
        cLink.setId(id);
        CTText ctText = CTText.Factory.newInstance();
        ctText.setStringValue(object.toString().split(":")[1]);
        CTR ctr = CTR.Factory.newInstance();
        ctr.setTArray(new CTText[] { ctText });
        // Insert the linked text into the link
        cLink.setRArray(new CTR[] { ctr });
    }


    public static void getWord(Map<String, Object> mapaSubstituicao, File arquivo, File outStream) {
        RunnableEx.run(() -> {
            try (InputStream resourceAsStream = new FileInputStream(arquivo);
                    XWPFDocument document1 = new XWPFDocument(resourceAsStream);
                    FileOutputStream stream = new FileOutputStream(outStream)) {
                changeHeader(mapaSubstituicao, document1);
                List<IBodyElement> bodyElements = document1.getBodyElements();
                bodyElements.stream().filter(e -> e.getElementType() == BodyElementType.PARAGRAPH).forEach(
                        (IBodyElement element) -> substituirParagrafo((XWPFParagraph) element, mapaSubstituicao));
                bodyElements.stream().filter(e -> e.getElementType() == BodyElementType.TABLE)
                        .forEach(tabela -> substituirTabela(tabela, mapaSubstituicao));
                document1.write(stream);
            }
        });
    }

    public static void getWord(Map<String, Object> mapaSubstituicao, String arquivo, File outStream) {
        getWord(mapaSubstituicao, ResourceFXUtils.toFile(arquivo), outStream);
    }

    private static void changeHeader(Map<String, Object> mapaSubstituicao, XWPFDocument document1) {
        for (XWPFHeader p : document1.getHeaderList()) {
            List<XWPFParagraph> paragraphs = p.getParagraphs();
            for (XWPFParagraph paragraph : paragraphs) {
                String text = paragraph.getText();
                if (mapaSubstituicao.containsKey(text) || mapaSubstituicao.containsKey(text.trim())) {
                    Object object = getObject(mapaSubstituicao, text);
                    substituirParagrafo(paragraph, Objects.toString(object, ""));
                }
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

    private static boolean isNotInMap(Map<String, Object> map, String cellText) {
        return StringUtils.isBlank(cellText) || !map.containsKey(cellText) && !map.containsKey(cellText.trim());
    }


    private static void removerLinks(XWPFParagraph paragraph) {
        int size = paragraph.getCTP().getHyperlinkList().size();
        for (int i = 0; i < size; i++) {
            paragraph.getCTP().removeHyperlink(0);
        }
    }

    private static void replaceImage(XWPFRun createRun, Image object) {
        RunnableEx.run(() -> {
            String imgFile = object.hashCode() + ".png";
            File outFile = File.createTempFile("png", imgFile);
            ImageIO.write(SwingFXUtils.fromFXImage(object, null), "PNG", outFile);
            createRun.addPicture(new FileInputStream(outFile), Document.PICTURE_TYPE_PNG, imgFile,
                    Units.toEMU(IMAGE_WIDTH), Units.toEMU(IMAGE_WIDTH / object.getWidth() * object.getHeight()));
        });
    }

    private static void setText(XWPFParagraph paragraph, String string) {
        List<XWPFRun> runs = paragraph.getRuns();
        XWPFRun xwpfRun = runs.isEmpty() ? paragraph.createRun() : runs.get(0);
        setText(xwpfRun, string);
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
        List<XWPFRun> runs = paragraph.getRuns();
        for (int i = 0; i < runs.size(); i++) {
            XWPFRun xwpfRun = runs.get(i);
            String text = xwpfRun.text();
            if (isNotInMap(mapaSubstituicao, text)) {
                continue;
            }
            LOG.info(text);
            Object object = getObject(mapaSubstituicao, text);
            if (object instanceof String) {
                setText(xwpfRun, object.toString());
            }
            if (object instanceof Collection) {
                for (Object ob0 : (Collection<?>) object) {
                    if (ob0 instanceof String) {
                        setText(xwpfRun, (String) ob0);
                    }
                    if (ob0 instanceof Image) {
                        replaceImage(xwpfRun, (Image) ob0);
                    }
                }
            }
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
                    LOG.info(cellText);
                    continue;
                }
                Object object = getObject(map, cellText);
                if (object != null) {
                    substituirCell(cell, object);
                }
            }
        }
    }

}
