package gaming.ex08;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ArkanoidLauncher extends Application {


    @Override
	public void start(Stage stage) {
        final Group group = new Group();
        final Scene scene = new Scene(group, 400, 600);
		ArkanoidModel.create(group, scene);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
