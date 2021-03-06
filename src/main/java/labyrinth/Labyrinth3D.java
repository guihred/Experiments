package labyrinth;

import static labyrinth.GhostGenerator.getMapa;

import java.util.List;
import java.util.stream.Collectors;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class Labyrinth3D extends Application implements CommomLabyrinth {
    private static final int STRAIGHT_ANGLE = 90;
    private static final int SIZE = 50;
	private static final double CAMERA_QUANTITY = 10.0;

	private PerspectiveCamera camera = new PerspectiveCamera(true);

	private Color color = Color.RED;
	private int i;
	private int j;

	private Group root = new Group();

	@Override
	public PerspectiveCamera getCamera() {
		return camera;
	}

	@Override
	public List<LabyrinthWall> getLabyrinthWalls() {
		return root.getChildren().stream().filter(LabyrinthWall.class::isInstance).map(LabyrinthWall.class::cast)
				.collect(Collectors.toList());
	}

	@Override
	public void start(Stage primaryStage) {
		initializeLabyrinth();
		Scene scene = new Scene(root);
        final double nearClip = 0.1;
        camera.setNearClip(nearClip);
		camera.setFarClip(1000.0);
		camera.setTranslateZ(-1000);
		scene.setCamera(camera);

		scene.setOnMouseClicked(e -> handleMouseClick());
		camera.setTranslateY(camera.getTranslateY() - CAMERA_QUANTITY);
		// End Step 2a
		// Step 2b: Add a Movement Keyboard Handler
		MovimentacaoTeclado value = new MovimentacaoTeclado(this);
		scene.setOnKeyPressed(value);
		scene.setOnKeyReleased(value::keyReleased);
        primaryStage.setTitle("Labyrinth 3D");
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	private void handleMouseClick() {
		String string = getMapa()[i][j];
		if ("_".equals(string)) {
			LabyrinthWall rectangle = new LabyrinthWall(SIZE, color);
			rectangle.setTranslateX(i * SIZE);
			rectangle.setTranslateZ(j * SIZE);
            rectangle.getRy().setAngle(STRAIGHT_ANGLE);
			root.getChildren().add(rectangle);
		} else {
			LabyrinthWall rectangle = new LabyrinthWall(SIZE, color);
			rectangle.setTranslateX(i * SIZE);
			rectangle.setTranslateZ(j * SIZE);
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
	private void initializeLabyrinth() {
		for (int k = getMapa().length - 1; k >= 0; k--) {
			for (int l = getMapa()[k].length - 1; l >= 0; l--) {
				String string = getMapa()[k][l];
				if ("_".equals(string)) {
					LabyrinthWall rectangle = new LabyrinthWall(SIZE, Color.BLUE);
					rectangle.setTranslateX(k * SIZE);
					rectangle.setTranslateZ(l * SIZE);
                    rectangle.getRy().setAngle(STRAIGHT_ANGLE);
					root.getChildren().add(rectangle);
				} else {
					LabyrinthWall rectangle = new LabyrinthWall(SIZE, Color.BLUE);
					rectangle.setTranslateX(k * SIZE);
					rectangle.setTranslateZ(l * SIZE);
					root.getChildren().add(rectangle);
				}
			}
		}
	}

	public static void main(String[] args) {
		launch(args);
	}


}
