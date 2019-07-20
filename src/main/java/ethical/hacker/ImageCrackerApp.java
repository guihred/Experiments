package ethical.hacker;

import javafx.application.Application;
import javafx.concurrent.Worker.State;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import netscape.javascript.JSObject;
import org.slf4j.Logger;
import utils.CommonsFX;
import utils.CrawlerTask;
import utils.HasLogging;
import utils.StringSigaUtils;

public class ImageCrackerApp extends Application {
    private static final Logger LOG = HasLogging.log();
    private static final String URL = "https://www-sisgf/SisGF/faces/pages/index.xhtml";
    private boolean successfull;
    private WebEngine engine;

    public void loadURL() {
        try {
            CrawlerTask.insertProxyConfig();
            engine.load(URL);
        } catch (Exception ex) {
            LOG.info("", ex);
        }
    }

    @Override
    public void start(Stage stage) throws Exception {
        WebView browser = new WebView();

        engine = browser.getEngine();
        Button loadButton = CommonsFX.newButton("Go", e -> loadURL());
        engine.getLoadWorker().stateProperty().addListener((ob, oldValue, newState) -> {
            stage.setTitle(engine.getLocation() + " " + newState);
            if (newState == State.SUCCEEDED) {
                tryToLog(browser, engine);
            }
        });

        HBox top = new HBox();
        top.getChildren().addAll(loadButton);
        BorderPane pane = new BorderPane();
        pane.setTop(top);
        pane.setCenter(browser);
        stage.setScene(new Scene(pane));
        stage.setTitle("Image Cracker");
        stage.show();
    }

    private void tryToLog(WebView browser, WebEngine engine) {
        engine.executeScript(String.format("$('#j_username').val('%s')", CrawlerTask.getHTTPUsername()));
        engine.executeScript(String.format("$('#j_password').val('%s')", CrawlerTask.getHTTPPassword()));
        JSObject o = (JSObject) engine.executeScript("$('#dtpCaptcha img').offset()");

        Integer width = StringSigaUtils.toInteger(engine.executeScript("$('#dtpCaptcha img').width()")) * 6 / 5;
        Integer height = StringSigaUtils.toInteger(engine.executeScript("$('#dtpCaptcha img').innerHeight()")) * 3 / 2;
        Integer top = StringSigaUtils.toInteger(o.getMember("top"));
        Integer left = StringSigaUtils.toInteger(o.getMember("left"));

        Rectangle2D viewport = new Rectangle2D(left, top, width, height);
        WritableImage take = take(browser, viewport);
        WritableImage createSelectedImage = ImageCracker.createSelectedImage(take);
        String crackImage = ImageCracker.crackImage(createSelectedImage).replaceAll("\\D", "");
        LOG.info("cracked Image = {}", crackImage);
        if (!successfull && crackImage.matches("\\d{4}")) {
            engine.executeScript(String.format("$('#captchaId').val('%s')", crackImage));
            engine.executeScript("$('#btnRegistrar').click()");
            successfull = true;
        }
    }

    public static void main(String[] args) {
        CrawlerTask.insertProxyConfig();
        launch(args);
    }

    public static WritableImage take(Node canvas, Rectangle2D viewport) {
        try {
            WritableImage writableImage = new WritableImage((int) viewport.getWidth(), (int) viewport.getHeight());
            SnapshotParameters params = new SnapshotParameters();
            params.setViewport(viewport);
            return canvas.snapshot(params, writableImage);
        } catch (final Exception e) {
            LOG.error("ERROR ", e);
            return null;
        }
    }
}
