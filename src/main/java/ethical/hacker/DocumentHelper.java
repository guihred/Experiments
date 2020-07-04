package ethical.hacker;

import extract.ImageLoader;
import java.io.File;
import java.io.IOException;
import java.net.URL;
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
import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import utils.*;

public final class DocumentHelper {

    private static final Logger LOG = HasLogging.log();
    private DocumentHelper() {
    }
    public static void addProperties(Object loadWorker, ListView<Text> value) {
        ClassReflectionUtils.allProperties(loadWorker, loadWorker.getClass()).forEach((s, prop) -> {
            Text e = new Text();
            e.textProperty().bind(Bindings.concat(StringSigaUtils.changeCase(s), ": ", prop));
            e.textProperty().addListener((ob, old, val) -> LOG.info("{}", val));
            value.getItems().add(e);
        });
        value.getItems().sort(Comparator.comparing(Text::getText));
    }

    public static Document getDoc(File file) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = factory.newDocumentBuilder();
        return documentBuilder.parse(file);
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

    public static void onDocumentChange(Document doc, String url, ListView<String> linksList2,
            ListView<ImageView> imageList2) {
        if (doc != null) {
            RunnableEx.runNewThread(() -> {
                ObservableList<String> links = getLinks(url, doc);
                RunnableEx.runInPlatform(() -> linksList2.setItems(links));
            });
            RunnableEx.runNewThread(() -> {
                ObservableList<ImageView> imgs = getImgs(url, doc);
                RunnableEx.runInPlatform(() -> imageList2.setItems(imgs));
            });
        }
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
        return new SimpleStringProperty(SupplierEx.getIgnore(() -> {
            URL url2 = new URL(url);
            return url2.getHost();
        }, url));
    }

}
