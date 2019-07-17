package fxsamples;

import java.security.SecureRandom;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class DrawingText extends Application {
    private SecureRandom rand = new SecureRandom();
	@Override
	public void start(Stage primaryStage) {
        primaryStage.setTitle("Drawing Text");
		Group root = new Group();
        Scene scene = new Scene(root, 500, 500, Color.WHITE);
		for (int i = 0; i < 100; i++) {
			int x = rand.nextInt((int) scene.getWidth());
			int y = rand.nextInt((int) scene.getHeight());
			Text text = new Text(x, y, "JavaFX 8");
			int rot = rand.nextInt(360);
            text.setFill(Color.rgb(rnd(), rnd(), rnd(), 1. / 2));
			text.setRotate(rot);
			root.getChildren().add(text);
		}
		primaryStage.setScene(scene);
		primaryStage.show();
	}

    private int rnd() {
        return rand.nextInt(256);
    }

	public static void main(String[] args) {
		launch(args);
	}
}
