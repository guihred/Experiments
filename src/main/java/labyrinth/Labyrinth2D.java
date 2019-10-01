package labyrinth;

import static labyrinth.GhostGenerator.getMapa;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class Labyrinth2D extends Application {
	private static final int SIZE = 50;

	private Color color = Color.RED;
	private int i;
	private int j;
	@Override
	public void start(Stage primaryStage) throws Exception {

		Group root = new Group();
		Button e = new Button();
		root.getChildren().add(e);

		initializeLabyrinth(root);

		Scene scene = new Scene(root);

		scene.setOnMouseClicked(event -> handleMouseClick(root));
        primaryStage.setTitle("Labyrinth 2D");
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	private void handleMouseClick(Group root) {
		String string = getMapa()[i][j];
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
		if (j >= getMapa()[i].length) {
			j = 0;
			i++;
		}
		if (i >= getMapa().length) {
			i = 0;
			j = 0;
			color = color == Color.RED ? Color.BLACK : Color.RED;
		}
	}

    public static void main(String[] args) {
		launch(args);
	}

	private static void initializeLabyrinth(Group root) {
		for (int k = 0; k < getMapa().length; k++) {
			for (int l = 0; l < getMapa()[k].length; l++) {
				String string = getMapa()[k][l];
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

}
