package ethical.hacker;

import extract.ImageLoader;
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
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import utils.*;

public class DocumentHelper {
    public static void addProperties(Object loadWorker, ListView<Text> value) {
        ClassReflectionUtils.allProperties(loadWorker, loadWorker.getClass()).forEach((s, prop) -> {
            Text e = new Text();
            e.textProperty().bind(Bindings.concat(StringSigaUtils.changeCase(s), ": ", prop));
            value.getItems().add(e);
        });
        value.getItems().sort(Comparator.comparing(Text::getText));
    }

    public static ObservableList<String> getByTagAttribute(String url, Document doc, String tagname, String attribute) {
        NodeList linkList = doc.getElementsByTagName(tagname);
        Property<String> currentDomain = getDomain(url);
        return IntStream.range(0, linkList.getLength()).mapToObj(linkList::item).map(Node::getAttributes)
                .flatMap(attributes -> IntStream.range(0, attributes.getLength()).mapToObj(attributes::item))
                .filter(e -> attribute.equalsIgnoreCase(e.getNodeName()))
                .map(FunctionEx.makeFunction(e -> ExtractUtils.addDomain(currentDomain, e.getTextContent()))).distinct()
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
    }

    public static Property<String> getDomain(String url) {
        return new SimpleStringProperty(SupplierEx.getIgnore(() -> new URL(url).getHost(), url));
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
                RunnableEx.runInPlatform(() -> {
                    linksList2.setItems(links);
                });
            });
            RunnableEx.runNewThread(() -> {
                ObservableList<ImageView> imgs = getImgs(url, doc);
                RunnableEx.runInPlatform(() -> {
                    imageList2.setItems(imgs);
                });
            });
        }
    }

}
