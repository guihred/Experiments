package extract;

import static utils.StringSigaUtils.removeMathematicalOperators;
import static utils.ex.RunnableEx.ignore;
import static utils.ex.SupplierEx.get;
import static utils.ex.SupplierEx.remap;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import javafx.beans.property.Property;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.io.RandomAccessFile;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import utils.CommonsFX;
import utils.ResourceFXUtils;
import utils.ex.RunnableEx;
import utils.ex.SupplierEx;

public final class PdfUtils {

    private static final String ERROR_IN_FILE = "ERROR IN FILE ";

    public static final String SPLIT_WORDS_REGEX = "[\\s]+";

    private PdfUtils() {
    }

    public static void createPDFFromImage(File outputFile, BufferedImage... img) throws IOException {
        try (PDDocument doc = new PDDocument()) {
            for (BufferedImage bimg : img) {
                float width = bimg.getWidth();
                float height = bimg.getHeight();
                PDPage page = new PDPage(new PDRectangle(width, height));
                PDImageXObject pdImage = LosslessFactory.createFromImage(doc, bimg);
                try (PDPageContentStream contentStream =
                        new PDPageContentStream(doc, page, AppendMode.APPEND, true, true)) {
                    float scale = 1f;
                    contentStream.drawImage(pdImage, 0, 0, pdImage.getWidth() * scale, pdImage.getHeight() * scale);
                }
                doc.addPage(page);
            }

            doc.save(outputFile);
        }
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
        }, ERROR_IN_FILE + file);
    }

    public static COSDocument parseAndGet(RandomAccessFile source) {
        return remap(() -> {
            PDFParser parser = new PDFParser(source);
            parser.parse();
            return parser.getDocument();
        }, ERROR_IN_FILE + source);
    }

    public static PdfInfo readFile(File file1) {
        return readFile(new PdfInfo(), file1, null);
    }

    public static PdfInfo readFile(PdfInfo pdfInfo) {
        return readFile(pdfInfo, pdfInfo.getFile(), null);
    }

    public static PdfInfo readFile(PdfInfo pdfInfo, File file1) {
        return readFile(pdfInfo, file1, null);
    }

    public static PdfInfo readText(File file1) throws IOException {
        File outFile = ResourceFXUtils.getOutFile("txt/" + file1.getName().replaceAll("\\.pdf", ".txt"));
        return readText(new PdfInfo(file1), outFile);
    }

    public static PdfInfo readText(PdfInfo fileInfo, File outFile) throws IOException {
        try (PrintStream out = new PrintStream(outFile, StandardCharsets.UTF_8.displayName())) {
            return readFile(fileInfo, fileInfo.getFile(), out);
        }
    }

    public static void runOnFile(int init, File file, BiConsumer<String, List<TextPosition>> onTextPosition,
            IntConsumer onPage, Consumer<String[]> onLines, BiConsumer<Integer, List<PdfImage>> onImages) {
        PdfUtils.extractImages(file);
        ignore(() -> runOnLines(init, file, onTextPosition, onPage, onLines, onImages));
    }

    private static Map<Integer, List<PdfImage>> extractImages(File file) {
        return PdfUtils.extractImages(file, 0, 0);
    }

    private static Map<Integer, List<PdfImage>> extractImages(File file, int start, int nPages) {
        return extractImages(file, start, nPages, null);
    }

    private static Map<Integer, List<PdfImage>> extractImages(File file, int start, int nPages,
            Property<Number> progress) {
        Map<Integer, List<PdfImage>> images = new ConcurrentHashMap<>();
        RunnableEx.run(() -> RunnableEx.remap(() -> {
            try (RandomAccessFile source = new RandomAccessFile(file, "r");
                    COSDocument cosDoc = parseAndGet(source);
                    PDDocument pdDoc = new PDDocument(cosDoc)) {
                int nPag = nPages == 0 ? pdDoc.getNumberOfPages() : nPages;
                for (int i = start; i < nPag; i++) {
                    PrintImageLocations printImageLocations = new PrintImageLocations(file);
                    List<PdfImage> pageImages = getPageImages(printImageLocations, i, pdDoc.getPage(i));
                    images.put(i, pageImages);
                    CommonsFX.update(progress, (double) i / (nPag - start));
                }
                CommonsFX.update(progress, 1);
            }
        }, ERROR_IN_FILE + file));
        return images;
    }

    private static List<PdfImage> getPageImages(PrintImageLocations printImageLocations, int i, PDPage page) {
        return get(() -> printImageLocations.processPage(page, i), new ArrayList<>());
    }

    private static void read(PdfInfo pdfInfo, File file1, PrintStream out) throws IOException {
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
                for (String remappedLines : pageLines) {
                    if (remappedLines.split(SPLIT_WORDS_REGEX).length >= 4 * remappedLines.length() / 10) {
                        remappedLines = remappedLines.replaceAll("(?<=[^\\s]) (?=[^\\s])", "");
                    }
                    lines1.add(remappedLines.replaceAll("\t", " "));
                }
                pdfInfo.getPages().add(lines1);
                if (out != null) {
                    lines1.forEach(out::println);
                }

                CommonsFX.update(pdfInfo.getProgress(), (double) i / pdfInfo.getNumberOfPages());

            }
            CommonsFX.update(pdfInfo.getProgress(), 1);
            pdfInfo.setNumberOfPages(pdfInfo.getNumberOfPages());
            pdfInfo.setImages(PdfUtils.extractImages(file1, 0, pdfInfo.getNumberOfPages(), pdfInfo.getProgress()));

            pdfInfo.getLines().setAll(pdfInfo.getPages().get(pdfInfo.getPageIndex()));
        }
    }

    private static PdfInfo readFile(PdfInfo pdfInfo, File file1, PrintStream out) {
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
        ignore(() -> read(pdfInfo, file1, out));
        return pdfInfo;
    }

    private static void runOnLines(int init, File file, BiConsumer<String, List<TextPosition>> onTextPosition,
            IntConsumer onPage, Consumer<String[]> onLines, BiConsumer<Integer, List<PdfImage>> onImages)
            throws IOException {
        try (RandomAccessFile source = new RandomAccessFile(file, "r");
                COSDocument cosDoc = PdfUtils.parseAndGet(source);
                PDDocument pdDoc = new PDDocument(cosDoc)) {
            PDFTextStripper pdfStripper = new PDFTextStripper() {
                @Override
                protected void writeString(String text1, List<TextPosition> textPositions) {
                    RunnableEx.remap(() -> super.writeString(text1, textPositions), "ERRO WRITING");
                    onTextPosition.accept(text1, textPositions);
                }
            };
            int numberOfPages = pdDoc.getNumberOfPages();
            PrintImageLocations printImageLocations = new PrintImageLocations(file);
            for (int i = init; i <= numberOfPages; i++) {
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
        }
    }

}
