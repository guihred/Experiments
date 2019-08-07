/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fxpro.ch08;

import static utils.SongUtils.seekAndUpdatePosition;
import static utils.SongUtils.updatePositionSlider;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;
import javafx.util.Duration;
import simplebuilder.SimpleSliderBuilder;
import utils.SongUtils;
import utils.StageHelper;

public class PlayerControlView extends BaseSongView {

    private Label statusLabel;
    private Label currentTimeLabel;
    private Slider positionSlider;
    private Image pauseImg;
    private Image playImg;
    private ImageView playPauseIcon;
    private Node controlPanel;

    public PlayerControlView(SongModel songModel) {
        super(songModel);
    }

    @Override
    protected Node initView() {
        final Button openButton = createOpenButton();
        controlPanel = createControlPanel();
        Slider volumeSlider = new SimpleSliderBuilder(0.0, 1.0, 1. / 10).id("volumeSlider").value(0).build();
        statusLabel = createLabel("Buffering", "statusDisplay");
        positionSlider = new SimpleSliderBuilder(0.0, 1.0, 1. / 10).id("positionSlider").value(0).build();
        volumeSlider.valueChangingProperty()
            .addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
                if (oldValue && !newValue) {
                    double pos = volumeSlider.getValue();
                    final MediaPlayer mediaPlayer = songModel.getMediaPlayer();
                    mediaPlayer.setVolume(pos);
                }
            });
        positionSlider.valueChangingProperty()
            .addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
                if (oldValue && !newValue) {
                    double pos = positionSlider.getValue();
                    final MediaPlayer mediaPlayer = songModel.getMediaPlayer();
                    final Duration seekTo = mediaPlayer.getTotalDuration().multiply(pos);
                    seekAndUpdatePosition(seekTo, positionSlider, mediaPlayer);
                }
            });
        songModel.getMediaPlayer().setOnEndOfMedia(songModel.getMediaPlayer()::stop);
        songModel.getMediaPlayer().statusProperty().addListener(new StatusListener());
        songModel.getMediaPlayer().currentTimeProperty().addListener(new CurrentTimeListener());
        songModel.mediaPlayerProperty().addListener((observable, oldValue, newValue) -> {
            newValue.setOnEndOfMedia(songModel.getMediaPlayer()::stop);
            newValue.statusProperty().addListener(new StatusListener());
            newValue.currentTimeProperty().addListener(new CurrentTimeListener());
        });

        Label totalDurationLabel = createLabel("00:00", "mediaText");
        currentTimeLabel = createLabel("00:00", "mediaText");
        final ImageView volLow = new ImageView();
        volLow.setId("volumeLow");
        final ImageView volHigh = new ImageView();
        volHigh.setId("volumeHigh");
        final GridPane gp = new GridPane();
        gp.setHgap(1);
        gp.setVgap(1);
        gp.setPadding(new Insets(10));
        final ColumnConstraints buttonCol = new ColumnConstraints(100);
        final ColumnConstraints spacerCol = new ColumnConstraints(40, 80, 80);
        final ColumnConstraints middleCol = new ColumnConstraints();
        middleCol.setHgrow(Priority.ALWAYS);
        gp.getColumnConstraints().addAll(buttonCol, spacerCol, middleCol, spacerCol, buttonCol);
        GridPane.setValignment(openButton, VPos.BOTTOM);
        GridPane.setHalignment(volHigh, HPos.RIGHT);
        GridPane.setValignment(volumeSlider, VPos.TOP);
        GridPane.setHalignment(statusLabel, HPos.RIGHT);
        GridPane.setValignment(statusLabel, VPos.TOP);
        GridPane.setHalignment(currentTimeLabel, HPos.RIGHT);
        gp.add(openButton, 0, 0, 1, 3);
        gp.add(volLow, 1, 0);
        gp.add(volHigh, 1, 0);
        gp.add(volumeSlider, 1, 1);
        gp.add(controlPanel, 2, 0, 1, 2);
        gp.add(statusLabel, 3, 1);
        gp.add(currentTimeLabel, 1, 2);
        gp.add(positionSlider, 2, 2);
        gp.add(totalDurationLabel, 3, 2);
        return gp;
    }

    private Node createControlPanel() {
        final HBox hbox = new HBox();
        hbox.setAlignment(Pos.CENTER);
        hbox.setFillHeight(false);
        final Button playPauseButton = createPlayPauseButton();
        final Button seekStartButton = new Button();
        seekStartButton.setId("seekStartButton");
        seekStartButton.setOnAction(
            (ActionEvent event) -> seekAndUpdatePosition(Duration.ZERO, positionSlider, songModel.getMediaPlayer()));
        final Button seekEndButton = new Button();
        seekEndButton.setId("seekEndButton");
        seekEndButton.setOnAction((ActionEvent event) -> {
            final MediaPlayer mediaPlayer = songModel.getMediaPlayer();
            final Duration totalDuration = mediaPlayer.getTotalDuration();
            final Duration oneSecond = Duration.seconds(1);
            seekAndUpdatePosition(totalDuration.subtract(oneSecond), positionSlider, mediaPlayer);
        });
        hbox.getChildren().addAll(seekStartButton, playPauseButton, seekEndButton);
        return hbox;
    }

    private Label createLabel(String text, String styleClass) {
        final Label label = new Label(text);
        label.getStyleClass().add(styleClass);
        return label;
    }

    private Button createOpenButton() {

        Button openButton = StageHelper.chooseFile(null, "Pick a Sound File", song -> {
            songModel.setURL(song.toURI().toString());
            songModel.getMediaPlayer().play();
        });
        openButton.setId("openButton");
        final int prefSize = 32;
        openButton.setPrefWidth(prefSize);
        openButton.setPrefHeight(prefSize);
        return openButton;
    }

    private Button createPlayPauseButton() {
        pauseImg = new Image(
            "https://cdn1.iconfinder.com/data/icons/material-audio-video/20/pause-circle-outline-128.png");
        playImg = new Image("https://cdn3.iconfinder.com/data/icons/google-material-design-icons/"
            + "48/ic_play_circle_outline_48px-128.png");
        playPauseIcon = new ImageView(playImg);
        playPauseIcon.setScaleX(0.5);
        playPauseIcon.setScaleY(0.5);
        final Button playPauseButton = new Button(null, playPauseIcon);
        playPauseButton.setId("playPauseButton");

        playPauseButton.setOnAction((ActionEvent arg0) -> {
            final MediaPlayer mediaPlayer = songModel.getMediaPlayer();
            if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                mediaPlayer.pause();
            } else {
                mediaPlayer.play();
            }
        });
        return playPauseButton;
    }

    private void updateStatus(Status newStatus) {

        if (newStatus == Status.UNKNOWN || newStatus == null) {
            controlPanel.setDisable(true);
            positionSlider.setDisable(true);
            statusLabel.setText("Buffering");
        } else {
            controlPanel.setDisable(false);
            positionSlider.setDisable(false);
            statusLabel.setText(newStatus.toString());
            if (newStatus == Status.PLAYING) {
                playPauseIcon.setImage(pauseImg);
            } else {
                playPauseIcon.setImage(playImg);
            }
        }
    }

    private class CurrentTimeListener implements InvalidationListener {

        @Override
        public void invalidated(Observable observable) {
            Platform.runLater(() -> {
                final MediaPlayer mediaPlayer = songModel.getMediaPlayer();
                final Duration currentTime = mediaPlayer.getCurrentTime();
                currentTimeLabel.setText(SongUtils.formatDuration(currentTime));
                updatePositionSlider(currentTime, positionSlider, mediaPlayer);
            });
        }
    }

    private class StatusListener implements InvalidationListener {
        @Override
        public void invalidated(Observable observable) {
            Platform.runLater(() -> updateStatus(songModel.getMediaPlayer().getStatus()));
        }
    }

}
