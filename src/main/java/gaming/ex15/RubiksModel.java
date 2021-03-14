package gaming.ex15;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Group;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;
import org.slf4j.Logger;
import simplebuilder.SimpleTimelineBuilder;
import utils.ex.HasLogging;

public class RubiksModel {
    private static final int CUBE_COMPLEXITY = 3;
	private static final Logger LOGGER = HasLogging.log();

	private RubiksPiece[][][] pieces = new RubiksPiece[CUBE_COMPLEXITY][CUBE_COMPLEXITY][CUBE_COMPLEXITY];

	private DoubleProperty angle = new SimpleDoubleProperty(0);
	private Timeline timeline = new SimpleTimelineBuilder()
			.keyFrames(new KeyFrame(Duration.ZERO, new KeyValue(angle, 0)),
					new KeyFrame(Duration.seconds(1), new KeyValue(angle, 90)))
			.onFinished(e -> unbindAll()).build();public void extracted(Group root) {
		for (int i = 0; i < CUBE_COMPLEXITY; i++) {
			for (int j = 0; j < CUBE_COMPLEXITY; j++) {
				for (int k = 0; k < CUBE_COMPLEXITY; k++) {
					RubiksPiece rubiksPiece = new RubiksPiece();
                    rubiksPiece.setTranslateX(-i * (RubiksPiece.RUBIKS_CUBE_SIZE + 1.0));
                    rubiksPiece.setTranslateY(j * (RubiksPiece.RUBIKS_CUBE_SIZE + 1.0));
                    rubiksPiece.setTranslateZ(k * (RubiksPiece.RUBIKS_CUBE_SIZE + 1.0));
					pieces[i][j][k] = rubiksPiece;
					root.getChildren().add(rubiksPiece);
				}
			}
		}
	}


	public void rotateCube(RubiksCubeFaces face, boolean clockwise) {
		List<RubiksPiece> piecesInFace = getFacePieces(face);
		for (RubiksPiece e : piecesInFace) {
			face.rotate(e, angle, clockwise);
		}
		timeline.playFromStart();
		List<RubiksPiece> arrayList = new ArrayList<>();
		for (int i = 0; i < piecesInFace.size(); i++) {
			int j = !clockwise ? rotateAntiClockWise(i) : rotateClockWise(i);
			RubiksPiece rubiksPiece2 = piecesInFace.get(j);
			arrayList.add(rubiksPiece2);
		}
		for (int i = 0; i < CUBE_COMPLEXITY; i++) {
			for (int j = 0; j < CUBE_COMPLEXITY; j++) {
				face.set(pieces, i, j, arrayList.get(i * CUBE_COMPLEXITY + j));
			}
		}
	}

	public void setPivot() {
		Stream.of(pieces).flatMap(Stream::of).flatMap(Stream::of).forEach(p -> setPivot(p, pieces[1][1][1]));
	}

    private List<RubiksPiece> getFacePieces(RubiksCubeFaces face) {
		return IntStream
				.range(0, CUBE_COMPLEXITY).boxed().flatMap(i -> IntStream.range(0, CUBE_COMPLEXITY)
						.mapToObj(j -> face.get(pieces, i, j)).map(RubiksPiece.class::cast))
				.collect(Collectors.toList());
	}

	private void unbindAll() {
		Stream.of(pieces).flatMap(Stream::of).flatMap(Stream::of).forEach(RubiksPiece::unbindAngle);
		if (RubiksPiece.DEBUG && LOGGER.isInfoEnabled()) {
			StringBuilder s = new StringBuilder();
			for (int i = 0; i < CUBE_COMPLEXITY; i++) {
				for (int j = 0; j < CUBE_COMPLEXITY; j++) {
					for (int k = 0; k < CUBE_COMPLEXITY; k++) {
						s.append(pieces[i][j][k] + " ");
					}
					s.append("\n");
				}
			}
			s.append("\n");
			LOGGER.info(s.toString());
		}
	}

    public static int rotateClockWise(int j) {
		return j % 3 * 3 + 2 - j / 3;
	}

	public static void setPivot(RubiksPiece pivot0, RubiksPiece pivot1) {
		if (pivot0.getRotations().isEmpty()) {
			RubiksCubeFaces[] values = RubiksCubeFaces.values();
			for (RubiksCubeFaces face : values) {
				Rotate rotate = new Rotate(0, face.getAxis());
				rotate.setPivotX(pivot1.getTranslateX() - pivot0.getTranslateX() - RubiksPiece.RUBIKS_CUBE_SIZE / 2.0);
				rotate.setPivotY(pivot1.getTranslateY() - pivot0.getTranslateY() - RubiksPiece.RUBIKS_CUBE_SIZE / 2.0);
				rotate.setPivotZ(pivot1.getTranslateZ() - pivot0.getTranslateZ());
				pivot0.getTransforms().add(rotate);
				pivot0.getRotations().put(face.getAxis(), rotate);
			}
		}
		pivot0.getTransforms().add(new Rotate(0));
	}
	private static int rotateAntiClockWise(int i) {
		return 6 - i % 3 * 3 + i / 3;
	}

}
