package extract.web;

import com.google.common.io.Files;
import java.io.File;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javafx.beans.binding.Bindings;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import utils.ClassReflectionUtils;
import utils.CommonsFX;
import utils.ExtractUtils;
import utils.StringSigaUtils;
import utils.ex.FunctionEx;
import utils.ex.HasLogging;
import utils.ex.RunnableEx;
import utils.ex.SupplierEx;

public final class DocumentHelper {

    private static final Logger LOG = HasLogging.log();

    private DocumentHelper() {
    }

    public static void addProperties(Object loadWorker, ListView<Text> value) {
        ClassReflectionUtils.allProperties(loadWorker, loadWorker.getClass()).forEach((s, prop) -> {
            Text e = new Text();
            e.textProperty().bind(Bindings.concat(StringSigaUtils.changeCase(s), ": ", prop));
            e.textProperty().addListener((ob, old, val) -> LOG.debug("{}", val));
            value.getItems().add(e);
        });
        value.getItems().sort(Comparator.comparing(Text::getText));
    }

    public static Document getDoc(File file) {
        return SupplierEx.remap(() -> {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
            dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            dbf.setXIncludeAware(false);
            dbf.setExpandEntityReferences(false);
            return dbf.newDocumentBuilder().parse(file);
        }, "ERROR PARSING " + file);
    }

    public static ObservableList<ImageView> getImgs(String url, Document doc) {
        Property<String> currentDomain = getDomain(url);
        return getByTagAttribute(url, doc, "img", "src").stream()
                .map(FunctionEx.makeFunction(t -> ImageLoader.convertToImage(currentDomain.getValue(), t)))
                .filter(Objects::nonNull).sorted(Comparator.comparing(ImageLoader::byArea))
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
    }

    public static ObservableList<String> getLinks(String url, Document doc) {
        return getByTagAttribute(url, doc, "a", "href");
    }

    public static Document newDocument() throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        DocumentBuilder documentBuilder = factory.newDocumentBuilder();
        return documentBuilder.newDocument();
    }

    public static void onDocumentChange(Document doc, String url, ListView<String> linksList2,
            ListView<ImageView> imageList2) {
        if (doc != null) {
            RunnableEx.runNewThread(() -> {
                ObservableList<String> links = getLinks(url, doc);
                CommonsFX.runInPlatform(() -> linksList2.setItems(links));
            });
            RunnableEx.runNewThread(() -> {
                ObservableList<ImageView> imgs = getImgs(url, doc);
                CommonsFX.runInPlatform(() -> imageList2.setItems(imgs));
            });
        }
    }

    public static void saveToFile(Document document, File file) throws TransformerException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        transformerFactory.setFeature("http://javax.xml.XMLConstants/feature/secure-processing", true);
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        DOMSource domSource = new DOMSource(document);
        StreamResult streamResult = new StreamResult(file);
        transformer.transform(domSource, streamResult);
    }

    public static void saveToHtmlFile(Document document, File file) throws TransformerException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        transformerFactory.setFeature("http://javax.xml.XMLConstants/feature/secure-processing", true);
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        DOMSource domSource = new DOMSource(document);
        StreamResult streamResult = new StreamResult(file);
        transformer.transform(domSource, streamResult);
        RunnableEx.run(() -> {
            String htmlContent = "<!DOCTYPE html>\n" + Files.toString(file, StandardCharsets.UTF_8)
                    .replaceAll("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", "");
            String lowerCase = StringSigaUtils.replaceToLowerCase(htmlContent);
            Files.write(lowerCase, file, StandardCharsets.UTF_8);
        });

    }

    private static ObservableList<String> getByTagAttribute(String url, Document doc, String tagname,
            String attribute) {
        NodeList linkList = doc.getElementsByTagName(tagname);
        Property<String> currentDomain = getDomain(url);
        return IntStream.range(0, linkList.getLength()).mapToObj(linkList::item).map(Node::getAttributes)
                .flatMap(attributes -> IntStream.range(0, attributes.getLength()).mapToObj(attributes::item))
                .filter(e -> attribute.equalsIgnoreCase(e.getNodeName()))
                .map(FunctionEx.makeFunction(e -> ExtractUtils.addDomain(currentDomain, e.getTextContent()))).distinct()
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
    }

    private static Property<String> getDomain(String url) {
        return new SimpleStringProperty(SupplierEx.getIgnore(() -> new URL(url).getHost(), url));
    }

}
