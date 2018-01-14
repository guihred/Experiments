package gaming.ex15;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import simplebuilder.SimpleTimelineBuilder;

public class RubiksCubeLauncher extends Application {
    public static final boolean DEBUG = true;
	public static final int CUBE_COMPLEXITY = 3;

	static final Logger LOGGER = LoggerFactory.getLogger(RubiksCubeLauncher.class);

	public static final int RUBIKS_CUBE_SIZE = 50;
	private RubiksPiece[][][] pieces = new RubiksPiece[CUBE_COMPLEXITY][CUBE_COMPLEXITY][CUBE_COMPLEXITY];

	@Override
	public void start(Stage stage) throws Exception {
		Group root = new Group();
		for (int i = 0; i < CUBE_COMPLEXITY; i++) {
			for (int j = 0; j < CUBE_COMPLEXITY; j++) {
				for (int k = 0; k < CUBE_COMPLEXITY; k++) {
					RubiksPiece rubiksPiece = new RubiksPiece(RUBIKS_CUBE_SIZE);
                    rubiksPiece.setTranslateX(-i * (RUBIKS_CUBE_SIZE + 1.0));
                    rubiksPiece.setTranslateY(j * (RUBIKS_CUBE_SIZE + 1.0));
                    rubiksPiece.setTranslateZ(k * (RUBIKS_CUBE_SIZE + 1.0));
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
		camera.setTranslateX(-200);
		camera.setTranslateZ(-200);
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
		setPivot();
	}
	
	public int rotateAntiClockWise(int i) {
		return 6 - i % 3 * 3 + i / 3;
	}

	public int rotateClockWise(int j) {
		return j % 3 * 3 + 2 - j / 3;
	}

	DoubleProperty angle = new SimpleDoubleProperty(0);
	Timeline timeline = new SimpleTimelineBuilder()
			.keyFrames(new KeyFrame(Duration.ZERO, new KeyValue(angle, 0)),
					new KeyFrame(Duration.seconds(1), new KeyValue(angle, 90)))
			.onFinished(e -> unbindAll()).build();

	private void setPivot() {
		Stream.of(pieces).flatMap(Stream::of).flatMap(Stream::of).forEach(p -> p.setPivot(pieces[1][1][1]));
	}
	private void unbindAll() {
        Stream.of(pieces).flatMap(Stream::of).flatMap(Stream::of).forEach(RubiksPiece::unbindAngle);
		if (RubiksCubeLauncher.DEBUG) {
			for (int i = 0; i < CUBE_COMPLEXITY; i++) {
				for (int j = 0; j < CUBE_COMPLEXITY; j++) {
					for (int k = 0; k < CUBE_COMPLEXITY; k++) {
						System.out.print(pieces[i][j][k] + " ");
					}
					System.out.println();
				}
			}
			System.out.println();
		}
	}

	public void rotateCube(RubiksCubeFaces face, boolean clockwise) {
		RubiksPiece pivot = pieces[1][1][1];
		List<RubiksPiece> collect = getFacePieces(face);
        for (RubiksPiece e : collect) {
            e.rotate(face, angle, clockwise);
        }
		timeline.playFromStart();
        if (RubiksCubeLauncher.DEBUG) {
            System.out.println(face);
        }
		List<RubiksPiece> arrayList = new ArrayList<>();
		for (int i = 0; i < collect.size(); i++) {
			int j = !clockwise ? rotateAntiClockWise(i) : rotateClockWise(i);
			RubiksPiece rubiksPiece2 = collect.get(j);
			arrayList.add(rubiksPiece2);
		}
		for (int i = 0; i < CUBE_COMPLEXITY; i++) {
			for (int j = 0; j < CUBE_COMPLEXITY; j++) {
				face.set(pieces, i, j, arrayList.get(i * CUBE_COMPLEXITY + j));
			}
		}
	}

	private List<RubiksPiece> getFacePieces(RubiksCubeFaces face) {
		return IntStream
                .range(0, CUBE_COMPLEXITY)
				.boxed()
				.flatMap(i -> IntStream
						.range(0, CUBE_COMPLEXITY)
						.mapToObj(j -> face.get(pieces, i, j))
						.map(RubiksPiece.class::cast))
				.collect(Collectors.toList());
	}

	public static void main(String[] args) {
		launch(args);
	}
}
