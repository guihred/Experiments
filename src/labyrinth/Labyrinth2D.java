package labyrinth;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class Labyrinth2D extends Application {

	private static String[][] mapa = {
			{ "_", "_", "_", "_", "_", "_" },
			{ "|", "_", "_", "_", "_", "|" }, 
			{ "|", "|", "_", "|", "_", "|" },
			{ "|", "|", "_", "|", "_", "|" }, 
			{ "|", "_", "_", "|", "_", "|" },
			{ "|", "_", "_", "_", "_", "|" }, 
			{ "|", "_", "_", "_", "_", "_" }, };

	private static final int SIZE = 50;

	public static void main(String[] args) {
		launch(args);
	}

	private Color color = Color.RED;
	private int i;
	private int j;

	private void initializeLabyrinth(Group root) {
		for (int k = 0; k < mapa.length; k++) {
			for (int l = 0; l < mapa[k].length; l++) {
				String string = mapa[k][l];
				if ("_".equals(string)) {
					Rectangle rectangle = new Rectangle(k * SIZE, l * SIZE,
							SIZE / 2, SIZE);
					root.getChildren().add(rectangle);
				} else {
					Rectangle rectangle = new Rectangle(k * SIZE, l * SIZE,
							SIZE, SIZE / 2);
					root.getChildren().add(rectangle);
				}
			}
		}
	}

	@Override
	public void start(Stage primaryStage) throws Exception {

		Group root = new Group();
		Button e = new Button();
		root.getChildren().add(e);

		initializeLabyrinth(root);

		Scene scene = new Scene(root);
		// PerspectiveCamera camera = new PerspectiveCamera(true);
		// camera.setNearClip(0.1);
		// camera.setFarClip(10000.0);
		// camera.setTranslateZ(-1000);
		// scene.setCamera(camera);

		scene.setOnMouseClicked(event -> {
			String string = mapa[i][j];
			if ("_".equals(string)) {
				Rectangle rectangle = new Rectangle(i * SIZE, j * SIZE,
						SIZE / 2, SIZE);
				rectangle.setFill(color);
				root.getChildren().add(rectangle);
			} else {
				Rectangle rectangle = new Rectangle(i * SIZE, j * SIZE, SIZE,
						SIZE / 2);
				rectangle.setFill(color);
				root.getChildren().add(rectangle);
			}
			j++;
			if (j >= mapa[i].length) {
				j = 0;
				i++;
			}
			if (i >= mapa.length) {
				i = 0;
				j = 0;
				color = color == Color.RED ? Color.BLACK : Color.RED;
			}

		});
		primaryStage.setTitle("EXP 1: Labyrinth");
		primaryStage.setScene(scene);
		primaryStage.show();
	}

}
