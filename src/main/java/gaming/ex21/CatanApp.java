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
        root.setLeft(value);
        double size = Terrain.RADIUS * Math.sqrt(3) * 11 / 2;
        Scene scene = new Scene(root, size * 3 / 2, size);
        primaryStage.setTitle("Settlers of Catan");
        primaryStage.setScene(scene);
        CatanModel.create(center, value);
        primaryStage.show();
    }

    public static void main(final String[] args) {
        launch(args);
    }

}
