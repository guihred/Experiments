package fxpro.ch02;


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
import javafx.scene.shape.Path;
import javafx.stage.Stage;
import javafx.util.Duration;
import simplebuilder.*;

public class PathTransitionExample extends Application {

    private final Ellipse ellipse =
            new SimpleEllipseBuilder().centerX(100).centerY(50).radiusX(4).radiusY(50)
			.fill(Color.BLUE).build();

    private final Path path = new SimplePathBuilder()
            .moveTo(100, 50)
            .arcTo(350, 350, 0, 300, 50, false, true)
            .build();

    private PathTransition anim = new SimplePathTransitionBuilder()
            .orientation(PathTransition.OrientationType.ORTHOGONAL_TO_TANGENT)
            .interpolator(Interpolator.LINEAR)
            .autoReverse(true)
            .cycleCount(Animation.INDEFINITE)
            .node(ellipse)
            .path(path)
            .duration(new Duration(1000.0))
            .build();

    @Override
    public void start(Stage stage) {
        Button stopButton = SimpleButtonBuilder.newButton("Stop", e -> anim.stop());
        Button resumeButton = SimpleButtonBuilder.newButton("Resume", e -> anim.play());
        Button pauseButton = SimpleButtonBuilder.newButton("Pause", e -> anim.pause());
        Button startButton = SimpleButtonBuilder.newButton("Start", e -> anim.playFromStart());
        final HBox hbox = new SimpleHBoxBuilder().layoutX(60).layoutY(420).spacing(10)
                .children(startButton, pauseButton, resumeButton, stopButton).build();
        final Scene scene = new Scene(new Group(ellipse, hbox), 400, 500);

        startButton.disableProperty().bind(anim.statusProperty().isNotEqualTo(Animation.Status.STOPPED));
        pauseButton.disableProperty().bind(anim.statusProperty().isNotEqualTo(Animation.Status.RUNNING));
        resumeButton.disableProperty().bind(anim.statusProperty().isNotEqualTo(Animation.Status.PAUSED));
        stopButton.disableProperty().bind(anim.statusProperty().isEqualTo(Animation.Status.STOPPED));
        stage.setScene(scene);
        stage.setTitle("Metronome using PathTransition");
        stage.show();
    }

    public static void main(String[] args) {
        Application.launch(args);
    }
}
