/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fxpro.ch08;

import static utils.RunnableEx.runIf;

import java.io.File;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javafx.application.Application;
import javafx.collections.MapChangeListener.Change;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.effect.Reflection;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;
import javafx.stage.Stage;
import org.slf4j.Logger;
import utils.*;

public class SimpleAudioPlayerLauncher extends Application {
    private static final Logger LOGGER = HasLogging.log();
    private Label album;
    private ImageView albumCover;
    private Label artist;
    private Label title;
    private Label year;
    private MediaPlayer mediaPlayer;

    public SimpleAudioPlayerLauncher() {
        createControls();
    }
    public void createMedia() {
        if (mediaPlayer == null) {
            createMedia(SupplierEx.get(() -> getRandomSong().toUri().toURL().toExternalForm()));
        }
    }

    public void createMedia(String url) {
        Media media = new Media(url);
        mediaPlayer = new MediaPlayer(media);
        mediaPlayer
            .setOnError(() -> RunnableEx.runIf(media.getError(), err -> LOGGER.error("error:{}", err.getMessage())));
        media.getMetadata().addListener((Change<? extends String, ? extends Object> ch) -> {
            if (ch.wasAdded()) {
                handleMetadata(ch.getKey(), ch.getValueAdded());
            }
        });
        mediaPlayer.setAutoPlay(true);
    }

    @Override
    public void start(Stage primaryStage) {
        createControls();
        createMedia();
        final Scene scene = new Scene(createGridPane());
        scene.getStylesheets().add(Chapter8Resource.MEDIA.getURL().toString());
        primaryStage.setScene(scene);
        primaryStage.setTitle("Simple Audio Player");
        primaryStage.show();
        primaryStage.setOnCloseRequest(e -> stopAndDispose());
        primaryStage.showingProperty().addListener(e -> stopAndDispose());
    }

    private void createControls() {
        artist = new Label();
        artist.setId("artist");
        album = new Label();
        album.setId("album");
        title = new Label();
        title.setId("title");
        year = new Label();
        year.setId("year");
        final Reflection reflection = new Reflection();
        reflection.setFraction(2. / 10);
        final Image image = new Image(MetadataView.DEFAULT_PICTURE);
        albumCover = new ImageView(image);
        final int fitWidth = 240;
        albumCover.setFitWidth(fitWidth);
        albumCover.setPreserveRatio(true);
        albumCover.setSmooth(true);
        albumCover.setEffect(reflection);
    }

    private GridPane createGridPane() {
        final GridPane gp = new GridPane();
        gp.setPadding(new Insets(10));
        gp.setHgap(20);
        gp.add(albumCover, 0, 0, 1, GridPane.REMAINING);
        gp.add(title, 1, 0);
        gp.add(artist, 1, 1);
        gp.add(album, 1, 2);
        gp.add(year, 1, 3);
        final ColumnConstraints c0 = new ColumnConstraints();
        final ColumnConstraints c1 = new ColumnConstraints();
        c1.setHgrow(Priority.ALWAYS);
        gp.getColumnConstraints().addAll(c0, c1);
        final RowConstraints r0 = new RowConstraints();
        r0.setValignment(VPos.TOP);
        gp.getRowConstraints().addAll(r0, r0, r0, r0);
        return gp;
    }

    private void handleMetadata(String key, Object value) {
        LOGGER.info("Key={},Value={}", key, value);
        String valueString = Objects.toString(value);
        if ("album".equals(key)) {
            album.setText(valueString);
        } else if ("artist".equals(key)) {
            artist.setText(valueString);
        }
        if ("title".equals(key)) {
            title.setText(valueString);
        }
        if ("year".equals(key)) {
            year.setText(valueString);
        }
        if ("image".equals(key) && value instanceof Image) {
            albumCover.setImage(ImageFXUtils.imageCopy((Image) value));
        }
    }

    private void stopAndDispose() {
        runIf(mediaPlayer, t -> {
            if (t.getStatus() == Status.PLAYING) {
                t.stop();
            }
            t.dispose();
        });
    }

    public static void main(String[] args) {
        launch(args);
    }

    private static Path getRandomSong() {
        File outFile = ResourceFXUtils.getUserFolder("Music");
        List<Path> pathByExtension = ResourceFXUtils.getPathByExtension(outFile, "mp3");
        Collections.shuffle(pathByExtension);
        return pathByExtension.remove(0);
    }
}
