package audio.mp3;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import utils.HasLogging;

public class PageImage extends Application {
    private static final Logger LOGGER = HasLogging.log();
    @Override
    public void start(Stage primaryStage) throws Exception {
        FlowPane root = new FlowPane();

        Platform.runLater(() -> {
            for (String url : GoogleImagesUtils.getImagens("Cachorro")) {
                try {
                    ImageView imageView = GoogleImagesUtils.convertToImage(url);
                    root.getChildren().add(imageView);
                    LOGGER.info("{} FOUND", url);
                } catch (Exception e) {
                    LOGGER.info("{} NOT found", url);
                    LOGGER.trace("", e);
                }
            }
        });
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();

    }

    public static void main(String[] args) {
        launch(args);
    }
}