/*
 * CubeFace.java
 *
 * Represents a face on the CubeNode UI component.
 *
 * Developed by James L. Weaver (jim.weaver#javafxpert.com) to demonstrate the
 * use of 3D features in the JavaFX 2.0 API
 */
package javafxpert.earthcubex;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.effect.PerspectiveTransform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import others.SimplePerspectiveTransformBuilder;

/**
 * 
 * @author Jim Weaver
 */
public class CubeFace extends Parent {
	static double FACE_HUE = 211;
	static double FACE_SAT = 0.25;

	static int FRONT_FACE = 1;
	static int RIGHT_FACE = 2;
	static int REAR_FACE = 3;
	static int LEFT_FACE = 0;
	static int TOP_FACE = 4;
	static int BOTTOM_FACE = 5;

	public static double edgeLength = 512;
	public static double radius = edgeLength * 0.5;

	CubeModel cubeModel = CubeModel.instance;

	Rectangle faceRect;
	Node node;

	DoubleProperty zPos = new SimpleDoubleProperty(0);

	public CubeFace(int face) {
		getChildren().addAll(createFaceRectangle(), createMapTiles(face));

		zPos.addListener((ov, oldValue, newValue) -> faceRect
				.setFill(computeFaceHSB()));
	}

	final Rectangle createFaceRectangle() {
		faceRect = new Rectangle();
		faceRect.setWidth(edgeLength);
		faceRect.setHeight(edgeLength);
		faceRect.setFill(Color.TRANSPARENT);
		return faceRect;
	}

	Node createMapTiles(int face) {
		if (face == FRONT_FACE || face == RIGHT_FACE || face == REAR_FACE
				|| face == LEFT_FACE) {
			return createSideMapTiles(face);
		} else if (face == TOP_FACE) {
			StackPane stackPane = new StackPane();
			for (int side = 0; side <= 3; side++) {
				stackPane.getChildren().add(createTopMapTiles(side));
			}
			ImageView mapTile = new ImageView();
			mapTile.setImage(new Image(
					"http://mt3.google.com/vt/v=w2.97&x=2&y=8&z=4"));
			mapTile.setFitWidth(CubeFace.edgeLength / 4);
			mapTile.setFitHeight(CubeFace.edgeLength / 4);
			mapTile.opacityProperty().bind(cubeModel.mapOpacity);
			stackPane.getChildren().add(mapTile);
			return stackPane;
		} else if (face == BOTTOM_FACE) {
			StackPane stackPane = new StackPane();
			for (int side = 0; side <= 3; side++) {
				stackPane.getChildren().add(createBottomMapTiles(side));
			}
			ImageView mapTile = new ImageView();
			mapTile.setImage(new Image(
					"http://mt3.google.com/vt/v=w2.97&x=2&y=15&z=4"));
			mapTile.setFitWidth(CubeFace.edgeLength / 4);
			mapTile.setFitHeight(CubeFace.edgeLength / 4);
			mapTile.opacityProperty().bind(cubeModel.mapOpacity);
			stackPane.getChildren().add(mapTile);
			return stackPane;
		} else {
			return null;
		}
	}

	Node createSideMapTiles(int sideNum) {
		TilePane tilePane;
		int xOffset = sideNum * 2;
		tilePane = new TilePane();
		tilePane.setPrefColumns(2);
		tilePane.setPrefRows(2);

		for (int y = 3; y <= 4; y++) {
			for (int x = xOffset; x <= xOffset + 1; x++) {
				int xm = (x + 1) % 8;
				ImageView mapTile = new ImageView();
				mapTile.setImage(new Image(
						"http://mt3.google.com/vt/v=w2.97&x=" + xm + "&y=" + y
								+ "&z=3"));
				mapTile.setFitWidth(CubeFace.edgeLength / 2);
				mapTile.setFitHeight(CubeFace.edgeLength / 2);
				mapTile.opacityProperty().bind(cubeModel.mapOpacity);
				tilePane.getChildren().add(mapTile);
			}
		}
		return tilePane;
	}

	Node createTopMapTiles(int sideNum) {
		TilePane tilePane;
		int xOffset = sideNum * 2;
		tilePane = new TilePane();
		tilePane.setRotate((450 - sideNum * 90) % 360);
		tilePane.setPrefColumns(2);
		tilePane.setPrefRows(3);
		PerspectiveTransform transform = new SimplePerspectiveTransformBuilder()
				.ulx(edgeLength * 0.375).uly(edgeLength * 0.625)
				.urx(edgeLength * 0.625).ury(edgeLength * 0.625).llx(0)
				.lly(edgeLength).lrx(edgeLength).lry(edgeLength).build();
		tilePane.setEffect(transform);

		for (int y = 0; y <= 2; y++) {
			for (int x = xOffset; x <= xOffset + 1; x++) {
				int xm = (x + 1) % 8;
				ImageView mapTile = new ImageView();

				mapTile.setImage(new Image(
						"http://mt3.google.com/vt/v=w2.97&x=" + xm + "&y=" + y
								+ "&z=3"));
				mapTile.setFitWidth(CubeFace.edgeLength / 2);
				mapTile.setFitHeight(CubeFace.edgeLength / 3);
				mapTile.opacityProperty().bind(cubeModel.mapOpacity);
				tilePane.getChildren().add(mapTile);
			}
		}
		return tilePane;
	}

	Node createBottomMapTiles(int sideNum) {
		TilePane tilePane;
		int xOffset = (sideNum + 1) * 2;
		tilePane = new TilePane();
		tilePane.setRotate(sideNum * 90 % 360);
		tilePane.setPrefColumns(2);
		tilePane.setPrefRows(3);

		PerspectiveTransform build = new SimplePerspectiveTransformBuilder().ulx(0).uly(0)
				.urx(edgeLength).ury(0).llx(edgeLength * 0.375)
				.lly(edgeLength * 0.375).lrx(edgeLength * 0.625)
				.lry(edgeLength * 0.375).build();
		tilePane.setEffect(build);

		for (int y = 5; y <= 7; y++) {
			for (int x = xOffset; x <= xOffset + 1; x++) {
				int xm = (x + 1) % 8;
				ImageView mapTile = new ImageView();
				mapTile.setImage(new Image(
						"http://mt3.google.com/vt/v=w2.97&x=" + xm + "&y=" + y
								+ "&z=3"));
				mapTile.setFitWidth(CubeFace.edgeLength / 2);
				mapTile.setFitHeight(CubeFace.edgeLength / 3);
				mapTile.opacityProperty().bind(cubeModel.mapOpacity);
				tilePane.getChildren().add(mapTile);
			}
		}
		return tilePane;
	}

	Paint computeFaceHSB() {
		Paint color = Color.hsb(FACE_HUE, FACE_SAT,
				Math.abs(-zPos.getValue() / (radius * 2)) + 0.40);
		return color;
	}
}
