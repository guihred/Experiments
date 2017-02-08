package physics;

import java.util.Random;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 *
 * @author wayne
 */
public class Physics extends Application {

	public static final int WIDTH = 600;
	public static final int HEIGHT = 600;
	public static final int PHYSICAL_WIDTH = 100;
	public static final int PHYSICAL_HEIGHT = 100;
	public static final int MAX_BALLS = 200;

	public static void main(String[] args) {
		Application.launch(args);
	}

	@Override
	public void start(Stage primaryStage) {
		primaryStage.setTitle("JavaFX + Box2D");
		Group root = new Group();
		PhysicalScene scene = new PhysicalScene(root, WIDTH, HEIGHT);

		final Ball[] ball = new Ball[MAX_BALLS];
		// new Ball(20,90);
		Random r = new Random(System.currentTimeMillis());
		for (int i = 0; i < MAX_BALLS; i++) {
			ball[i] = new Ball(r.nextInt(100), 95);
		}

		final Ramp leftNet = new Ramp(0, 90, 40, 85);
		final Ramp rightNet = new Ramp(60, 85, 100, 90);

		final Ground ground = new Ground();

		final Wall leftWall = new Wall(0, 0, 1, 100);
		final Wall rightWall = new Wall(99, 0, 1, 100);

		final Ramp topRamp = new Ramp(25, 75, 55, 70);
		final Ramp middleRamp = new Ramp(50, 50, 80, 60);
		final Ramp bottomRamp = new Ramp(20, 45, 60, 30);
		final Ramp lastRamp = new Ramp(50, 20, 80, 30);
		final Timeline timeline = new Timeline();
		timeline.setCycleCount(Animation.INDEFINITE);
		// timeline.setDelay(Duration.seconds(1.0/60.0));

		// create a keyFrame, the keyValue is reached at time 2s
		Duration duration = Duration.seconds(1.0 / 60.0);
		// one can add a specific action when the keyframe is reached
		EventHandler<ActionEvent> onFinished = t -> {
			PhysicalScene.world.step(1.0F / 60.f, 1, 1);
			for (int i = 0; i < MAX_BALLS; i++) {
				float xpos = toPixelX(ball[i].body.getPosition().x);
				float ypos = toPixelY(ball[i].body.getPosition().y);
				ball[i].node.setLayoutX(xpos);
				ball[i].node.setLayoutY(ypos);
			}
		};

		KeyFrame keyFrame = new KeyFrame(duration, onFinished, null, null);

		// add the keyframe to the timeline
		timeline.getKeyFrames().add(keyFrame);

		Button btn = new Button();
		btn.setLayoutX(10);
		btn.setLayoutY(0);
		btn.setText("Release Balls");
		btn.setOnAction(event -> timeline.playFromStart());

		root.getChildren().add(btn);
		for (int i = 0; i < MAX_BALLS; i++) {
			root.getChildren().add(ball[i].node);
		}
		root.getChildren().add(ground.node);
		root.getChildren().add(leftNet.node);
		root.getChildren().add(rightNet.node);
		root.getChildren().add(leftWall.node);
		root.getChildren().add(rightWall.node);
		root.getChildren().add(topRamp.node);
		root.getChildren().add(middleRamp.node);
		root.getChildren().add(bottomRamp.node);
		root.getChildren().add(lastRamp.node);
		primaryStage.setScene(scene);
		primaryStage.show();
		// timeline.setDelay(Duration.seconds(2));
		// timeline.playFromStart();

	}

	/*
	 * JavaFX Coordinates: (0,0) --> (WIDTH,HEIGHT) in pixels World Coordinates:
	 * (0,100) --> (100, 0) in meters
	 */
	public static int toPixelX(float worldX) {
		float x = WIDTH * worldX / 100.0F;
		return (int) x;
	}

	public static int toPixelY(float worldY) {
		float y = HEIGHT - 1.0F * HEIGHT * worldY / 100.0F;
		return (int) y;
	}

	public static float toPixelWidth(float worldWidth) {
		return WIDTH * worldWidth / 100.0F;
	}

	public static float toPixelHeight(float worldHeight) {
		return HEIGHT * worldHeight / 100.0F;
	}
}
