package ethical.hacker;

import static extract.DocumentHelper.addProperties;
import static extract.DocumentHelper.onDocumentChange;

import extract.DocumentHelper;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
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
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jsoup.Connection.Response;
import org.slf4j.Logger;
import simplebuilder.SimpleDialogBuilder;
import simplebuilder.SimpleListViewBuilder;
import utils.CommonsFX;
import utils.ExtractUtils;
import utils.ex.HasLogging;
import utils.ex.RunnableEx;

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
        SimpleListViewBuilder.onDoubleClick(historyList, this::loadSite);
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
        engine.setCreatePopupHandler(pop -> {
            WebView button = new WebView();
            new SimpleDialogBuilder().node(button).resizable(pop.isResizable()).build();
            return button.getEngine();
        });
        engine.locationProperty().addListener((ob, old, val) -> {
            siteField.setText(val);
            onDocumentChange(engine.getDocument(), getUrl(), linksList, imageList);
        });
        engine.documentProperty().addListener((ob, old, doc) -> onDocumentChange(doc, getUrl(), linksList, imageList));

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
        if ("Malformed URL".equalsIgnoreCase(message)) {
            if (!url.contains("://")) {
                siteField.setText("http://" + url);
                LOG.info("REMAPPING URL {} {}", url, getUrl());
                loadSite(getUrl());
            }
            return;
        }
        LOG.info("ERROR LOADING {} {}", url, message);
        if ("SSL handshake failed".equalsIgnoreCase(message)) {
            // RunnableEx.run(() -> InstallCert.installCertificate(siteField.getText()))
            return;
        }
        LOG.error("ERROR LOADING {}", url, newException);
        RunnableEx.runNewThread(() -> {

            Response executeRequest = ExtractUtils.executeRequest(url, cookies);
            String body = executeRequest.body();
            String contentType = executeRequest.contentType();
            if (contentType.contains("html")) {
                engine.loadContent(body, contentType);
            } else {
                HttpClient client = HttpClientBuilder.create().build();
                HttpGet post = new HttpGet(url);
                HttpResponse response = client.execute(post);
                HttpEntity entity = response.getEntity();
                BufferedReader rd =
                        new BufferedReader(new InputStreamReader(entity.getContent(), StandardCharsets.UTF_8));
                String collect = rd.lines().collect(Collectors.joining("\n"));
                engine.loadContent(collect);
            }

        });

    }

    public static void main(String[] args) {
        launch(args);
    }

}
