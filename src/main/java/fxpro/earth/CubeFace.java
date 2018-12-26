package fxpro.earth;

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
import simplebuilder.SimplePerspectiveTransformBuilder;
import utils.CrawlerTask;

/**
 * 
 * @author Jim Weaver
 */
public class CubeFace extends Parent {
	private static final double FACE_HUE = 211;
	private static final double FACE_SAT = 0.25;

	public static final int FRONT_FACE = 1;
	public static final int RIGHT_FACE = 2;
	public static final int REAR_FACE = 3;
	public static final int LEFT_FACE = 0;
	public static final int TOP_FACE = 4;
	public static final int BOTTOM_FACE = 5;

	public static final double EDGE_LENGTH = 512;
	public static final double RADIUS = EDGE_LENGTH * 0.5;

	private CubeModel cubeModel = CubeModel.instance;

	private Rectangle faceRect;
	private DoubleProperty zPos = new SimpleDoubleProperty(0);

	public CubeFace(int face) {
		getChildren().addAll(createFaceRectangle(), createMapTiles(face));

		zPos.addListener((ov, oldValue, newValue) -> faceRect
				.setFill(computeFaceHSB()));
	}

	public final Double getZPos() {
		return zPosProperty().getValue();
	}

	public final void setZPos(final double zPos) {
		zPosProperty().set(zPos);
	}

	public final DoubleProperty zPosProperty() {
		return zPos;
	}

	private final Paint computeFaceHSB() {
		return Color.hsb(FACE_HUE, FACE_SAT,
                Math.abs(-zPos.getValue() / (RADIUS * 2)) + 4. / 10);
	}

    private Node createBottomMapTiles(int sideNum) {
		TilePane tilePane;
		int xOffset = (sideNum + 1) * 2;
		tilePane = new TilePane();
		tilePane.setRotate(sideNum * 90 % 360);
		tilePane.setPrefColumns(2);
		tilePane.setPrefRows(3);

        final double proportion = 0.375;
        PerspectiveTransform build = new SimplePerspectiveTransformBuilder().ulx(0).uly(0)
                .urx(EDGE_LENGTH).ury(0).llx(EDGE_LENGTH * proportion).lly(EDGE_LENGTH * proportion)
                .lrx(EDGE_LENGTH * (1 - proportion)).lry(EDGE_LENGTH * proportion).build();
		tilePane.setEffect(build);

		for (int y = 5; y <= 7; y++) {
			loadFace(tilePane, xOffset, y);
		}
		return tilePane;
	}

    private final Rectangle createFaceRectangle() {
		faceRect = new Rectangle();
		faceRect.setWidth(EDGE_LENGTH);
		faceRect.setHeight(EDGE_LENGTH);
		faceRect.setFill(Color.TRANSPARENT);
		return faceRect;
	}

    private 	final Node createMapTiles(int face) {
        CrawlerTask.insertProxyConfig();
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
			mapTile.setFitWidth(CubeFace.EDGE_LENGTH / 4);
			mapTile.setFitHeight(CubeFace.EDGE_LENGTH / 4);
			mapTile.opacityProperty().bind(cubeModel.getMapOpacity());
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
			mapTile.setFitWidth(CubeFace.EDGE_LENGTH / 4);
			mapTile.setFitHeight(CubeFace.EDGE_LENGTH / 4);
			mapTile.opacityProperty().bind(cubeModel.getMapOpacity());
			stackPane.getChildren().add(mapTile);
			return stackPane;
		} else {
			return null;
		}
	}

    private     Node createSideMapTiles(int sideNum) {
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
				mapTile.setFitWidth(CubeFace.EDGE_LENGTH / 2);
				mapTile.setFitHeight(CubeFace.EDGE_LENGTH / 2);
				mapTile.opacityProperty().bind(cubeModel.getMapOpacity());
				tilePane.getChildren().add(mapTile);
			}
		}
		return tilePane;
	}

	private 	Node createTopMapTiles(int sideNum) {
		TilePane tilePane;
		int xOffset = sideNum * 2;
		tilePane = new TilePane();
        final int initialAngle = 450;
        tilePane.setRotate((initialAngle - sideNum * 90) % 360);
		tilePane.setPrefColumns(2);
		tilePane.setPrefRows(3);
        final double proportion = 0.625;
        PerspectiveTransform transform = new SimplePerspectiveTransformBuilder()
                .ulx(EDGE_LENGTH * (1 - proportion)).uly(EDGE_LENGTH * proportion).urx(EDGE_LENGTH * proportion)
                .ury(EDGE_LENGTH * proportion).llx(0)
				.lly(EDGE_LENGTH).lrx(EDGE_LENGTH).lry(EDGE_LENGTH).build();
		tilePane.setEffect(transform);

		for (int y = 0; y <= 2; y++) {
			loadFace(tilePane, xOffset, y);
		}
		return tilePane;
	}

    private void loadFace(TilePane tilePane, int xOffset, int y) {
		for (int x = xOffset; x <= xOffset + 1; x++) {
			int xm = (x + 1) % 8;
			ImageView mapTile = new ImageView();
			mapTile.setImage(new Image(
					"http://mt3.google.com/vt/v=w2.97&x=" + xm + "&y=" + y
							+ "&z=3"));
			mapTile.setFitWidth(CubeFace.EDGE_LENGTH / 2);
			mapTile.setFitHeight(CubeFace.EDGE_LENGTH / 3);
			mapTile.opacityProperty().bind(cubeModel.getMapOpacity());
			tilePane.getChildren().add(mapTile);
		}
	}

}
