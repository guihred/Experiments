package fxproexercises.ch02;
import static others.CommonsFX.newButton;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
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
import others.SimpleHBoxBuilder;
import others.SimpleLineBuilder;

public class FxProCH2c extends Application {

	DoubleProperty startXVal = new SimpleDoubleProperty(100.0);
	Button startButton;
	Button pauseButton;
	Button resumeButton;
	Button stopButton;
	Line line;
	Timeline anim = new Timeline(new KeyFrame(new Duration(0.0), new KeyValue(startXVal, 100.0)),
			new KeyFrame(new Duration(1000.0), new KeyValue(startXVal, 300.0, Interpolator.LINEAR)));
	{
		anim.setAutoReverse(true);
		anim.setCycleCount(Animation.INDEFINITE);
	}

	public static void main(String[] args) {
		Application.launch(args);
	}

	@Override
	public void start(Stage stage) {
		line = new SimpleLineBuilder()
				.startX(0)
				.startY(50)
				.endX(200)
				.endY(400)
				.stroke(Color.BLUE)
				.strokeWidth(4)
				.build();
		stopButton = newButton("Stop", (e) -> {
			anim.stop();
		});
		resumeButton = newButton("Resume", (e) -> {
			anim.play();
		});
		pauseButton = newButton("Pause", (e) -> {
			anim.pause();
		});
		startButton = newButton("Start", (e) -> {
			anim.playFromStart();
		});
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
