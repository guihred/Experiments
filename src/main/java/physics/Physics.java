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
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.jbox2d.common.Vec2;

/**
 *
 * @author wayne
 */
public class Physics extends Application {

    public static final int PHYSICAL_WIDTH = 100;
    public static final int PHYSICAL_HEIGHT = 100;
    public static final int MAX_BALLS = 10;
    private final Random random = new Random();

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("JavaFX + Box2D");
        Group root = new Group();
        PhysicalScene scene = new PhysicalScene(root, BasePhysicalObject.WIDTH, BasePhysicalObject.HEIGHT);

        final Ball[] ball = new Ball[MAX_BALLS];
        for (int i = 0; i < MAX_BALLS; i++) {
            ball[i] = new Ball(random.nextInt(PHYSICAL_WIDTH), random.nextInt(PHYSICAL_WIDTH));
        }

        final Ramp leftNet = new Ramp(0, 90, 40, 85);
        final Ramp rightNet = new Ramp(60, 85, PHYSICAL_WIDTH, 90);

        final GroundObject ground = new GroundObject();

        final Wall leftWall = new Wall(0, 0, 1, PHYSICAL_HEIGHT);
        final Wall rightWall = new Wall(99, 0, 1, PHYSICAL_HEIGHT);

        final Ramp topRamp = new Ramp(25, 75, 55, 70);
        final Ramp middleRamp = new Ramp(50, 50, 80, 60);
        final Ramp bottomRamp = new Ramp(20, 45, 60, 30);
        final Ramp lastRamp = new Ramp(50, 20, 80, 30);
        final Timeline timeline = new Timeline();
        timeline.setCycleCount(Animation.INDEFINITE);

        // create a keyFrame, the keyValue is reached at time 2s
        Duration duration = Duration.seconds(1.0 / 60.0);
        // one can add a specific action when the keyframe is reached
        scene.setOnKeyReleased(e -> {
            KeyCode code = e.getCode();
            if (code == KeyCode.ENTER) {
                for (int i = 0; i < MAX_BALLS; i++) {
                    ball[i].body.applyLinearImpulse(new Vec2(0, 20), new Vec2(0, 0));
                }
            }
        });
        EventHandler<ActionEvent> onFinished = t -> {
            PhysicalScene.getWorld().step(1.0F / 60.F, 1, 1);
            for (int i = 0; i < MAX_BALLS; i++) {
                float xpos = BasePhysicalObject.toPixelX(ball[i].body.getPosition().x);
                float ypos = BasePhysicalObject.toPixelY(ball[i].body.getPosition().y);
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

    }

    public static void main(String[] args) {
        Application.launch(args);
    }
}
