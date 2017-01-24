/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gaming.ex02;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 *
 * @author Note
 */
public class MemoryLauncher extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        final GridPane gridPane = new GridPane();
        gridPane.setBackground(new Background(new BackgroundFill(Color.BLUE, CornerRadii.EMPTY, Insets.EMPTY)));

        final MemoryModel memoryModel = new MemoryModel();

        for (int i = 0; i < memoryModel.map.length; i++) {
            for (int j = 0; j < memoryModel.map[i].length; j++) {
                MemorySquare map1 = memoryModel.map[i][j];
                gridPane.add(new StackPane(map1, map1.shape), i, j);
            }
        }

        final BorderPane borderPane = new BorderPane(gridPane);
        final Scene scene = new Scene(borderPane);
        stage.setScene(scene);
        stage.setWidth(400);
        stage.setHeight(200);
        stage.show();
    }
    public static void main(String[] args) {
        launch(args);
    }
}
