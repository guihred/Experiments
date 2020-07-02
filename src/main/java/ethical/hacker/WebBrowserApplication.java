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
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
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
import simplebuilder.SimpleDialogBuilder;
import utils.*;

public class WebBrowserApplication extends Application {
    private static final Logger LOG = HasLogging.log();
    @FXML
    private ListView<Text> varList;
    @FXML
    private ListView<String> linksList;
    @FXML
    private ListView<ImageView> imageList;
    @FXML
    private ListView<String> historyList;
    @FXML
    private TextField siteField;
    @FXML
    private ProgressIndicator progressIndicator;
    @FXML
    private WebView browser;
    private WebEngine engine;
    private Map<String, String> cookies = new HashMap<>();

    public void initialize() {
        ExtractUtils.insertProxyConfig();
        engine = browser.getEngine();
        Worker<Void> loadWorker = engine.getLoadWorker();
        progressIndicator.progressProperty().bind(loadWorker.progressProperty());
        siteField.prefWidthProperty()
                .bind(browser.widthProperty().add(progressIndicator.widthProperty().add(50).negate()));
        loadWorker.stateProperty().addListener((ob, oldValue, newState) -> ((Stage) siteField.getScene().getWindow())
                .setTitle(engine.getLocation() + " " + newState));
        loadWorker.exceptionProperty().addListener((ob, oldValue, newException) -> onException(newException));
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
            onDocumentChange(engine.getDocument());
        });
        engine.documentProperty().addListener((ob, old, doc) -> onDocumentChange(doc));
        File outFile = ResourceFXUtils.getOutFile("cache");
        outFile.mkdir();
        engine.setUserDataDirectory(outFile);

    }

    public void loadSite(String textField) {
        RunnableEx.ignore(() -> engine.load(textField));
    }

    public void onActionButton6() {
        RunnableEx.ignore(() -> engine.getHistory().go(engine.getHistory().getCurrentIndex() - 1));
    }

    public void onKeyReleasedTextField8(KeyEvent ev) {
        if (ev.getCode() == KeyCode.ENTER) {
            loadSite(siteField.getText());
        }
    }

    @Override
    public void start(Stage primaryStage) {
        CommonsFX.loadFXML("WebBrowserApplication", "WebBrowserApplication.fxml", this, primaryStage);
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

    private void onDocumentChange(Document doc) {
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
