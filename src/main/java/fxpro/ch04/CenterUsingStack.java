/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fxpro.ch04;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import utils.CommonsFX;

public class CenterUsingStack extends Application {

    @Override
    public void start(Stage primaryStage) {
        Text text = new Text("JavaFX Reversi");
        text.setFill(Color.WHITE);
        Ellipse ellipse = new Ellipse();
        StackPane stack = new StackPane();
        stack.getChildren().addAll(ellipse, text);
        Scene scene = new Scene(stack, 500, 500);
        ellipse.radiusXProperty().bind(scene.widthProperty().divide(2));
        ellipse.radiusYProperty().bind(scene.heightProperty().divide(2));
        CommonsFX.addCSS(scene, "reversi.css");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
