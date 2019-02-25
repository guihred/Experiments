package gaming.ex21;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class CatanApp extends Application {

    @Override
    public void start(final Stage primaryStage) throws Exception {
        StackPane center = new StackPane();
		Pane value = new VBox();
        BorderPane root = new BorderPane(center);
        root.setRight(value);
        double size = Terrain.RADIUS * Math.sqrt(3) * 5.5;
		Scene scene = new Scene(root, size * 1.5, size);
        primaryStage.setTitle("Settlers of Catan");
        primaryStage.setScene(scene);
        primaryStage.show();
		CatanModel.create(center, value);
    }


    public static void main(final String[] args) {
        launch(args);
    }

}
