package ethical.hacker;

import extract.ImageLoader;
import java.io.File;
import java.net.URL;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebHistory.Entry;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.jsoup.Connection.Response;
import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import simplebuilder.SimpleButtonBuilder;
import simplebuilder.SimpleDialogBuilder;
import utils.*;

/**
 * Using the web viewer, implement a browser with a URL bar and a back button.
 * Hint: WebEngine.getHistory().
 */

public class WebBrowserApplication extends Application {

    private static final Logger LOG = HasLogging.log();
    private WebEngine engine;
    private TextField siteField;
    private Map<String, String> cookies = new HashMap<>();

    public void loadSite(String textField) {
        engine.load(textField);
    }

    @Override
    public void start(Stage stage) {
        ExtractUtils.insertProxyConfig();
        siteField = new TextField(ResourceFXUtils.toExternalForm("About.html"));
        WebView browser = new WebView();
        engine = browser.getEngine();
        Button backButton = SimpleButtonBuilder.newButton("<-",
                event -> RunnableEx.ignore(() -> engine.getHistory().go(engine.getHistory().getCurrentIndex() - 1)));
        Worker<Void> loadWorker = engine.getLoadWorker();
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.progressProperty().bind(loadWorker.progressProperty());
        HBox top = new HBox();
        BorderPane pane = new BorderPane();
        pane.setTop(top);
        pane.setCenter(browser);
        ListView<Text> varList = new ListView<>();
        ListView<String> linksList = new ListView<>();
        ListView<ImageView> imageList = new ListView<>();
        ListView<String> historyList = new ListView<>();
        Accordion value = new Accordion(new TitledPane("Links", linksList), new TitledPane("Variables", varList),
                new TitledPane("Images", imageList), new TitledPane("History", historyList));
        pane.setRight(value);
        Scene scene = new Scene(pane);
        stage.setScene(scene);
        CommonsFX.addCSS(scene, "filesComparator.css");
        stage.setTitle("Web Browser");
        stage.show();

        siteField.prefWidthProperty()
                .bind(browser.widthProperty().add(progressIndicator.widthProperty().add(50).negate()));
        top.getChildren().addAll(backButton, progressIndicator, siteField);
        loadWorker.stateProperty()
                .addListener((ob, oldValue, newState) -> stage.setTitle(engine.getLocation() + " " + newState));
        loadWorker.exceptionProperty().addListener((ob, oldValue, newException) -> onException(newException));
        siteField.setOnKeyReleased(ev -> {
            if (ev.getCode() == KeyCode.ENTER) {
                loadSite(siteField.getText());
            }
        });
        engine.getHistory().getEntries().addListener((Change<? extends Entry> c) -> {
            c.next();
            c.getRemoved().stream().forEach(e -> historyList.getItems().remove(e.getUrl()));
            c.getAddedSubList().stream().forEach(e -> historyList.getItems().add(e.getUrl()));
        });
        addProperties(loadWorker, varList);
        addProperties(engine, varList);
        engine.setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:76.0) Gecko/20100101 Firefox/76.0");
        engine.setConfirmHandler(str -> {
            LOG.info("CONFIRM {}", str);
            return true;
        });
        engine.setOnAlert(str -> LOG.info("ALERT {}", str.getData()));
        engine.setOnError(str -> LOG.info("ERROR {}", str.getException()));
        engine.setOnResized(str -> LOG.info("RESIZED {}", str.getData()));
        engine.setOnVisibilityChanged(str -> LOG.info("VISIBILITY CHANGED {}", str.getData()));
        engine.setPromptHandler(str -> {
            LOG.info("PROMPTED {}", str.getMessage());
            return str.getDefaultValue();
        });
        engine.setCreatePopupHandler(pop -> {
            WebView button = new WebView();
            new SimpleDialogBuilder().button(button).resizable(pop.isResizable()).build();
            return button.getEngine();
        });
        engine.locationProperty().addListener((ob, old, val) -> {
            siteField.setText(val);
            onDocumentChange(linksList, imageList, engine.getDocument());
        });
        engine.documentProperty().addListener((ob, old, doc) -> onDocumentChange(linksList, imageList, doc));
        File outFile = ResourceFXUtils.getOutFile("cache");
        outFile.mkdir();
        engine.setUserDataDirectory(outFile);

    }


    private ObservableList<String> getByTagAttribute(Document doc, String tagname, String string) {
        NodeList linkList = doc.getElementsByTagName(tagname);
        Property<String> currentDomain = getDomain();
        return IntStream.range(0, linkList.getLength()).mapToObj(linkList::item).map(Node::getAttributes)
                .flatMap(attributes -> IntStream.range(0, attributes.getLength()).mapToObj(attributes::item))
                .filter(e -> string.equalsIgnoreCase(e.getNodeName()))
                .map(FunctionEx.makeFunction(e -> ExtractUtils.addDomain(currentDomain, e.getTextContent()))).distinct()
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
    }

    private Property<String> getDomain() {
        return new SimpleStringProperty(
                SupplierEx.getIgnore(() -> new URL(siteField.getText()).getHost(), siteField.getText()));
    }

    private ObservableList<ImageView> getImgs(Document doc) {
        Property<String> currentDomain = getDomain();
        return getByTagAttribute(doc, "img", "src").stream()
                .map(FunctionEx.makeFunction(t -> ImageLoader.convertToImage(currentDomain.getValue(), t)))
                .filter(Objects::nonNull).sorted(Comparator.comparing(ImageLoader::byArea))
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
    }

    private ObservableList<String> getLinks(Document doc) {
        return getByTagAttribute(doc, "a", "href");
    }

    private void onDocumentChange(ListView<String> linksList, ListView<ImageView> imageList, Document doc) {
        if (doc != null) {
            RunnableEx.runNewThread(() -> {
                ObservableList<String> links = getLinks(doc);
                RunnableEx.runInPlatform(() -> linksList.setItems(links));
            });
            RunnableEx.runNewThread(() -> {
                ObservableList<ImageView> imgs = getImgs(doc);
                RunnableEx.runInPlatform(() -> imageList.setItems(imgs));
            });
        }
    }

    private void onException(Throwable newException) {
        if (newException == null) {
            return;
        }
        String message = newException.getMessage();
        String url = siteField.getText();
        LOG.info("ERROR LOADING {} {}", url, message);
        LOG.trace("ERROR LOADING {}", url, newException);
        if ("SSL handshake failed".equalsIgnoreCase(message)) {
            // RunnableEx.run(() -> InstallCert.installCertificate(siteField.getText()))
            return;
        }
        if ("Malformed URL".equalsIgnoreCase(message)) {
            if (!url.contains("://")) {
                siteField.setText("http://" + url);
                loadSite("http://" + url);
            }
            return;
        }
        RunnableEx.run(() -> {
            Response executeRequest = ExtractUtils.executeRequest(url, cookies);
            String body = executeRequest.body();
            String contentType = executeRequest.contentType();
            engine.loadContent(body, contentType);
        });

    }

    public static void main(String[] args) {
        launch(args);
    }

    private static void addProperties(Object loadWorker, ListView<Text> value) {
        ClassReflectionUtils.allProperties(loadWorker, loadWorker.getClass()).forEach((s, prop) -> {
            Text e = new Text();
            e.textProperty().bind(Bindings.concat(StringSigaUtils.changeCase(s), ": ", prop));
            value.getItems().add(e);
        });
        value.getItems().sort(Comparator.comparing(Text::getText));
    }
}