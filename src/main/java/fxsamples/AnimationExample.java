package fxsamples;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.shape.FillRule;
import javafx.stage.Stage;
import utils.ResourceFXUtils;

public class AnimationExample extends Application {

	private static final String EARTH_PATH = "m-7 -7.50 a 14 15 8.2 1 1 -0.02 0.04m3.76 4.70 "
			+ "c1.76 -1 6 0.76 5.76 2.26 "
			+ "c3.50 2.76 3.50 2 3 3 c-2.50 3.50 -3.26 4.26 -4 4.26"
			+ " c-1.26 2.76 -1.26 4.50 -1.26 4.50 c-1.76 -1 -2.26 -7.50 -2.76 -7.26"
			+ " c-1.26 -2.26 -0.76 -5.50 -1.50 -6.50 c-1.50 -2.50 -1.50 -3 -3 -4.26"
			+ " m3.76 4 c-0.76 -1.50 -0.50 -0.76 -1 -3 c-1.50 -0.76 -0.76 -2 0 -2.76"
			+ " c0.76 -0.50 1.76 1.26 2.26 1.76 c1 -3.50 2 -4 4.76 -4.26"
			+ " c0.76 -1.50 0.76 -1.50 -0.26 -2.50 c-1.50 1.50 -1.50 1.50 -1.26 0.76"
			+ " c-1.26 0 -1.26 0 0.50 -1.76 c2.26 0.50 2.26 0.50 2 -0.50"
			+ " c1.50 -0.50 0 2.50 2 2.26 c1.26 -2.26 1.26 -2.76 2.76 -2.26"
			+ " c2 0 1.76 1.26 2 0.50 c0.50 1.50 0.50 1.50 0 1.26"
			+ " c-1.26 1.76 -1.26 1.76 -0.26 1.50 c0.50 1.50 0.50 1.50 0.26 0.50"
			+ " c0.76 -1 1.50 -1 1.76 -1.50 c-0.50 2.50 -0.50 2.50 0.26 1.26"
			+ " c-1.76 1.76 -1.76 3.50 -2 1.76 c0.26 0.50 0.50 1.76 0.26 1.26"
			+ " c2.76 -0.26 2.50 0 2.26 0.26 c0 -1.50 0 -1.50 0 -1.76"
			+ " c-2.26 4 -2.26 4 -2 2.76 c-1 3 -0.26 6 -0.76 2.50 c0 2.76 0 3 0.50 3.26"
			+ " c3.76 0.26 3.76 0.26 4.26 3.50 c0 2 0 2 -0.50 2.76"
			+ " c-0.76 3.26 -0.76 3.26 -0.50 3.76 c1.76 0 1.76 0 1.50 -0.26"
			+ " c0.76 0.50 0.76 0.50 -3.50 4.26 c-5 0.50 -6.50 1 -6.26 0.76"
			+ " c-1.50 0.50 -2.26 0 -3.26 -0.50 c-2 0.76 -2 1.26 -2.26 0.26"
			+ " c-1.76 0 -5.50 -3 -8 -8 c-1 -7.26 -0.76 -7.50 0.50 -12.50 ";
	private static final String SUN_PATH = " m-16 -15 a 32 30 45 1 1 -0.08 0.16l-4.96 8.96 l-24 -27.04 l32 12 l2 -22"
			+ " l14 15.04 l13.04 -31.04 l5.04 32 l22 -9.04 l-7.04 21.04"
			+ " l31.04 -11.04 l-26 24 l20 3.04 l-20 8 l29.04 16 l-33.04 -4"
			+ " l12 17.04 l-21.04 -10 l8 33.04 l-20 -27.04 l-6 18"
			+ " l-7.04 -19.04 l-22 25.04 l10 -30 l-18 5.04 l12 -14 l-38 6"
			+ " l28 -19.04 l-16 -8 l17.04 -3.04 m27.04 38";

	@Override
	public void start(Stage theStage) {
		theStage.setTitle("Animation Example");

		Group root = new Group();
		Scene theScene = new Scene(root);
		theStage.setScene(theScene);

		final int canvasSize = 512;
		Canvas canvas = new Canvas(canvasSize, canvasSize);
		root.getChildren().add(canvas);

		GraphicsContext gc = canvas.getGraphicsContext2D();
		Image space = new Image(ResourceFXUtils.toExternalForm("space.jpg"));

		final long startNanoTime = System.nanoTime();

		new AnimationTimer() {
			@Override
			public void handle(long currentNanoTime) {
				double t = (currentNanoTime - startNanoTime) / 1e9;

				final int sunPosition = canvasSize / 2;
				final int radius = sunPosition / 2;
				double x = sunPosition + radius * Math.cos(t);
				double y = sunPosition + radius * Math.sin(t);
				// background image clears canvas
				gc.drawImage(space, 0, 0);
				gc.setFillRule(FillRule.EVEN_ODD);
				drawSVG(gc, x, y, "m-7 -7.50 a 14 15 8.2 1 1 -0.02 0.04", Color.BLUE);
				drawSVG(gc, x, y, EARTH_PATH, Color.GREEN);
				drawSVG(gc, sunPosition, sunPosition, SUN_PATH, Color.ORANGE);
				drawSVG(gc, sunPosition, sunPosition, " m-16 -15a 32 30 45 1 1 -0.08 0.16", Color.YELLOW);
			}

		}.start();

		theStage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}

	private static void drawSVG(GraphicsContext gc, double x, double y, String svgpath, Color orange) {
		gc.beginPath();
		gc.moveTo(x, y);
		gc.setFill(orange);
		gc.appendSVGPath(svgpath);
		gc.fill();
		gc.closePath();
	}

}