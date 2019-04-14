package gaming.ex13;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import utils.ResourceFXUtils;

public class SolitaireLauncher extends Application {


    @Override
    public void start(Stage stage) throws Exception {
        final StackPane group = new StackPane();
		final BorderPane borderPane = new BorderPane(group);
        final int width = 600;
        final Scene scene = new Scene(borderPane, width, width);
		SolitaireModel.create(group, scene);
        stage.setScene(scene);
        scene.getStylesheets().add(ResourceFXUtils.toExternalForm("solitaire.css"));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
