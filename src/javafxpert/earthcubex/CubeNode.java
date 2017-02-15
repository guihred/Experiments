/*
 * CubeNode.java
 *
 * A cube-shaped UI component upon whose faces other nodes may placed.
 *
 * Developed by James L. Weaver (jim.weaver#javafxpert.com) to demonstrate the
 * use of 3D features in the JavaFX 2.0 API
 */
package javafxpert.earthcubex;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.scene.Parent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.util.Duration;
import simplebuilder.SimpleRotateBuilder;

/**
 *
 * @author Jim Weaver
 */
public class CubeNode extends Parent {
	private static final double HOME_ANGLE_X = 30.0;
	private static final double HOME_ANGLE_Y = -40.0;
	private static final double MAX_TRANSLATE_Z = 10000.0;
	private static final double MIN_TRANSLATE_Z = 0.0;

	private DoubleProperty angleX = new SimpleDoubleProperty(0);

	private DoubleProperty angleY = new SimpleDoubleProperty(0);
	private CubeFace bottomFace;
	private CubeModel cubeModel = CubeModel.instance;
	private double dragPressedAngleX;
	private double dragPressedAngleY;

	private double dragStartOffsetX;

	private double dragStartOffsetY;

	private CubeFace frontFace;

	private Timeline hideMapTimeline = new Timeline(
			new KeyFrame(new Duration(0.0), t -> goHomePosition(), new KeyValue(cubeModel.getMapOpacity(), 0.7,Interpolator.LINEAR)),
			new KeyFrame(new Duration(1000.0), new KeyValue(cubeModel.getMapOpacity(), 0.0, Interpolator.EASE_BOTH)))
	;

	private CubeFace leftFace;

	private CubeFace rearFace;
	private CubeFace rightFace;
	private Timeline showMapTimeline = new Timeline(
					new KeyFrame(new Duration(0.0), t -> goHomePosition(),
							new KeyValue(cubeModel.getMapOpacity(), 0.0,
									Interpolator.LINEAR)),
					new KeyFrame(new Duration(1000.0), new KeyValue(
					cubeModel.getMapOpacity(), 0.7, Interpolator.EASE_BOTH)));
	private CubeFace topFace;

	public CubeNode() {

		angleX.addListener(ov -> arrangeFacesZOrder());

		angleY.addListener((ov, oldValue, newValue) -> arrangeFacesZOrder());

		rearFace = new CubeFace(CubeFace.REAR_FACE);
		Rotate build = new SimpleRotateBuilder().angle(180.0).axis(Rotate.Y_AXIS).pivotX(CubeFace.EDGE_LENGTH / 2)
				.build();

		rearFace.getTransforms().setAll(
				new Translate(0, 0, CubeFace.EDGE_LENGTH),
				build);

		bottomFace = new CubeFace(CubeFace.BOTTOM_FACE);
		bottomFace.getTransforms().setAll(
				new Translate(0, 0, CubeFace.EDGE_LENGTH),
				new SimpleRotateBuilder().angle(90.0).axis(Rotate.X_AXIS)
						.pivotY(CubeFace.EDGE_LENGTH).build());

		leftFace = new CubeFace(CubeFace.LEFT_FACE);
		leftFace.getTransforms().setAll(
				new Translate(0, 0, CubeFace.EDGE_LENGTH),
				new SimpleRotateBuilder().angle(90.0).axis(Rotate.Y_AXIS)
						.pivotX(0).build());

		rightFace = new CubeFace(CubeFace.RIGHT_FACE);
		rightFace.getTransforms().setAll(
				new Translate(0, 0, CubeFace.EDGE_LENGTH),
				new SimpleRotateBuilder().angle(-90.0).axis(Rotate.Y_AXIS)
						.pivotX(CubeFace.EDGE_LENGTH).build());

		topFace = new CubeFace(CubeFace.TOP_FACE);
		topFace.getTransforms().setAll(
				new Translate(0, 0, CubeFace.EDGE_LENGTH),
				new SimpleRotateBuilder().angle(-90.0).axis(Rotate.X_AXIS)
						.pivotX(0).build());

		frontFace = new CubeFace(CubeFace.FRONT_FACE);
		frontFace.getTransforms().setAll(
				new Translate(0, 0, 0));

		getChildren().addAll(rearFace, topFace, leftFace, rightFace,
				bottomFace, frontFace);

		Rotate xRotate = new SimpleRotateBuilder().axis(Rotate.X_AXIS).pivotX(CubeFace.EDGE_LENGTH * 0.5)
				.pivotY(CubeFace.EDGE_LENGTH * 0.5).pivotZ(CubeFace.EDGE_LENGTH * 0.5).build();
		Rotate yRotate = new SimpleRotateBuilder().axis(Rotate.Y_AXIS).pivotX(CubeFace.EDGE_LENGTH * 0.5)
				.pivotY(CubeFace.EDGE_LENGTH * 0.5).pivotZ(CubeFace.EDGE_LENGTH * 0.5).build();
		getTransforms().setAll(xRotate, yRotate);
		xRotate.angleProperty().bind(angleX);
		yRotate.angleProperty().bind(angleY);

		setOnMousePressed(me -> {
			dragPressedAngleX = angleX.getValue();
			dragPressedAngleY = angleY.getValue();

			dragStartOffsetX = me.getScreenX() - getScene().getWindow().getX();
			dragStartOffsetY = me.getScreenY() - getScene().getWindow().getY();
		});

		setOnMouseDragged(this::handleMouseDragged);

		setOnKeyPressed(ke -> {
			if (ke.getCode() == KeyCode.SPACE) {
				if (cubeModel.getMapOpacity().getValue() == 0.0) {
					showMapTimeline.playFromStart();
				} else {
					hideMapTimeline.playFromStart();
				}
			} else if (ke.getCode() == KeyCode.ESCAPE) {
				Platform.exit();
			}
		});
	}

