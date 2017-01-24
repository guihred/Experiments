/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fxproexercises.ch04;

import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.scene.SceneBuilder;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class ReversiSquareTest extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setScene(SceneBuilder.create()
                .root(new StackPane(new ReversiSquare()))
                .build());
        primaryStage.show();
    }
}
