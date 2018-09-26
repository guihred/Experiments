/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fxpro.ch08;

import java.io.File;
import java.net.MalformedURLException;
import javafx.application.Application;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import utils.ResourceFXUtils;

public class MediaPlayerExample extends Application {

    @Override
    public void start(Stage primaryStage) throws MalformedURLException {
        File resource = ResourceFXUtils.toFile("TeenTitans.mp3");
        Media media = new Media(resource.toURI().toString());
        MediaPlayer mediaPlayer = new MediaPlayer(media);
        mediaPlayer.setVolume(0);
        mediaPlayer.play();
        primaryStage.setTitle("Media Player");
        primaryStage.setWidth(200);
        primaryStage.setHeight(200);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
