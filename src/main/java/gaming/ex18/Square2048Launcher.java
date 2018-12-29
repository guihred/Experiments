/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gaming.ex18;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import utils.ResourceFXUtils;

public class Square2048Launcher extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        final GridPane gridPane = new GridPane();
        final Square2048Model memoryModel = new Square2048Model(gridPane);

        final Scene scene = new Scene(gridPane);
        stage.setScene(scene);
        scene.setOnKeyPressed(memoryModel::handleKeyPressed);
        scene.getStylesheets().add(ResourceFXUtils.toExternalForm("square2048.css"));
        stage.show();
    }
    public static void main(String[] args) {
        launch(args);
    }
}
