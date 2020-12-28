package fxsamples;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import utils.CommonsFX;

public class WebViewSample extends Application {

    @Override
    public void start(Stage stage) {
        stage.setTitle("Web View Sample");
        Scene scene = new Scene(new BrowserView(), 900, 600, Color.web("#666970"));
        stage.setScene(scene);
        CommonsFX.addCSS(scene, "BrowserToolbar.css");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}