package ethical.hacker;

import static ethical.hacker.DocumentHelper.addProperties;
import static ethical.hacker.DocumentHelper.onDocumentChange;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import javafx.application.Application;
import javafx.collections.ListChangeListener.Change;
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
        DocumentHelper.addProperties(loadWorker, varList);
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
            onDocumentChange(engine.getDocument(), getUrl(), linksList, imageList);
        });
        engine.documentProperty().addListener((ob, old, doc) -> onDocumentChange(doc, getUrl(), linksList, imageList));
        File outFile = ResourceFXUtils.getOutFile("cache");
        outFile.mkdir();
        engine.setUserDataDirectory(outFile);

    }

    public void loadSite(String url) {
        RunnableEx.ignore(() -> engine.load(url));
    }

    public void onActionButton6() {
        RunnableEx.ignore(() -> engine.getHistory().go(engine.getHistory().getCurrentIndex() - 1));
    }

    public void onKeyReleasedTextField8(KeyEvent ev) {
        if (ev.getCode() == KeyCode.ENTER) {
            loadSite(getUrl());
        }
    }

    @Override
    public void start(Stage primaryStage) {
        CommonsFX.loadFXML("WebBrowserApplication", "WebBrowserApplication.fxml", this, primaryStage);
    }

    private String getUrl() {
        return siteField.getText();
    }

    private void onException(Throwable newException) {
        if (newException == null) {
            return;
        }
        String message = newException.getMessage();
        String url = getUrl();
        LOG.info("ERROR LOADING {} {}", url, message);
        LOG.trace("ERROR LOADING {}", url, newException);
        if ("SSL handshake failed".equalsIgnoreCase(message)) {
            // RunnableEx.run(() -> InstallCert.installCertificate(siteField.getText()))
            return;
        }
        if ("Malformed URL".equalsIgnoreCase(message)) {
            if (!url.contains("://")) {
                siteField.setText("http://" + url);
                loadSite(getUrl());
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

}
