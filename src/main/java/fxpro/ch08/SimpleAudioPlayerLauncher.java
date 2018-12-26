/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fxpro.ch08;

import java.net.URL;
import javafx.application.Application;
import javafx.collections.MapChangeListener;
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
import javafx.stage.Stage;
import org.slf4j.Logger;
import utils.HasLogging;

public class SimpleAudioPlayerLauncher extends Application {
	private static final Logger LOGGER = HasLogging.log();
    private Label album;
    private ImageView albumCover;
    private Label artist;
    private Label title;
    private Label year;
    public void createMedia()  {
        Media media = new Media(Chapter8Resource.TEEN_TITANS.getURL().toString());
        MediaPlayer mediaPlayer = new MediaPlayer(media);
        mediaPlayer.setOnError(() -> {
            final String errorMessage = media.getError().getMessage();
            LOGGER.error("error:{}", errorMessage);
        });
        mediaPlayer.setVolume(0);
		media.getMetadata().addListener((MapChangeListener<String, Object>) ch -> {
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
        try {

			final URL stylesheet = Chapter8Resource.MEDIA.getURL();
            scene.getStylesheets().add(stylesheet.toString());
        } catch (Exception e) {
			LOGGER.error("", e);
        }
        primaryStage.setScene(scene);
        primaryStage.setTitle("Simple Audio Player");
        primaryStage.show();
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
        reflection.setFraction(0.2);
		final Image image = new Image(MetadataView.DEFAULT_PICTURE);
        albumCover = new ImageView(image);
        albumCover.setFitWidth(240);
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
		if ("album".equals(key)) {
            album.setText(value.toString());
		} else if ("artist".equals(key)) {
            artist.setText(value.toString());
        }
		if ("title".equals(key)) {
            title.setText(value.toString());
        }
		if ("year".equals(key)) {
            year.setText(value.toString());
        }
		if ("image".equals(key)) {
            albumCover.setImage((Image) value);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
