/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fxpro.ch04;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class ReversiSquareApp extends Application {

    @Override
    public void start(Stage primaryStage) {
		primaryStage.setScene(new Scene(new StackPane(new ReversiSquare())));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
