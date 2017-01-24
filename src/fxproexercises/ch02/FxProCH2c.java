package fxproexercises.ch02;
import static others.CommonsFX.newButton;
import static others.CommonsFX.newLine;

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
		anim.setCycleCount(Timeline.INDEFINITE);
	}

	public static void main(String[] args) {
		Application.launch(args);
	}

	@Override
	public void start(Stage stage) {
		int startY = 50;
		int endX = 200;
		int endY = 400;
		int strokeWidth = 4;
		Color color = Color.BLUE;
		line = newLine(0, startY, endX, endY, strokeWidth, color);
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
		HBox hbox = new HBox(startButton, pauseButton, resumeButton, stopButton);
		hbox.setLayoutX(60);
		hbox.setLayoutY(420);
		hbox.setSpacing(10);
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
