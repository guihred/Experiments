package audio.mp3;

import java.io.File;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import utils.ResourceFXUtils;

public class VideoApplication extends Application {
    @Override
    public void start(Stage stage) throws Exception {

        // goes to user Directory
        File f = ResourceFXUtils.getFirstPathByExtension(ResourceFXUtils.getOutFile(), ".mp4").toFile();

        // Converts media to string URL
        Media media = new Media(f.toURI().toURL().toString());
        MediaPlayer player = new MediaPlayer(media);
        MediaView viewer = new MediaView(player);

        // change width and height to fit video
        viewer.fitWidthProperty().bind(Bindings.selectDouble(viewer.sceneProperty(), "width"));
        viewer.fitHeightProperty().bind(Bindings.selectDouble(viewer.sceneProperty(), "height"));
        viewer.setPreserveRatio(true);

        StackPane root = new StackPane();
        root.getChildren().add(viewer);

        // set the Scene
        Scene scenes = new Scene(root, 500, 500, Color.BLACK);
        stage.setScene(scenes);
        stage.setTitle("Riddle Game");
        stage.show();
        player.play();
    }

    public static void main(String[] args) throws Exception {
        launch(args);
    }
}