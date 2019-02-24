package gaming.ex21;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class CatanApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        StackPane center = new StackPane();
        VBox value = new VBox();
        CatanModel.create(center, value);
        BorderPane root = new BorderPane(center);
        root.setRight(value);
        double size = Terrain.RADIUS * Math.sqrt(3) * 5.5;
        Scene scene = new Scene(root, size, size);
        primaryStage.setTitle("Settlers of Catan");
        primaryStage.setScene(scene);
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }

}
