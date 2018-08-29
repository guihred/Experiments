/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fxproexercises.ch08;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 *
 * @author Note
 */
public class BasicAudioPlayerWithControlLauncher extends Application {

    private final SongModel songModel = new SongModel();

    private void initSceneDragAndDrop(Scene scene) {
        scene.setOnDragOver((DragEvent event) -> {
            Dragboard db = event.getDragboard();
            if (db.hasFiles() || db.hasUrl()) {
                event.acceptTransferModes(TransferMode.ANY);
            }
            event.consume();
        });
        scene.setOnDragDropped((DragEvent event) -> {
            Dragboard db = event.getDragboard();
            String url = null;
            if (db.hasFiles()) {
                url = db.getFiles().get(0).toURI().toString();
            } else if (db.hasUrl()) {
                url = db.getUrl();
            }
            if (url != null) {
                songModel.setURL(url);
                songModel.getMediaPlayer().play();

            }
            event.setDropCompleted(url != null);
            event.consume();
        });
    }

    @Override
    public void start(Stage primaryStage) {
		songModel.setURL(Chapter8Resource.TEEN_TITANS.getURL().toString());
        MetadataView metaDataView = new MetadataView(songModel);
        PlayerControlView playerControlsView = new PlayerControlView(songModel);
        final BorderPane root = new BorderPane();
        root.setCenter(metaDataView.getViewNode());
        root.setBottom(playerControlsView.getViewNode());
        final Scene scene = new Scene(root, 800, 400);
        initSceneDragAndDrop(scene);

		scene.getStylesheets().add(Chapter8Resource.MEDIA.getURL().toString());
        primaryStage.setScene(scene);
        primaryStage.setTitle("Audio Player 3");
        primaryStage.show();
        songModel.getPlayer().play();
    }

    public static void main(String[] args) {
        launch(args);
	}
}
