/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fxsamples;

import java.util.ArrayList;
import java.util.List;
import javafx.animation.Animation;
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
import simplebuilder.SimpleCircleBuilder;

public class VanishingCirclesApp extends Application {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;

    private static final int CIRCLE_DURATION_SECONDS = 40;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Vanishing Circles");
        Group root = new Group();
        Scene scene = new Scene(root, WIDTH, HEIGHT, Color.BLACK);
        List<Circle> circles = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            final Circle circle = new SimpleCircleBuilder().radius(150).centerX(rnd(WIDTH)).centerY(rnd(HEIGHT))
                .fill(new Color(rnd(1), rnd(1), rnd(1), .2)).effect(new BoxBlur(10, 10, 3)).stroke(Color.WHITE).build();
            circle.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent t) -> {
                KeyValue collapse = new KeyValue(circle.radiusProperty(), 0);
                new Timeline(new KeyFrame(Duration.seconds(3), collapse)).play();
            });
            circle.strokeWidthProperty().bind(Bindings.when(circle.hoverProperty()).then(4).otherwise(0));
            circles.add(circle);
        }
        root.getChildren().addAll(circles);
        primaryStage.setScene(scene);
        primaryStage.show();
        Timeline moveCircles = new Timeline();
        for (Circle circle : circles) {
            KeyValue moveX = new KeyValue(circle.centerXProperty(), rnd(WIDTH));
            KeyValue moveY = new KeyValue(circle.centerYProperty(), rnd(HEIGHT));
            moveCircles.getKeyFrames().add(new KeyFrame(Duration.seconds(CIRCLE_DURATION_SECONDS), moveX, moveY));
        }
        moveCircles.setCycleCount(Animation.INDEFINITE);
        moveCircles.play();
    }

    public static void main(String[] args) {
        Application.launch(args);
    }

    private static double rnd(int width2) {
        return Math.random() * width2;
    }
}
