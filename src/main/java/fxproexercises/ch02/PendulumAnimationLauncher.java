package fxproexercises.ch02;
import static simplebuilder.CommonsFX.newButton;

import javafx.animation.*;
import javafx.application.Application;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.stage.Stage;
import javafx.util.Duration;
import simplebuilder.SimpleHBoxBuilder;
import simplebuilder.SimpleLineBuilder;
import simplebuilder.SimpleTimelineBuilder;

public class PendulumAnimationLauncher extends Application {

	private DoubleProperty startXVal = new SimpleDoubleProperty(100.0);
	private Timeline anim = new SimpleTimelineBuilder()
			.autoReverse(true)
			.cycleCount(Animation.INDEFINITE)
			.keyFrames(
					new KeyFrame(Duration.ZERO, new KeyValue(startXVal, 100.0)),
					new KeyFrame(new Duration(1000.0), new KeyValue(startXVal, 300.0, Interpolator.LINEAR)))
			.build();

	public static void main(String[] args) {
		Application.launch(args);
	}

	@Override
	public void start(Stage stage) {
        Line line = new SimpleLineBuilder()
				.startX(0)
				.startY(50)
				.endX(200)
				.endY(400)
				.stroke(Color.BLUE)
				.strokeWidth(4)
				.build();
        Button stopButton = newButton("Stop", e -> anim.stop());
        Button resumeButton = newButton("Resume", e -> anim.play());
        Button pauseButton = newButton("Pause", e -> anim.pause());
        Button startButton = newButton("Start", e -> anim.playFromStart());
		HBox hbox = new SimpleHBoxBuilder()
				.children(startButton, pauseButton, resumeButton, stopButton)
				.layoutX(60)
				.layoutY(420)
				.spacing(10)
				.build();
		final Group root = new Group(line, hbox);
		Scene scene = new Scene(root, 400, 500);
		line.startXProperty().bind(startXVal);
		startButton.disableProperty().bind(anim.statusProperty().isNotEqualTo(Animation.Status.STOPPED));
		pauseButton.disableProperty().bind(anim.statusProperty().isNotEqualTo(Animation.Status.RUNNING));
		resumeButton.disableProperty().bind(anim.statusProperty().isNotEqualTo(Animation.Status.PAUSED));
		stopButton.disableProperty().bind(anim.statusProperty().isEqualTo(Animation.Status.STOPPED));
		stage.setScene(scene);
		stage.setTitle("Metronome 1");
		stage.show();
	}
}
