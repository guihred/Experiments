package fxsamples;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import utils.ResourceFXUtils;

public class AnimationExample extends Application {

    @Override
    public void start(Stage theStage) {
        theStage.setTitle("Animation Example");

        Group root = new Group();
        Scene theScene = new Scene(root);
        theStage.setScene(theScene);

        final int canvasSize = 512;
        Canvas canvas = new Canvas(canvasSize, canvasSize);
        root.getChildren().add(canvas);

        GraphicsContext gc = canvas.getGraphicsContext2D();
        Image earth = new Image(ResourceFXUtils.toExternalForm("earth.png"));
        Image sun = new Image(ResourceFXUtils.toExternalForm("sun.png"));
        Image space = new Image(ResourceFXUtils.toExternalForm("space.jpg"));

        final long startNanoTime = System.nanoTime();

        new AnimationTimer() {
            @Override
            public void handle(long currentNanoTime) {
                double t = (currentNanoTime - startNanoTime) / 1e9;

                final int sunPosition = canvasSize / 2;
                final int radius = sunPosition / 2;
                double x = sunPosition + radius * Math.cos(t);
                double y = sunPosition + radius * Math.sin(t);

                // background image clears canvas
                gc.drawImage(space, 0, 0);
                gc.drawImage(earth, x, y);
                gc.drawImage(sun, sunPosition, sunPosition);
            }
        }.start();

        theStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}