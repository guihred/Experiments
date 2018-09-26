/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fxpro.ch08;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Note
 */
public class BasicAudioClipExample extends Application {
	private static final Logger LOGGER = LoggerFactory.getLogger(BasicAudioClipExample.class);
    @Override
    public void start(Stage primaryStage) {
        try {
			Media sound = new Media(Chapter8Resource.TEEN_TITANS.getURL().toString());
            MediaPlayer mediaPlayer = new MediaPlayer(sound);
            final Button button = new Button("Bing Zzzzt!");
            button.setOnAction(event -> mediaPlayer.play());
            final StackPane stackPane = new StackPane();
            stackPane.setPadding(new Insets(10));
            stackPane.getChildren().add(button);
            final Scene scene = new Scene(stackPane, 200, 200);
			scene.getStylesheets().add(Chapter8Resource.MEDIA.getURL().toString());
            primaryStage.setTitle("Basic AudioClip Example");
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (Exception e) {
			LOGGER.error("ERROR FxProCH8a", e);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
