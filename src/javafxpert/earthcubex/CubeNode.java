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
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.input.KeyCode;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.util.Duration;
import others.SimpleRotateBuilder;

/**
 *
 * @author Jim Weaver
 */
public class CubeNode extends Parent {
	static double HOME_ANGLE_X = 30.0;
	static double HOME_ANGLE_Y = -40.0;
	static double MIN_ANGLE_X = -70.0;
	static double MAX_ANGLE_X = 70.0;
	static double MIN_ANGLE_Y = -720.0;
	static double MAX_ANGLE_Y = 720.0;
	static double MIN_TRANSLATE_Z = 0.0;
	static double MAX_TRANSLATE_Z = 10000.0;

	CubeModel cubeModel = CubeModel.instance;

	CubeFace rearFace;
	CubeFace bottomFace;
	CubeFace leftFace;
	CubeFace rightFace;
	CubeFace topFace;
	CubeFace frontFace;

	DoubleProperty angleX = new SimpleDoubleProperty(0);

	DoubleProperty angleY = new SimpleDoubleProperty(0);

	public CubeNode(CubeModel model) {

		angleX.addListener((ov) -> {
			arrangeFacesZOrder();

		});

		angleY.addListener((ov, oldValue, newValue) -> arrangeFacesZOrder());

		rearFace = new CubeFace(cubeModel, CubeFace.REAR_FACE);
		Rotate build = new SimpleRotateBuilder().angle(180.0).axis(Rotate.Y_AXIS).pivotX(CubeFace.edgeLength / 2)
				.build();

		rearFace.getTransforms().setAll(
				new Translate(0, 0, CubeFace.edgeLength),
				build);

		bottomFace = new CubeFace(cubeModel, CubeFace.BOTTOM_FACE);
		bottomFace.getTransforms().setAll(
				new Translate(0, 0, CubeFace.edgeLength),
				new SimpleRotateBuilder().angle(90.0).axis(Rotate.X_AXIS)
						.pivotY(CubeFace.edgeLength).build());

		leftFace = new CubeFace(cubeModel, CubeFace.LEFT_FACE);
		leftFace.getTransforms().setAll(
				new Translate(0, 0, CubeFace.edgeLength),
				new SimpleRotateBuilder().angle(90.0).axis(Rotate.Y_AXIS)
						.pivotX(0).build());

		rightFace = new CubeFace(cubeModel, CubeFace.RIGHT_FACE);
		rightFace.getTransforms().setAll(
				new Translate(0, 0, CubeFace.edgeLength),
				new SimpleRotateBuilder().angle(-90.0).axis(Rotate.Y_AXIS)
						.pivotX(CubeFace.edgeLength).build());

		topFace = new CubeFace(cubeModel, CubeFace.TOP_FACE);
		topFace.getTransforms().setAll(
				new Translate(0, 0, CubeFace.edgeLength),
				new SimpleRotateBuilder().angle(-90.0).axis(Rotate.X_AXIS)
						.pivotX(0).build());

		frontFace = new CubeFace(cubeModel, CubeFace.FRONT_FACE);
		frontFace.getTransforms().setAll(
				new Translate(0, 0, 0));

		getChildren().addAll(rearFace, topFace, leftFace, rightFace,
				bottomFace, frontFace);

		Rotate xRotate;
		Rotate yRotate;
		xRotate = new Rotate();
		xRotate.setAxis(Rotate.X_AXIS);
		xRotate.setPivotX(CubeFace.edgeLength * 0.5);
		xRotate.setPivotY(CubeFace.edgeLength * 0.5);
		xRotate.setPivotZ(CubeFace.edgeLength * 0.5);
		getTransforms().setAll(
				xRotate,
				yRotate = new SimpleRotateBuilder().axis(Rotate.Y_AXIS)
						.pivotX(CubeFace.edgeLength * 0.5)
						.pivotY(CubeFace.edgeLength * 0.5)
						.pivotZ(CubeFace.edgeLength * 0.5).build()

		);
		xRotate.angleProperty().bind(angleX);
		yRotate.angleProperty().bind(angleY);

		setOnMousePressed(me -> {
			dragPressedAngleX = angleX.getValue();
			dragPressedAngleY = angleY.getValue();

			dragStartOffsetX = me.getScreenX() - getScene().getWindow().getX();
			dragStartOffsetY = me.getScreenY() - getScene().getWindow().getY();
		});

		setOnMouseDragged(me -> {
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
		});

		setOnKeyPressed(ke -> {
			if (ke.getCode() == KeyCode.SPACE) {
				if (cubeModel.mapOpacity.getValue() == 0.0) {
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
		rearFace.zPos.setValue(CubeFace.radius
				* Math.cos(Math.toRadians(angleY.getValue() + 0)));
		bottomFace.zPos.setValue(CubeFace.radius
				* Math.cos(Math.toRadians(angleX.getValue() + 270)));
		leftFace.zPos.setValue(CubeFace.radius
				* Math.cos(Math.toRadians(angleY.getValue() + 270)));
		rightFace.zPos.setValue(CubeFace.radius
				* Math.cos(Math.toRadians(angleY.getValue() + 90)));
		topFace.zPos.setValue(CubeFace.radius
				* Math.cos(Math.toRadians(angleX.getValue() + 90)));
		frontFace.zPos.setValue(CubeFace.radius
				* Math.cos(Math.toRadians(angleY.getValue() + 180)));

		FXCollections.sort(getChildren(), new CubeFaceComparator());
	}

	public void goHomePosition() {
		Timeline homeTimeline = new Timeline(
						new KeyFrame(new Duration(1000.0), new KeyValue(angleX,
								HOME_ANGLE_X, Interpolator.EASE_BOTH),
								new KeyValue(angleY, HOME_ANGLE_Y,
								Interpolator.EASE_BOTH)));
		homeTimeline.play();
	}

	public Timeline showMapTimeline = new Timeline(
					new KeyFrame(new Duration(0.0), t -> goHomePosition(),
							new KeyValue(cubeModel.mapOpacity, 0.0,
									Interpolator.LINEAR)),
					new KeyFrame(new Duration(1000.0), new KeyValue(
							cubeModel.mapOpacity, 0.7, Interpolator.EASE_BOTH)))
	;

	public Timeline hideMapTimeline = new Timeline(
					new KeyFrame(new Duration(0.0), t -> {
						// goHomePosition();
						}, new KeyValue(cubeModel.mapOpacity, 0.7,
								Interpolator.LINEAR)),
					new KeyFrame(new Duration(1000.0), new KeyValue(
							cubeModel.mapOpacity, 0.0, Interpolator.EASE_BOTH)))
	;

	public Node frontNode;
	public Node rearNode;
	public Node leftNode;
	public Node rightNode;
	public Node topNode;
	public Node bottomNode;

	double dragPressedAngleX;
	double dragPressedAngleY;

	double dragStartOffsetX;
	double dragStartOffsetY;

}
