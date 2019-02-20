package gaming.ex21;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class CatanApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        StackPane root = new StackPane();
        CatanModel.create(root);
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
