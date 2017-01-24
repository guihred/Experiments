/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fxproexercises.ch08;

import java.io.File;
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

public class FxProCH8c extends Application {

    public static void main(String[] args) {
        launch(args);
    }
    private Media media;
    private MediaPlayer mediaPlayer;
    private Label artist;
    private Label album;
    private Label title;
    private Label year;
    private ImageView albumCover;
    @Override
    public void start(Stage primaryStage) {
        createControls();
        createMedia();
        final Scene scene = new Scene(createGridPane(), 800, 400);
        try {

            final URL stylesheet = new URL("file:C:\\Users\\Note\\Documents\\Sistemas\\Workspace\\Teste\\media.css");
            scene.getStylesheets().add(stylesheet.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        primaryStage.setScene(scene);
        primaryStage.setTitle("Audio Player 2");
        primaryStage.show();
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
        final Image image = new Image(new File("C:\\Users\\Note\\Pictures\\fb.jpg").toURI().toString());
        albumCover = new ImageView(image);
        albumCover.setFitWidth(240);
        albumCover.setPreserveRatio(true);
        albumCover.setSmooth(true);
        albumCover.setEffect(reflection);
    }

    public void createMedia()  {
        File resource = new File("C:\\Users\\Note\\Documents\\Sistemas\\Workspace\\Teste\\TeenTitans.mp3");
        media = new Media(resource.toURI().toString());
         mediaPlayer = new MediaPlayer(media);
        mediaPlayer.setOnError(() -> {
            final String errorMessage = media.getError().getMessage();
            System.err.println("error:" + errorMessage);
        });
        media.getMetadata().addListener((MapChangeListener<String, Object>) (ch) -> {
                if (ch.wasAdded()) {
                    handleMetadata(ch.getKey(), ch.getValueAdded());
                }
        });
        mediaPlayer.setAutoPlay(true);
    }

    private void handleMetadata(String key, Object value) {
        if (key.equals("album")) {
            album.setText(value.toString());
        } else if (key.equals("artist")) {
            artist.setText(value.toString());
        }
        if (key.equals("title")) {
            title.setText(value.toString());
        }
        if (key.equals("year")) {
            year.setText(value.toString());
        }
        if (key.equals("image")) {
            albumCover.setImage((Image) value);
        }
    }
}
