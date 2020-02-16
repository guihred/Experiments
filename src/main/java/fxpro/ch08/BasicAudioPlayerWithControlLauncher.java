/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fxpro.ch08;

import static utils.CommonsFX.onCloseWindow;

import extract.SongUtils;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import utils.CrawlerTask;
import utils.RotateUtils;

/**
 *
 * @author Note
 */
public class BasicAudioPlayerWithControlLauncher extends Application {

    private final SongModel songModel = new SongModel();

    public void playUrl(String url) {
        songModel.setURL(url);
        songModel.getMediaPlayer().play();
    }

    @Override
    public void start(Stage primaryStage) {
        CrawlerTask.insertProxyConfig();
        songModel.setURL(Chapter8Resource.TEEN_TITANS.getURL().toString());
        MetadataView metaDataView = new MetadataView(songModel);
        PlayerControlView playerControlsView = new PlayerControlView(songModel);
        final BorderPane root = new BorderPane();
        root.setCenter(metaDataView.getViewNode());
        root.setBottom(playerControlsView.getViewNode());
        final Scene scene = new Scene(root, 800, 400);
        RotateUtils.initSceneDragAndDrop(scene, this::playUrl);

		scene.getStylesheets().add(Chapter8Resource.MEDIA.getURL().toString());
        primaryStage.setScene(scene);
        onCloseWindow(primaryStage, () -> SongUtils.stopAndDispose(songModel.getMediaPlayer()));
        primaryStage.setTitle("Basic Audio Player With Control");
        primaryStage.show();
        songModel.getPlayer().play();
    }

    public static void main(String[] args) {

        launch(args);
	}
}
