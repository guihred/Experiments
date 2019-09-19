package extract;

import static utils.StringSigaUtils.removeMathematicalOperators;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.io.RandomAccessFile;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.slf4j.Logger;
import utils.HasLogging;
import utils.RunnableEx;
import utils.SupplierEx;

public final class PdfUtils {

    public static final String SPLIT_WORDS_REGEX = "[\\s]+";

    private static final Logger LOG = HasLogging.log();

    private PdfUtils() {
    }

    public static void extractImages(File file) {
        PdfUtils.extractImages(file, 0, 0);
    }

    public static Map<Integer, List<PdfImage>> extractImages(File file, int start, int nPages) {
        return extractImages(file, start, nPages, null);
    }

    public static Map<Integer, List<PdfImage>> extractImages(File file, int start, int nPages,
        DoubleProperty progress) {
        Map<Integer, List<PdfImage>> images = new ConcurrentHashMap<>();
        new Thread(() -> RunnableEx.ignore(() -> {
            try (RandomAccessFile source = new RandomAccessFile(file, "r");
                COSDocument cosDoc = parseAndGet(source);
                PDDocument pdDoc = new PDDocument(cosDoc)) {
                int nPag = nPages == 0 ? pdDoc.getNumberOfPages() : nPages;
                for (int i = start; i < nPag; i++) {
                    PrintImageLocations printImageLocations = new PrintImageLocations();
                    PDPage page = pdDoc.getPage(i);
                    List<PdfImage> pageImages = getPageImages(printImageLocations, i, page);
                    images.put(i, pageImages);
                    double current = i;
                    if (progress != null) {
                        Platform.runLater(() -> progress.set(current / (nPag - start)));
                    }
                }
            }
        })).start();
        return images;
    }

    public static String[] getAllLines(File file) {
        return SupplierEx.remap(() -> {
            try (RandomAccessFile source = new RandomAccessFile(file, "r");
                COSDocument cosDoc = PdfUtils.parseAndGet(source);
                PDDocument pdDoc = new PDDocument(cosDoc)) {
                PDFTextStripper pdfStripper = new PDFTextStripper();
                pdfStripper.setStartPage(1);
                String parsedText = pdfStripper.getText(pdDoc);
                return parsedText.split("\r\n");
            }
        }, "ERROR IN FILE " + file);
    }

    public static COSDocument parseAndGet(RandomAccessFile source) throws IOException {
        PDFParser parser = new PDFParser(source);
        parser.parse();
        return parser.getDocument();
    }

    public static PdfInfo readFile(File file1) {
        return readFile(new PdfInfo(), file1, null);
    }

    public static PdfInfo readFile(File file1, PrintStream out) {
        return readFile(new PdfInfo(), file1, out);
    }

    public static PdfInfo readFile(PdfInfo pdfInfo, File file1) {
        return readFile(pdfInfo, file1, null);
    }

    public static PdfInfo readFile(PdfInfo pdfInfo, File file1, PrintStream out) {
        if (file1 == null) {
            return pdfInfo;
        }
        pdfInfo.setProgress(0);
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
                if (out != null) {
                    lines1.forEach(out::println);
                }

                LOG.trace("READING PAGE {}", i);
            }
            pdfInfo.setNumberOfPages(pdfInfo.getNumberOfPages());
            pdfInfo.setImages(PdfUtils.extractImages(file1, 0, pdfInfo.getNumberOfPages(), pdfInfo.getProgress()));

            pdfInfo.getLines().setAll(pdfInfo.getPages().get(pdfInfo.getPageIndex()));

        } catch (Exception e) {
            LOG.error("", e);
        }

        return pdfInfo;
    }

    public static void runOnFile(File file, BiConsumer<String, List<TextPosition>> onTextPosition,
        IntConsumer onPage,
        Consumer<String[]> onLines, BiConsumer<Integer, List<PdfImage>> onImages) {
        PdfUtils.extractImages(file);
        try (RandomAccessFile source = new RandomAccessFile(file, "r");
            COSDocument cosDoc = PdfUtils.parseAndGet(source);
            PDDocument pdDoc = new PDDocument(cosDoc)) {
            PDFTextStripper pdfStripper = new PDFTextStripper() {
                @Override
                protected void writeString(String text1, List<TextPosition> textPositions) throws IOException {
                    super.writeString(text1, textPositions);
                    onTextPosition.accept(text1, textPositions);
                }
            };
            int numberOfPages = pdDoc.getNumberOfPages();
            PrintImageLocations printImageLocations = new PrintImageLocations();
            for (int i = 2; i <= numberOfPages; i++) {
                PDPage page = pdDoc.getPage(i - 1);
                onPage.accept(i);
                pdfStripper.setStartPage(i);
                pdfStripper.setEndPage(i);
                List<PdfImage> images = printImageLocations.processPage(page, i);
                String parsedText = removeMathematicalOperators(pdfStripper.getText(pdDoc));
                String[] lines = parsedText.split("\r\n");
                onLines.accept(lines);
                onImages.accept(i, images);
            }
        } catch (Throwable e) {
            LOG.error("", e);
        }
    }

    private static List<PdfImage> getPageImages(PrintImageLocations printImageLocations, int i, PDPage page) {
        return SupplierEx.get(() -> printImageLocations.processPage(page, i), Collections.emptyList());

    }

}
