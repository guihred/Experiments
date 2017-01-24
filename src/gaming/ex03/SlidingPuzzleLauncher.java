/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gaming.ex03;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class SlidingPuzzleLauncher extends Application {
    SlidingPuzzleModel memoryModel;
    @Override
    public void start(Stage stage) throws Exception {
        final GridPane gridPane = new GridPane();
        gridPane.setBackground(new Background(new BackgroundFill(Color.BLUE, CornerRadii.EMPTY, Insets.EMPTY)));
        memoryModel = new SlidingPuzzleModel(gridPane);
        final Scene scene = new Scene(gridPane);
        stage.setScene(scene);
        stage.setWidth(200);
        stage.setHeight(200);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
