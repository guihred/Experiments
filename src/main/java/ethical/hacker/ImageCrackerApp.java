package ethical.hacker;

import static utils.RunnableEx.make;
import static utils.StringSigaUtils.toInteger;

import java.util.function.Supplier;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
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

public class ImageCrackerApp extends Application {
    private static final Logger LOG = HasLogging.log();
    private static final String URL = "https://www-sisgf/SisGF/faces/pages/index.xhtml";
    private BooleanProperty successfull = new SimpleBooleanProperty();
    private WebEngine engine;

    public BooleanProperty loadURL() {
        successfull.set(false);
        try {
            CrawlerTask.insertProxyConfig();
            engine.load(URL);
        } catch (Exception ex) {
            LOG.info("", ex);
        }
        return successfull;
    }

    @Override
    public void start(Stage stage) throws Exception {
        WebView browser = new WebView();

        engine = browser.getEngine();
        Button loadButton = CommonsFX.newButton("Go", e -> loadURL());
        engine.getLoadWorker().stateProperty().addListener((ob, oldValue, newState) -> {
            stage.setTitle(engine.getLocation() + " " + newState);
            if (newState == State.SUCCEEDED) {
                try {
                    new Thread(() -> tryToLog(browser)).start();
                } catch (Exception e1) {
                    LOG.error("", e1);
                }
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

    private void runInPlatform(String setValue) {

        Platform.runLater(make(() -> engine.executeScript(setValue)));
    }

    private Object runInPlatformAndWait(String setValue) {
        return runInPlatformAndWait(() -> engine.executeScript(setValue));
    }

    private <T> T runInPlatformAndWait(Supplier<T> expression) {
        ObjectProperty<T> obj = new SimpleObjectProperty<>();
        Platform.runLater(make(() -> obj.set(expression.get())));
        while (obj.get() == null) {
            // DO NOTHING
        }
        return obj.get();
    }

    private void tryToLog(WebView browser) {
        runInPlatform(setValue("j_username", CrawlerTask.getHTTPUsername()));
        runInPlatform(setValue("j_password", CrawlerTask.getHTTPPassword()));
        JSObject o = (JSObject) runInPlatformAndWait("$('#dtpCaptcha img').offset()");

        Integer width = toInteger(runInPlatformAndWait("$('#dtpCaptcha img').width()")) * 6 / 5;
        Integer height = toInteger(runInPlatformAndWait("$('#dtpCaptcha img').innerHeight()")) * 3 / 2;
        Integer top = toInteger(runInPlatformAndWait(() -> o.getMember("top")));
        Integer left = toInteger(runInPlatformAndWait(() -> o.getMember("left")));

        Rectangle2D viewport = new Rectangle2D(left, top, width, height);
        for (int i = 0; i < 5 && !successfull.get(); i++) {
            waitABit();
            WritableImage take = runInPlatformAndWait(() -> take(browser, viewport));
            WritableImage createSelectedImage = ImageCracker.createSelectedImage(take);
            String cracked = ImageCracker.crackImage(createSelectedImage);
            LOG.info("cracked Image = {} tries={}", cracked, i + 1);
            String crackImage = cracked.replaceAll("\\D", "");
            if (crackImage.matches("\\d{4}")) {
                runInPlatform(setValue("captchaId", crackImage));
                runInPlatform("$('#btnRegistrar').click()");
                successfull.set(true);
            } else {
                runInPlatform("$('#dtpCaptcha a').get(1).click()");
            }
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
        } catch (Exception e) {
            LOG.error("ERROR ", e);
            return null;
        }
    }

    public static void waitABit() {
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            LOG.error("NOT SUPPOSED TO HAPPEN", e);
        }
    }

    private static String setValue(String id, String httpUsername) {
        return String.format("document.getElementById(\"%s\").value = \"%s\";", id, httpUsername);
    }
}
