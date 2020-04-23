package fxpro.ch02;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.PathTransition;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.stage.Stage;
import javafx.util.Duration;
import simplebuilder.*;

public class RandomPathExample extends Application {

    private Ellipse ellipse = new SimpleEllipseBuilder().centerX(100).centerY(50).radiusX(4).radiusY(4).fill(Color.BLUE)
        .build();

    private PathTransition anim = new SimplePathTransitionBuilder()
        .orientation(PathTransition.OrientationType.ORTHOGONAL_TO_TANGENT).interpolator(Interpolator.LINEAR)
        .autoReverse(false).cycleCount(Animation.INDEFINITE).node(ellipse).duration(new Duration(1000.0)).build();

    private Random random = new Random();

    @Override
    public void start(Stage stage) {
        Button stopButton = SimpleButtonBuilder.newButton("Stop", e -> anim.stop());
        Button resumeButton = SimpleButtonBuilder.newButton("Resume", e -> anim.play());
        Button pauseButton = SimpleButtonBuilder.newButton("Pause", e -> anim.pause());
        Button startButton = SimpleButtonBuilder.newButton("Start", e -> anim.playFromStart());
        SimplePathBuilder moveTo = new SimplePathBuilder().moveTo(200, 200);
        HBox hbox = new SimpleHBoxBuilder().layoutX(60).layoutY(420).spacing(10)
            .children(startButton, pauseButton, resumeButton, stopButton).build();
        Scene scene = new Scene(new Group(ellipse, moveTo.build(), hbox), 400, 500);
        List<Runnable> run = Arrays.asList(
            () -> moveTo.arcTo(d(), d(), d(), d(), d(), random.nextBoolean(), random.nextBoolean()),
            () -> moveTo.cubicCurveTo(d(), d(), d(), d(), d(), d()), () -> moveTo.quadCurveTo(d(), d(), d(), d()),
            () -> moveTo.hLineTo(d()), () -> moveTo.vLineTo(d()), () -> moveTo.lineTo(d(), d()));
        Collections.shuffle(run);
        run.forEach(Runnable::run);
        moveTo.closePath();
        anim.setPath(moveTo.build());
        startButton.disableProperty().bind(anim.statusProperty().isNotEqualTo(Animation.Status.STOPPED));
        pauseButton.disableProperty().bind(anim.statusProperty().isNotEqualTo(Animation.Status.RUNNING));
        resumeButton.disableProperty().bind(anim.statusProperty().isNotEqualTo(Animation.Status.PAUSED));
        stopButton.disableProperty().bind(anim.statusProperty().isEqualTo(Animation.Status.STOPPED));
        stage.setScene(scene);
        stage.setTitle("Random Path Movement");
        stage.show();
    }

    private double d() {
        return random.nextDouble() * 200;
    }

    public static void main(String[] args) {
        Application.launch(args);
    }
}
