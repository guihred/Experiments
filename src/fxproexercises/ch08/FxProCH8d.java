/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fxproexercises.ch08;

import java.io.File;
import javafx.application.Application;
import static javafx.application.Application.launch;
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
public class FxProCH8d extends Application {

    private final SongModel songModel;
    private MetadataView metaDataView;
    private PlayerControlView playerControlsView;

    public static void main(String[] args) {
        launch(args);
    }

    public FxProCH8d() {
        songModel = new SongModel();
    }

    @Override
    public void start(Stage primaryStage) {
        songModel.setURL(new File("C:\\Users\\Note\\Documents\\Sistemas\\Workspace\\Teste\\TeenTitans.mp3").toURI().toString());
        metaDataView = new MetadataView(songModel);
        playerControlsView = new PlayerControlView(songModel);
        final BorderPane root = new BorderPane();
        root.setCenter(metaDataView.getViewNode());
        root.setBottom(playerControlsView.getViewNode());
        final Scene scene = new Scene(root, 800, 400);
        initSceneDragAndDrop(scene);
        final String stylesheet = new File("C:\\Users\\Note\\Documents\\Sistemas\\Workspace\\Teste\\media.css").toURI().toString();
        scene.getStylesheets().add(stylesheet);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Audio Player 3");
        primaryStage.show();
        songModel.getPlayer().play();
    }

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
}
