package fxproexercises.ch02;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.animation.TimelineBuilder;
import javafx.application.Application;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Group;
import javafx.scene.GroupBuilder;
import javafx.scene.Scene;
import javafx.scene.SceneBuilder;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBuilder;
import javafx.scene.layout.HBoxBuilder;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.LineBuilder;
import javafx.stage.Stage;
import javafx.util.Duration;

public class FxProCH2c extends Application {

    DoubleProperty startXVal = new SimpleDoubleProperty(100.0);
    Button startButton;
    Button pauseButton;
    Button resumeButton;
    Button stopButton;
    Line line;
    Timeline anim = TimelineBuilder.create()
            .autoReverse(true)
            .keyFrames(
                    new KeyFrame(
                            new Duration(0.0),
                            new KeyValue(startXVal, 100.0)
                    ),
                    new KeyFrame(
                            new Duration(1000.0),
                            new KeyValue(startXVal, 300.0, Interpolator.LINEAR)
                    )
            )
            .cycleCount(Timeline.INDEFINITE)
            .build();

    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage stage) {
        line = LineBuilder.create()
                .startY(50)
                .endX(200)
                .endY(400)
                .strokeWidth(4)
                .stroke(Color.BLUE)
                .build();
        stopButton = ButtonBuilder.create()
                .text("Stop")
                .onAction((e) -> {
                    anim.stop();
                })
                .build();
        resumeButton = ButtonBuilder.create()
                .text("Resume")
                .onAction((e) -> {
                    anim.play();
                })
                .build();
        pauseButton = ButtonBuilder.create()
                .text("Pause")
                .onAction((e) -> {
                    anim.pause();
                })
                .build();
        startButton = ButtonBuilder.create()
                .text("Start")
                .onAction((e) -> {
                    anim.playFromStart();
                })
                .build();
        final Group root = GroupBuilder.create()
                .children(
                        line,
                        HBoxBuilder.create()
                        .layoutX(60).layoutY(420)
                        .spacing(10)
                        .children(startButton, pauseButton, resumeButton, stopButton).build()
                )
                .build();
        Scene scene = SceneBuilder.create()
                .width(400)
                .height(500)
                .root(root)
                .build();
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
