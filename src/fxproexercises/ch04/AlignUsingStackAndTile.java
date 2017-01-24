package fxproexercises.ch04;

import javafx.application.Application;
import static javafx.application.Application.launch;
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

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        StackPane left = new StackPane();
        left.setStyle("-fx-background-color: black");

        Text text = new Text("JavaFX");
        text.setFont(Font.font(null, FontWeight.BOLD, 18));
        text.setFill(Color.WHITE);
        StackPane.setAlignment(text, Pos.CENTER_RIGHT);
        left.getChildren().add(text);
        Text right = new Text("Reversi");
        right.setFont(Font.font(null, FontWeight.BOLD, 18));
        TilePane tiles = new TilePane();
        tiles.setSnapToPixel(false);
        TilePane.setAlignment(right, Pos.CENTER_LEFT);
        tiles.getChildren().addAll(left, right);
        Scene scene = new Scene(tiles, 400, 100);
        left.prefWidthProperty().bind(scene.widthProperty().divide(2));
        left.prefHeightProperty().bind(scene.heightProperty());

        primaryStage.setHeight(100);
        primaryStage.setWidth(400);
        ;
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
