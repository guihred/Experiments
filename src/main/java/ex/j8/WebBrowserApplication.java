package ex.j8;

import extract.ImageLoader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Comparator;
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
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebHistory.Entry;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import simplebuilder.SimpleButtonBuilder;
import utils.*;

/**
 * Using the web viewer, implement a browser with a URL bar and a back button.
 * Hint: WebEngine.getHistory().
 */

public class WebBrowserApplication extends Application {

    private static final Logger LOG = HasLogging.log();
    private WebEngine engine;
    private TextField siteField;

    @Override
    public void start(Stage stage) {
        ExtractUtils.insertProxyConfig();
        siteField = new TextField(ResourceFXUtils.toExternalForm("About.html"));
        WebView browser = new WebView();
        engine = browser.getEngine();
        siteField.setOnKeyReleased(ev -> {
            if (ev.getCode() == KeyCode.ENTER) {
                loadSite(siteField);
            }
        });
        Button backButton = SimpleButtonBuilder.newButton("<-",
                event -> RunnableEx.ignore(() -> engine.getHistory().go(engine.getHistory().getCurrentIndex() - 1)));
        Worker<Void> loadWorker = engine.getLoadWorker();
        loadWorker.stateProperty()
                .addListener((ob, oldValue, newState) -> stage.setTitle(engine.getLocation() + " " + newState));
        engine.locationProperty().addListener((ob, old, val) -> siteField.setText(val));
        loadWorker.exceptionProperty().addListener((ob, oldValue, newException) -> onException(newException));
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.progressProperty().bind(loadWorker.progressProperty());
        HBox top = new HBox();
        siteField.prefWidthProperty()
                .bind(browser.widthProperty().add(progressIndicator.widthProperty().add(50).negate()));
        top.getChildren().addAll(backButton, progressIndicator, siteField);
        BorderPane pane = new BorderPane();
        pane.setTop(top);
        pane.setCenter(browser);
        ListView<Text> varList = new ListView<>();
        ListView<String> linksList = new ListView<>();
        ListView<ImageView> imageList = new ListView<>();
        ListView<String> historyList = new ListView<>();
        final WebHistory history = engine.getHistory();
        history.getEntries().addListener((Change<? extends Entry> c) -> {
            c.next();
            c.getRemoved().stream().forEach(e -> historyList.getItems().remove(e.getUrl()));
            c.getAddedSubList().stream().forEach(e -> historyList.getItems().add(e.getUrl()));
        });
        Accordion value = new Accordion(new TitledPane("Links", linksList), new TitledPane("Variables", varList),
                new TitledPane("Images", imageList), new TitledPane("History", historyList));
        pane.setRight(value);

        addProperties(loadWorker, varList);
        addProperties(engine, varList);
        engine.setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:76.0) Gecko/20100101 Firefox/76.0");
        engine.documentProperty().addListener((ob, old, doc) -> {
            if (doc != null) {
                RunnableEx.run(() -> linksList.setItems(getLinks(doc)));
                RunnableEx.run(() -> imageList.setItems(getImgs(doc)));
            }
        });
        Scene scene = new Scene(pane);
        stage.setScene(scene);
        CommonsFX.addCSS(scene, "filesComparator.css");
        stage.setTitle("Web Browser");
        stage.show();

    }

    private ObservableList<ImageView> getImgs(Document doc) throws MalformedURLException {
        NodeList linkList = doc.getElementsByTagName("img");
        Property<String> currentDomain = new SimpleStringProperty(new URL(siteField.getText()).getHost());
        return IntStream.range(0, linkList.getLength()).mapToObj(linkList::item)
                .filter(e -> e.getAttributes().getNamedItem("src") != null)
                .map(FunctionEx.makeFunction(e -> ExtractUtils.addDomain(currentDomain,
                        e.getAttributes().getNamedItem("src").getTextContent())))
                .filter(StringUtils::isNotBlank).distinct().map(e -> {
                    LOG.info("image {}", e);
                    return e;
                }).map(FunctionEx.makeFunction(t -> ImageLoader.convertToImage(currentDomain.getValue(), t)))
                .filter(Objects::nonNull).sorted(Comparator.comparing(ImageLoader::byArea))
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
    }

    private ObservableList<String> getLinks(Document doc) throws MalformedURLException {
        NodeList linkList = doc.getElementsByTagName("a");
        Property<String> currentDomain = new SimpleStringProperty(new URL(siteField.getText()).getHost());
        return IntStream.range(0, linkList.getLength()).mapToObj(linkList::item)
                .filter(e -> e.getAttributes().getNamedItem("href") != null)
                .map(FunctionEx.makeFunction(e -> ExtractUtils.addDomain(currentDomain,
                        e.getAttributes().getNamedItem("href").getTextContent())))
                .distinct().collect(Collectors.toCollection(FXCollections::observableArrayList));
    }

    private void loadSite(TextField textField) {
        engine.load(textField.getText());
    }

    private void onException(Throwable newException) {
        if (newException == null) {
            return;
        }
        if ("SSL handshake failed".equalsIgnoreCase(newException.getMessage())) {
            // RunnableEx.run(() -> InstallCert.installCertificate(siteField.getText()))
            return;
        }
        if ("Malformed URL".equalsIgnoreCase(newException.getMessage())) {
            if (!siteField.getText().contains("://")) {
                siteField.setText("http://" + siteField.getText());
            }
            loadSite(siteField);
            return;
        }
        LOG.info("ERROR LOADING {}", siteField.getText(), newException);
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