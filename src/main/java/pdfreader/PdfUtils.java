package pdfreader;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.apache.pdfbox.cos.*;
import org.apache.pdfbox.io.RandomAccessFile;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionGoTo;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineNode;
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

    public static void main(String[] args) {
        readFile(new PdfInfo(), new File(
            "C:\\Users\\guilherme.hmedeiros\\Documents\\Dev\\FXperiments\\Material\\SSH - The Secure Shell.pdf"));
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
                lines1.forEach(e -> System.out.println(e));

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

    static void printBookmark(List<COSObject> indentation) throws IOException {
        Map<String, Object> hashSet = new HashMap<>();
        Set<String> names = new HashSet<>();

        for (COSObject object : indentation) {
//            System.out.println(ClassReflectionUtils.getDescription(object));
            COSBase object2 = object.getObject();
            if (object2 instanceof COSDictionary) {
                COSDictionary a = (COSDictionary) object2;
                a.entrySet().stream().filter(e -> !hashSet.containsKey(e.getKey().getName()))
                    .forEach(e -> hashSet.put(e.getKey().getName(), extractObj(e)));
                String collect = a.keySet().stream().map(e -> e.getName()).collect(Collectors.joining(","));
                names.add(collect);

            }
        }
        hashSet.entrySet().forEach(System.out::println);
        names.forEach(System.out::println);
    }

    static void printBookmark(PDOutlineNode bookmark, String indentation) throws IOException {
        PDOutlineItem current = bookmark.getFirstChild();
        while (current != null) {
            if (current.getDestination() instanceof PDPageDestination) {
                PDPageDestination pd = (PDPageDestination) current.getDestination();
                System.out.println(indentation + "Destination page: " + (pd.retrievePageNumber() + 1));
            }
            if (current.getAction() instanceof PDActionGoTo) {
                PDActionGoTo gta = (PDActionGoTo) current.getAction();
                if (gta.getDestination() instanceof PDPageDestination) {
                    PDPageDestination pd = (PDPageDestination) gta.getDestination();
                    System.out.println(indentation + "Destination page: " + (pd.retrievePageNumber() + 1));
                }
            }
            System.out.println(indentation + current.getTitle());
            printBookmark(current, indentation + " - ");
            current = current.getNextSibling();
        }
    }

    private static Object extracted(COSBase value) {
        return extracted(value, 0);
    }

    private static Object extracted(COSBase value, int i) {
        if (i > 2) {
            return value;
        }
        if (value instanceof COSString) {
            COSString name = (COSString) value;
            return name.getASCII();
        }
        if (value instanceof COSFloat) {
            COSFloat name = (COSFloat) value;
            return name.floatValue();
        }
        if (value instanceof COSInteger) {
            COSInteger name = (COSInteger) value;
            return name.intValue();
        }
        if (value instanceof COSArray) {
            COSArray name = (COSArray) value;
            return name.toList().stream().map(e -> extracted(e, i + 1)).collect(Collectors.toList());
        }
        if (value instanceof COSName) {
            COSName name = (COSName) value;
            return name.getName();
        }
        if (value instanceof COSObject) {
            COSObject name = (COSObject) value;
            return extracted(name.getObject(), i + 1);
        }
        if (value instanceof COSDictionary) {
            COSDictionary name = (COSDictionary) value;
            Map<Object, Object> hashMap = new HashMap<>();
            for (Entry<COSName, COSBase> entry : name.entrySet()) {
                hashMap.put(extracted(entry.getKey(), i + 1), extracted(entry.getValue(), i + 1));
            }
            return hashMap;
        }

        return value;
    }

    private static Object extractObj(Entry<COSName, COSBase> obj) {
        COSBase value = obj.getValue();
        return extracted(value);
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