	final void arrangeFacesZOrder() {
		rearFace.setZPos(CubeFace.RADIUS * Math.cos(Math.toRadians(angleY.getValue() + 0)));
		bottomFace.setZPos(CubeFace.RADIUS * Math.cos(Math.toRadians(angleX.getValue() + 270)));
		leftFace.setZPos(CubeFace.RADIUS * Math.cos(Math.toRadians(angleY.getValue() + 270)));
		rightFace.setZPos(CubeFace.RADIUS * Math.cos(Math.toRadians(angleY.getValue() + 90)));
		topFace.setZPos(CubeFace.RADIUS * Math.cos(Math.toRadians(angleX.getValue() + 90)));
		frontFace.setZPos(CubeFace.RADIUS * Math.cos(Math.toRadians(angleY.getValue() + 180)));

		FXCollections.sort(getChildren(), new CubeFaceComparator());
	}

	public void goHomePosition() {
		Timeline homeTimeline = new Timeline(
				new KeyFrame(new Duration(1000.0), new KeyValue(angleX, HOME_ANGLE_X, Interpolator.EASE_BOTH),
						new KeyValue(angleY, HOME_ANGLE_Y, Interpolator.EASE_BOTH)));
		homeTimeline.play();
	}

	private void handleMouseDragged(MouseEvent me) {
		if (me.isControlDown()) {
			getScene().getWindow().setX(me.getScreenX() - dragStartOffsetX);
			getScene().getWindow().setY(me.getScreenY() - dragStartOffsetY);
		} else if (me.isAltDown()) {
			double curTranslateZ = getTranslateZ();
			double proposedTranslateZ = curTranslateZ
					+ (me.getScreenY() - getScene().getWindow().getY() - dragStartOffsetY)
					* 10;
			if (proposedTranslateZ > MAX_TRANSLATE_Z) {
				setTranslateZ(MAX_TRANSLATE_Z);
			} else if (proposedTranslateZ < MIN_TRANSLATE_Z) {
				setTranslateZ(MIN_TRANSLATE_Z);
			} else {
				setTranslateZ(proposedTranslateZ);
			}
		} else {
			angleY.setValue((me.getScreenX()
					- getScene().getWindow().getX() - dragStartOffsetX)
					/ 3 * -1 + dragPressedAngleY);
			angleX.setValue((me.getScreenY()
					- getScene().getWindow().getY() - dragStartOffsetY)
					/ 3 + dragPressedAngleX);
		}
	}
	public void playShowMap() {
		showMapTimeline.playFromStart();
	}

}
