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
import pdfreader.PrintImageLocations.PDFImage;
import utils.HasLogging;

public final class PdfUtils {
    public static final String SPLIT_WORDS_REGEX = "[\\s]+";

    private static final Logger LOG = HasLogging.log();
    private PdfUtils() {

    }

    public static void extractImages(File file) {
        PdfUtils.extractImages(file, 0, 0);
    }

    public static Map<Integer, List<PDFImage>> extractImages(File file, int start, int nPages) {
        Map<Integer, List<PDFImage>> images = new ConcurrentHashMap<>();
        new Thread(() -> {
            try (RandomAccessFile source = new RandomAccessFile(file, "r");
                    COSDocument cosDoc = parseAndGet(source);
                    PDDocument pdDoc = new PDDocument(cosDoc)) {
                int nPag = nPages == 0 ? pdDoc.getNumberOfPages() : nPages;

                for (int i = start; i < nPag; i++) {
                    PrintImageLocations printImageLocations = new PrintImageLocations();
                    PDPage page = pdDoc.getPage(i);
                    List<PDFImage> pageImages = getPageImages(printImageLocations, i, page);
                    images.put(i, pageImages);
                }
            } catch (Exception e) {
                LOG.info("", e);
            }
        }).start();
        return images;
    }

    public static COSDocument parseAndGet(RandomAccessFile source) throws IOException {
        PDFParser parser = new PDFParser(source);
        parser.parse();
        return parser.getDocument();
    }

    public static PdfInfo readFile(File file1) {
        PdfInfo pdfInfo = new PdfInfo();
        pdfInfo.setFile(file1);
        try (RandomAccessFile source = new RandomAccessFile(file1, "r");
                COSDocument cosDoc = PdfUtils.parseAndGet(source);
                PDDocument pdDoc = new PDDocument(cosDoc)) {
            pdfInfo.setNumberOfPages(pdDoc.getNumberOfPages());
            int start = 0;
            PDFTextStripper pdfStripper = new PDFTextStripper();
            for (int i = start; i < pdfInfo.getNumberOfPages(); i++) {
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
            }
            pdfInfo.setNumberOfPages(pdfInfo.getNumberOfPages() - start);
            pdfInfo.setImages(PdfUtils.extractImages(file1, start, pdfInfo.getNumberOfPages()));
        } catch (Exception e) {
            HasLogging.log().error("", e);
        }
        return pdfInfo;
    }

    private static List<PDFImage> getPageImages(PrintImageLocations printImageLocations, int i, PDPage page) {
        try {
            List<PDFImage> images1 = printImageLocations.processPage(page, i);
            LOG.info("images extracted {}", images1);
            return images1;
        } catch (Exception e) {
            LOG.info("", e);
            return Collections.emptyList();
        }
    }

}
