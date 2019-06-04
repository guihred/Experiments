package pdfreader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.io.RandomAccessFile;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import utils.HasLogging;

public final class PdfUtils {
    public static final String SPLIT_WORDS_REGEX = "[\\s]+";

    private static final Logger LOG = HasLogging.log();

    private PdfUtils() {

    }

    public static void extractImages(File file) {
        PdfUtils.extractImages(file, 0, 0);
    }

    public static Map<Integer, List<PdfImage>> extractImages(File file, int start, int nPages) {
        Map<Integer, List<PdfImage>> images = new ConcurrentHashMap<>();
        new Thread(() -> {
            try (RandomAccessFile source = new RandomAccessFile(file, "r");
                COSDocument cosDoc = parseAndGet(source);
                PDDocument pdDoc = new PDDocument(cosDoc)) {
                int nPag = nPages == 0 ? pdDoc.getNumberOfPages() : nPages;

                for (int i = start; i < nPag; i++) {
                    PrintImageLocations printImageLocations = new PrintImageLocations();
                    PDPage page = pdDoc.getPage(i);
                    List<PdfImage> pageImages = getPageImages(printImageLocations, i, page);
                    images.put(i, pageImages);
                }
            } catch (Exception e) {
                LOG.trace("", e);
            }
        }).start();
        return images;
    }

    public static COSDocument parseAndGet(RandomAccessFile source) throws IOException {
        PDFParser parser = new PDFParser(source);
        parser.parse();
        return parser.getDocument();
    }

    public static PdfInfo readFile(PdfInfo pdfInfo, File file1) {
        if (file1 == null) {
            return pdfInfo;
        }
        pdfInfo.setIndex(0);
        pdfInfo.setLineIndex(0);
        pdfInfo.getLines().clear();
        pdfInfo.getSkipLines().clear();
        pdfInfo.getWords().clear();
        pdfInfo.setFile(file1);
        pdfInfo.setPageIndex(0);
        pdfInfo.getPages().clear();
        try (RandomAccessFile source = new RandomAccessFile(file1, "r");
            COSDocument cosDoc = PdfUtils.parseAndGet(source);
            PDDocument pdDoc = new PDDocument(cosDoc)) {
            pdfInfo.setNumberOfPages(pdDoc.getNumberOfPages());
            PDFTextStripper pdfStripper = new PDFTextStripper();
            for (int i = 0; i < pdfInfo.getNumberOfPages(); i++) {
                pdfStripper.setStartPage(i);
                pdfStripper.setEndPage(i);
                String parsedText = pdfStripper.getText(pdDoc);

                String[] pageLines = parsedText.split("\r\n");
                List<String> lines1 = new ArrayList<>();
                for (String string : pageLines) {
                    if (string.split(SPLIT_WORDS_REGEX).length >= 4 * string.length() / 10) {
                        string = string.replaceAll("(?<=[^\\s]) (?=[^\\s])", "");
                    }
                    lines1.add(string.replaceAll("\t", " "));
                }
                pdfInfo.getPages().add(lines1);
                LOG.trace("READING PAGE {}", i);
            }
            pdfInfo.setNumberOfPages(pdfInfo.getNumberOfPages());
            pdfInfo.setImages(PdfUtils.extractImages(file1, 0, pdfInfo.getNumberOfPages()));

            pdfInfo.getLines().setAll(pdfInfo.getPages().get(pdfInfo.getPageIndex()));

        } catch (Exception e) {
            LOG.info("", e);
        }

        return pdfInfo;
    }

    private static List<PdfImage> getPageImages(PrintImageLocations printImageLocations, int i, PDPage page) {
        try {
            List<PdfImage> images1 = printImageLocations.processPage(page, i);
            LOG.trace("images extracted {}", images1);
            return images1;
        } catch (Exception e) {
            LOG.trace("", e);
            return Collections.emptyList();
        }
    }

}
