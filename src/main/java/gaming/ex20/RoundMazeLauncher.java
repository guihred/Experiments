package gaming.ex20;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.stage.Stage;

public class RoundMazeLauncher extends Application {


    private static final int PAD = 50;
    private static final int HEIGHT = 620;

    @Override
	public void start(Stage stage) {
        Canvas canvas = new Canvas(HEIGHT, HEIGHT);
        canvas.setTranslateX(10);
        final Scene scene = new Scene(new Group(canvas));
		RoundMazeModel.create(scene, canvas).draw();
        stage.setScene(scene);
        stage.setWidth(canvas.getWidth() + PAD);
        stage.setHeight(canvas.getHeight() + PAD);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
