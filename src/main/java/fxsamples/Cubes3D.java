package fxsamples;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * @author Jasper Potts
 */
public class Cubes3D extends Application {

	@Override
	public void start(Stage stage) throws Exception {
		stage.setTitle("Cube 3D");

		Cube c = new Cube(50, Color.RED, 1);
		c.rx.setAngle(45);
		c.ry.setAngle(45);
		Cube c2 = new Cube(50, Color.GREEN, 1);
		c2.setTranslateX(100);
		c2.rx.setAngle(45);
		c2.ry.setAngle(45);
		Cube c3 = new Cube(50, Color.ORANGE, 1);
		c3.setTranslateX(-100);
		c3.rx.setAngle(45);
		c3.ry.setAngle(45);
		Timeline animation = new Timeline();
		animation.getKeyFrames().addAll(
				new KeyFrame(Duration.ZERO, 
    			        new KeyValue(c.ry.angleProperty(),0D), 
    			        new KeyValue(c2.rx.angleProperty(), 0D),
    					new KeyValue(c3.rz.angleProperty(), 0D)),
				new KeyFrame(Duration.millis(1000), 
				        new KeyValue(c.ry.angleProperty(), 360D), 
				        new KeyValue(c2.rx.angleProperty(), 360D), 
				        new KeyValue(c3.rz.angleProperty(), 360D)));
		animation.setCycleCount(Animation.INDEFINITE);
		// create root group
		Group root = new Group(c, c2, c3);
		// translate and rotate group so that origin is center and +Y is up
        root.setTranslateX(200);
        root.setTranslateY(75);
		root.getTransforms().add(new Rotate(180, Rotate.X_AXIS));
		// create scene
		Scene scene = new Scene(root, 400, 150);
		scene.setCamera(new PerspectiveCamera());
		stage.setScene(scene);
		stage.show();
		// start spining animation
		animation.play();
	}

	public static void main(String[] args) {
		launch(args);
	}
}