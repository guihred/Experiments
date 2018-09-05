package fxsamples;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import simplebuilder.ResourceFXUtils;

public class AnimationExample extends Application {

	@Override
	public void start(Stage theStage) {
        theStage.setTitle("Animation Example");

		Group root = new Group();
		Scene theScene = new Scene(root);
		theStage.setScene(theScene);

		Canvas canvas = new Canvas(512, 512);
		root.getChildren().add(canvas);

		GraphicsContext gc = canvas.getGraphicsContext2D();
		Image earth = new Image(ResourceFXUtils.toExternalForm("earth.png"));
		Image sun = new Image(ResourceFXUtils.toExternalForm("sun.png"));
		Image space = new Image(ResourceFXUtils.toExternalForm("space.jpg"));

		final long startNanoTime = System.nanoTime();

		new AnimationTimer() {
			@Override
			public void handle(long currentNanoTime) {
				double t = (currentNanoTime - startNanoTime) / 1000000000.0;

				double x = 232 + 128 * Math.cos(t);
				double y = 232 + 128 * Math.sin(t);

				// background image clears canvas
				gc.drawImage(space, 0, 0);
				gc.drawImage(earth, x, y);
				gc.drawImage(sun, 196, 196);
			}
		}.start();

		theStage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}

}