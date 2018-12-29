package fxpro.ch04;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import utils.ClassReflectionUtils;
import utils.ResourceFXUtils;

public class AlignUsingStackAndTile extends Application {

    @Override
    public void start(Stage primaryStage) {
        StackPane left = new StackPane();
        Text text = new Text("JavaFX");
        text.setFill(Color.WHITE);
		Text right = new Text("Reversi");
        StackPane.setAlignment(text, Pos.CENTER_RIGHT);
        left.getChildren().add(text);
        TilePane tiles = new TilePane();
        tiles.setSnapToPixel(false);
        TilePane.setAlignment(right, Pos.CENTER_LEFT);
        tiles.getChildren().addAll(left, right);
        Scene scene = new Scene(tiles);
        left.prefWidthProperty().bind(scene.widthProperty().divide(2));
        left.prefHeightProperty().bind(scene.heightProperty());
        tiles.getStyleClass().add("title");
        scene.getStylesheets().add(ResourceFXUtils.toExternalForm("reversi.css"));
        ClassReflectionUtils.displayCSSStyler(scene, "reversi.css");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
