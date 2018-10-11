package fxpro.ch04;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class AlignUsingStackAndTile extends Application {

    @Override
    public void start(Stage primaryStage) {
        StackPane left = new StackPane();
        left.setStyle("-fx-background-color: black");

        Text text = new Text("JavaFX");
        text.setFont(Font.font(null, FontWeight.BOLD, 18));
        text.setFill(Color.WHITE);
		Text right = new Text("Reversi");
		right.setFont(Font.font(null, FontWeight.BOLD, 18));
        StackPane.setAlignment(text, Pos.CENTER_RIGHT);
        left.getChildren().add(text);
        TilePane tiles = new TilePane();
        tiles.setSnapToPixel(false);
        TilePane.setAlignment(right, Pos.CENTER_LEFT);
        tiles.getChildren().addAll(left, right);
        Scene scene = new Scene(tiles, 400, 100);
        left.prefWidthProperty().bind(scene.widthProperty().divide(2));
        left.prefHeightProperty().bind(scene.heightProperty());

        primaryStage.setHeight(100);
        primaryStage.setWidth(400);

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}