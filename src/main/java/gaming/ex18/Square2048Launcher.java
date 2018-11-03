/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gaming.ex18;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class Square2048Launcher extends Application {

    private static final int WIDTH = 400;
    @Override
    public void start(Stage stage) throws Exception {
        final GridPane gridPane = new GridPane();
        gridPane.setBackground(new Background(new BackgroundFill(Color.BLUE, CornerRadii.EMPTY, Insets.EMPTY)));

		final Square2048Model memoryModel = new Square2048Model();

        for (int i = 0; i < memoryModel.getMap().length; i++) {
            for (int j = 0; j < memoryModel.getMap()[i].length; j++) {
                Square2048 map1 = memoryModel.getMap()[i][j];
                gridPane.add(map1, i, j);
            }
        }

        final BorderPane borderPane = new BorderPane(gridPane);
        final Scene scene = new Scene(borderPane);
        stage.setScene(scene);
        stage.setWidth(WIDTH);
        stage.setHeight(WIDTH);

        scene.setOnKeyPressed(memoryModel::handleKeyPressed);
        stage.show();
    }
    public static void main(String[] args) {
        launch(args);
    }
}
