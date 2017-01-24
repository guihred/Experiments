package gaming.ex08;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ArkanoidLauncher extends Application {


    @Override
    public void start(Stage stage) throws Exception {
        final Group group = new Group();
        final Scene scene = new Scene(group);
        new ArkanoidModel(group, scene);
        stage.setScene(scene);
        stage.setWidth(400);
        stage.setHeight(600);
        stage.setResizable(false);
        stage.show();

        

    }

    public static void main(String[] args) {
        launch(args);
    }
}
