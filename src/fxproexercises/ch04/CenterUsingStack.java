/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fxproexercises.ch04;

import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class CenterUsingStack extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        Text text = new Text("JavaFX Reversi");
        text.setFont(Font.font(null, FontWeight.BOLD, 18));
        text.setFill(Color.WHITE);
        Ellipse ellipse = new Ellipse();
        StackPane stack = new StackPane();
        stack.getChildren().addAll(ellipse, text);
        Scene scene = new Scene(stack, 400, 100);
        ellipse.radiusXProperty().bind(scene.widthProperty().divide(2));
        ellipse.radiusYProperty().bind(scene.heightProperty().divide(2));
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
