/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fxproexercises.ch08;

import java.io.File;
import java.net.URL;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;

/**
 *
 * @author Note
 */
public class FxProCH8a extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            final File resource = new File("C:\\Users\\Note\\Documents\\Sistemas\\Workspace\\Teste\\TeenTitans.mp3");
            Media sound = new Media(resource.toURI().toString());
            System.out.println(resource.toURI().toString());
            MediaPlayer mediaPlayer = new MediaPlayer(sound);
            final Button button = new Button("Bing Zzzzt!");
            button.setOnAction(event -> mediaPlayer.play());
            final StackPane stackPane = new StackPane();
            stackPane.setPadding(new Insets(10));
            stackPane.getChildren().add(button);
            final Scene scene = new Scene(stackPane, 200, 200);
            final URL stylesheet = new URL("file:C:\\Users\\Note\\Documents\\Sistemas\\Workspace\\Teste\\media.css");
            scene.getStylesheets().add(stylesheet.toString());
            primaryStage.setTitle("Basic AudioClip Example");
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
