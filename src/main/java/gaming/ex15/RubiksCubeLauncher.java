package gaming.ex15;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.input.MouseEvent;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import javafx.util.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RubiksCubeLauncher extends Application {
	public static final int CUBE_COMPLEXITY = 3;

	static final Logger LOGGER = LoggerFactory.getLogger(RubiksCubeLauncher.class);

	public static final int RUBIKS_CUBE_SIZE = 20;
	private RubiksPiece[][][] pieces = new RubiksPiece[CUBE_COMPLEXITY][CUBE_COMPLEXITY][CUBE_COMPLEXITY];

	@Override
	public void start(Stage stage) throws Exception {
		Group root = new Group();
		for (int i = 0; i < CUBE_COMPLEXITY; i++) {
			for (int j = 0; j < CUBE_COMPLEXITY; j++) {
				for (int k = 0; k < CUBE_COMPLEXITY; k++) {
					RubiksPiece rubiksPiece = new RubiksPiece(RUBIKS_CUBE_SIZE);
					rubiksPiece.setTranslateX(i * (RUBIKS_CUBE_SIZE + 1));
					rubiksPiece.setTranslateY(j * (RUBIKS_CUBE_SIZE + 1));
					rubiksPiece.setTranslateZ(k * (RUBIKS_CUBE_SIZE + 1));
					pieces[i][j][k] = rubiksPiece;
					root.getChildren().add(rubiksPiece);
				}
			}
		}
		SubScene subScene = new SubScene(root, 640, 480, true, SceneAntialiasing.BALANCED);
		subScene.heightProperty().bind(stage.heightProperty());
		subScene.widthProperty().bind(stage.widthProperty());
		PerspectiveCamera camera = new PerspectiveCamera(true);
		camera.setFarClip(1000.0);
		camera.setTranslateZ(-100);
		camera.setRotationAxis(Rotate.Y_AXIS);
		camera.setNearClip(0.2);
		camera.setFieldOfView(40);
		subScene.setCamera(camera);

		Scene sc = new Scene(new Group(subScene));
		stage.setScene(sc);
		stage.show();

		EventHandler<MouseEvent> a = new RubiksMouseEvent(sc, camera);
		RubiksKeyboard value = new RubiksKeyboard(camera, this);
		sc.setOnKeyPressed(value);
		sc.setOnKeyReleased(value::keyReleased);
		sc.setOnMouseMoved(a);
		sc.setOnMouseMoved(a);
	}
//	0,1,2	6,3,0
//	3,4,5	7,4,1
//	6,7,8	8,5,2
	// i=a/3
	// j=a%3
	// x=a%3-1
	// y=1-a/3
	// b=(a%3-1)*3+(1-a/3)
	
	public int rotateClockWise(int i) {
		return 6 - i % 3 * 3 + i / 3;
	}

	public int rotateAntiClockWise(int j) {
		return j % 3 * 3 + 2 - j / 3;
	}

	public void rotateCube(RubiksCubeFaces face, boolean clockwise) {
		List<RubiksPiece> collect = IntStream.range(0, CUBE_COMPLEXITY).boxed().flatMap(i -> {
			Stream<RubiksPiece> mapToObj = IntStream.range(0, CUBE_COMPLEXITY).mapToObj(j -> face.get(pieces, i, j));
			return mapToObj;
		}).collect(Collectors.toList());

		DoubleProperty angle = new SimpleDoubleProperty(0);
		RubiksPiece pivot = face.get(pieces, 1, 1);
		collect.forEach((RubiksPiece e) -> e.rotate(face, pivot, angle));
		Timeline timeline = new Timeline(new KeyFrame(Duration.ZERO, new KeyValue(angle, 0)), new KeyFrame(
				Duration.seconds(1), new KeyValue(angle, clockwise ? -90 : 90)));
		timeline.play();
		List<RubiksPiece> arrayList = new ArrayList<>();
		for (int i = 0; i < collect.size(); i++) {
			int j = !clockwise ? rotateClockWise(i) : rotateAntiClockWise(i);
			RubiksPiece rubiksPiece2 = collect.get(j);
			arrayList.add(rubiksPiece2);
		}
		for (int i = 0; i < CUBE_COMPLEXITY; i++) {
			for (int j = 0; j < CUBE_COMPLEXITY; j++) {
				face.set(pieces, i, j, arrayList.get(i * CUBE_COMPLEXITY + j));
			}
		}
	}

	public static void main(String[] args) {
		launch(args);
	}
}
