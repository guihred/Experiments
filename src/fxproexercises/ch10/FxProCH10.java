/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fxproexercises.ch10;

import java.util.ArrayList;
import java.util.List;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.effect.BoxBlur;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;
import others.SimpleCircleBuilder;

public class FxProCH10 extends Application {

    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Vanishing Circles");
        Group root = new Group();
        Scene scene = new Scene(root, 800, 600, Color.BLACK);
        List<Circle> circles = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            final Circle circle = new SimpleCircleBuilder().radius(150)
            		.centerX(Math.random() * 800)
            		.centerY(Math.random() * 600)
            		.fill(new Color(Math.random(), Math.random(), Math.random(), .2))
            		.effect(new BoxBlur(10, 10, 3))
            		.stroke(Color.WHITE)
            		.build()
            		;
            circle.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent t) -> {
                KeyValue collapse = new KeyValue(circle.radiusProperty(), 0);
                new Timeline(new KeyFrame(Duration.seconds(3), collapse)).play();
            });
            circle.strokeWidthProperty().bind(Bindings.when(circle.hoverProperty())
                    .then(4)
                    .otherwise(0));
            circles.add(circle);
        }
        root.getChildren().addAll(circles);
        primaryStage.setScene(scene);
        primaryStage.show();
        Timeline moveCircles = new Timeline();
        circles.forEach((Circle circle) -> {
            KeyValue moveX = new KeyValue(circle.centerXProperty(), Math.random() * 800);
            KeyValue moveY = new KeyValue(circle.centerYProperty(), Math.random() * 600);
            moveCircles.getKeyFrames().add(new KeyFrame(Duration.seconds(40), moveX, moveY));
        });
        moveCircles.play();
    }
}
