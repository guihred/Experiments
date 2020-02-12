package fxsamples;

import static utils.CommonsFX.onCloseWindow;
import static utils.RunnableEx.runIf;

import java.util.Random;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import org.slf4j.Logger;
import simplebuilder.SimpleCircleBuilder;
import utils.CommonsFX;
import utils.HasLogging;
import utils.ResourceFXUtils;
import utils.RotateUtils;

public class PlayingAudio extends Application {
    private static final Logger LOG = HasLogging.log();
    @FXML
    private Slider seekpositionslider;
    @FXML
    private Rectangle applicationArea;
    @FXML
    private Group playButton;
    @FXML
    private Group closeButton;
    @FXML
    private Group pauseButton;
    @FXML
    private ToggleButton toggleButton3;
    @FXML
    private Rectangle buttonArea;
    @FXML
    private Group buttonGroup;
    @FXML
    protected Group vizContainer;
    protected Stage mainStage;
    private Random rand = new Random();
    private MediaPlayer mediaPlayer;
    private Point2D anchorPt;
    private Point2D previousLocation;
    private ChangeListener<Duration> progressListener = (observable, oldValue, newValue) -> seekpositionslider
        .setValue(newValue.toSeconds());

    public void initialize() {
        mainStage.centerOnScreen();
        seekpositionslider.valueProperty().addListener(observable1 -> {
            if (seekpositionslider.isValueChanging() && mediaPlayer != null
                && mediaPlayer.getStatus() == MediaPlayer.Status.PAUSED) {
                double dur = seekpositionslider.getValue() * 1000;
                mediaPlayer.seek(Duration.millis(dur));
            }
        });
    }

    public void onActionToggleButton3() {
        if (mediaPlayer != null) {
            boolean selected = toggleButton3.isSelected();
            updatePlayAndPauseButtons(selected);
            if (selected && mediaPlayer.getStatus() == Status.PLAYING) {
                mediaPlayer.pause();
            } else if (!selected) {
                mediaPlayer.play();
            }
        }
    }

    public void onMouseClickedClosebutton() {
        mainStage.close();
        runIf(mediaPlayer, t -> {
            t.stop();
            t.dispose();
        });
    }

    public void onMousePressedStopbutton() {
        if (mediaPlayer != null) {
            updatePlayAndPauseButtons(true);
            if (mediaPlayer.getStatus() == Status.PLAYING) {
                mediaPlayer.stop();
            }
        }
    }

    public void playMedia(String url) {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
            mediaPlayer.setOnPaused(null);
            mediaPlayer.setOnPlaying(null);
            mediaPlayer.setOnReady(null);
            mediaPlayer.currentTimeProperty().removeListener(progressListener);
            mediaPlayer.setAudioSpectrumListener(null);
        }
        Media media = new Media(url);
        media.getMetadata().keySet().forEach(LOG::info);
        mediaPlayer = new MediaPlayer(media);
        mediaPlayer.currentTimeProperty().addListener(progressListener);
        mediaPlayer.setOnReady(() -> {
            updatePlayAndPauseButtons(false);
            seekpositionslider.setValue(0);
            seekpositionslider.setMax(mediaPlayer.getMedia().getDuration().toSeconds());
            mediaPlayer.play();
        });
        mediaPlayer.setOnEndOfMedia(() -> {
            updatePlayAndPauseButtons(true);
            mediaPlayer.stop();
        });
        onCloseWindow(mainStage, () -> {
            mediaPlayer.stop();
            mediaPlayer.dispose();
        });
        mediaPlayer
            .setAudioSpectrumListener(this::onAudioSpectrum);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        mainStage = primaryStage;
        CommonsFX.loadFXML("Playing Audio", "PlayingAudio.fxml", this, primaryStage, 500, 500);
        onCloseWindow(primaryStage, () -> runIf(mediaPlayer, t -> {
            t.stop();
            t.dispose();
        }));
        plugEventsToScene(mainStage.getScene());
    }

    /**
     * @param timestamp
     * @param duration
     * @param magnitudes
     * @param phases
     */
    protected void onAudioSpectrum(double timestamp, double duration, float[] magnitudes, float[] phases) {
        vizContainer.getChildren().clear();
        int i = 0;
        double y = mainStage.getScene().getHeight() / 2;
        for (float phase : phases) {
            int red = rnd();
            int green = rnd();
            int blue = rnd();
            Circle circle = new SimpleCircleBuilder().radius(10).centerX((double) 10 + i)
                .centerY(y + (double) phase * 100).fill(Color.rgb(red, green, blue, 7. / 10)).build();
            vizContainer.getChildren().add(circle);
            i += 5;
        }
    }

    protected int rnd() {
        final int bound = 256;
        return rand.nextInt(bound);
    }

    private void plugEventsToScene(Scene scene) {
        scene.setOnMousePressed(mouseEvent -> anchorPt = new Point2D(mouseEvent.getScreenX(), mouseEvent.getScreenY()));
        scene.setOnMouseDragged(mouseEvent -> {
            if (anchorPt != null && previousLocation != null) {
                mainStage.setX(previousLocation.getX() + mouseEvent.getScreenX() - anchorPt.getX());
                mainStage.setY(previousLocation.getY() + mouseEvent.getScreenY() - anchorPt.getY());
            }
        });
        scene.setOnMouseReleased(mouseEvent -> previousLocation = new Point2D(mainStage.getX(), mainStage.getY()));
        mainStage.addEventHandler(WindowEvent.WINDOW_SHOWN,
            t -> previousLocation = new Point2D(mainStage.getX(), mainStage.getY()));
        applicationArea.widthProperty().bind(scene.widthProperty());
        applicationArea.heightProperty().bind(scene.heightProperty());
        buttonGroup.translateXProperty().bind(scene.widthProperty().subtract(buttonArea.getWidth() + 6));
        buttonGroup.translateYProperty().bind(scene.heightProperty().subtract(buttonArea.getHeight() + 6));
        seekpositionslider.translateYProperty().bind(scene.heightProperty().subtract(50));
        RotateUtils.initSceneDragAndDrop(scene, this::playMedia);
        closeButton.translateXProperty().bind(scene.widthProperty().subtract(15));
        scene.setFill(Color.BLACK);
        scene.getStylesheets().add(ResourceFXUtils.toExternalForm("media.css"));
    }

    private void updatePlayAndPauseButtons(boolean playVisible) {
        toggleButton3.setGraphic(playVisible ? playButton : pauseButton);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
