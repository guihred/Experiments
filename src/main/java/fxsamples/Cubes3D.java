package fxsamples;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * @author Jasper Potts
 */
public class Cubes3D extends Application {

    @Override
    public void start(Stage stage) {
        stage.setTitle("Cube 3D");

        final Cube c = new Cube(50, Color.RED, 1);
        final int initialAngle = 45;
        c.rx.setAngle(initialAngle);
        c.ry.setAngle(initialAngle);
        final Cube c2 = new Cube(50, Color.GREEN, 1);
        c2.setTranslateX(100);
        c2.rx.setAngle(initialAngle);
        c2.ry.setAngle(initialAngle);
        final Cube c3 = new Cube(50, Color.ORANGE, 1);
        c3.setTranslateX(-100);
        c3.rx.setAngle(initialAngle);
        c3.ry.setAngle(initialAngle);
        Timeline animation = new Timeline();
        final double fullCircle = 360.;
        animation.getKeyFrames()
                .addAll(new KeyFrame(Duration.ZERO, new KeyValue(c.ry.angleProperty(), 0.),
                        new KeyValue(c2.rx.angleProperty(), 0.), new KeyValue(c3.rz.angleProperty(), 0.)),
                        new KeyFrame(Duration.millis(1000), new KeyValue(c.ry.angleProperty(), fullCircle),
                                new KeyValue(c2.rx.angleProperty(), fullCircle),
                                new KeyValue(c3.rz.angleProperty(), fullCircle)));
        animation.setCycleCount(Animation.INDEFINITE);
        // create root group
        Group root = new Group(c, c2, c3);
        // translate and rotate group so that origin is center and +Y is up
        final double height = 150;
        final double width = 400;
        root.setTranslateX(width / 2);
        root.setTranslateY(height / 2);
        root.getTransforms().add(new Rotate(fullCircle / 2, Rotate.X_AXIS));
        // create scene
        Scene scene = new Scene(root, width, height);
        scene.setCamera(new PerspectiveCamera());
        stage.setScene(scene);
        stage.show();
        // start spining animation
        animation.play();
    }

    public static void main(String[] args) {
        launch(args);
    }
}