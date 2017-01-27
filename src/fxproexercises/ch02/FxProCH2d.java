package fxproexercises.ch02;

import static others.CommonsFX.newArcTo;
import static others.CommonsFX.newButton;
import static others.CommonsFX.newPathTransistion;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.PathTransition;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.stage.Stage;
import javafx.util.Duration;
import others.SimpleEllipseBuilder;
import others.SimpleHBoxBuilder;

public class FxProCH2d extends Application {

    Button startButton;
    Button pauseButton;
    Button resumeButton;
    Button stopButton;
	Ellipse ellipse = new SimpleEllipseBuilder().centerX(100).centerY(50).radiusX(4).radiusY(50).fill(Color.BLUE)
	.build();

	Path path = new Path(new MoveTo(100, 50), newArcTo(300, 50, 350, 350, true));

	PathTransition anim = newPathTransistion(new Duration(1000.0), path, ellipse,
			PathTransition.OrientationType.ORTHOGONAL_TO_TANGENT, Interpolator.LINEAR, true, Animation.INDEFINITE);

    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage stage) {
		stopButton = newButton("Stop", (e) -> anim.stop());
		resumeButton = newButton("Resume", (e) -> anim.play());
		pauseButton = newButton("Pause", (e) -> anim.pause());
		startButton = newButton("Start", (e) -> anim.playFromStart());
		Node[] children = { startButton, pauseButton, resumeButton, stopButton };
		HBox hbox = new SimpleHBoxBuilder().layoutX(60).layoutY(420).spacing(10).children(children).build();
		Scene scene = new Scene(new Group(ellipse, hbox), 400, 500);

        startButton.disableProperty().bind(anim.statusProperty().isNotEqualTo(Animation.Status.STOPPED));
        pauseButton.disableProperty().bind(anim.statusProperty().isNotEqualTo(Animation.Status.RUNNING));
        resumeButton.disableProperty().bind(anim.statusProperty().isNotEqualTo(Animation.Status.PAUSED));
        stopButton.disableProperty().bind(anim.statusProperty().isEqualTo(Animation.Status.STOPPED));
        stage.setScene(scene);
        stage.setTitle("Metronome using PathTransition");
        stage.show();
    }
}
