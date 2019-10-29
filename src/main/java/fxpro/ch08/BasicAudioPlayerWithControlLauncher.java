/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fxpro.ch08;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import utils.CommonsFX;
import utils.CrawlerTask;

/**
 *
 * @author Note
 */
public class BasicAudioPlayerWithControlLauncher extends Application {

    private final SongModel songModel = new SongModel();

    @Override
    public void start(Stage primaryStage) {
		songModel.setURL(Chapter8Resource.TEEN_TITANS.getURL().toString());
        MetadataView metaDataView = new MetadataView(songModel);
        PlayerControlView playerControlsView = new PlayerControlView(songModel);
        final BorderPane root = new BorderPane();
        root.setCenter(metaDataView.getViewNode());
        root.setBottom(playerControlsView.getViewNode());
        final Scene scene = new Scene(root, 800, 400);
        CommonsFX.initSceneDragAndDrop(scene, url -> {
            songModel.setURL(url);
            songModel.getMediaPlayer().play();
        });

		scene.getStylesheets().add(Chapter8Resource.MEDIA.getURL().toString());
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(e -> songModel.getMediaPlayer().dispose());
        primaryStage.setTitle("Basic Audio Player With Control");
        primaryStage.show();
        songModel.getPlayer().play();
    }

    public static void main(String[] args) {
        CrawlerTask.insertProxyConfig();
        launch(args);
	}
}
