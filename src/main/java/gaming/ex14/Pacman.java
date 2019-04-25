package gaming.ex14;

import javafx.animation.Animation;
import javafx.animation.Animation.Status;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import simplebuilder.SimpleTimelineBuilder;

public class Pacman extends Arc {
    private static final float ANGLE_LENGTH = 270.0F;
    private static final float RADIUS = 15.0F;
    private static final float START_ANGLE = 45.0F;

    private PacmanDirection direction = PacmanDirection.RIGHT;

    private Timeline eatingAnimation = new SimpleTimelineBuilder()
        .keyFrames(new KeyFrame(Duration.ZERO, new KeyValue(startAngleProperty(), START_ANGLE)),
            new KeyFrame(Duration.ZERO, new KeyValue(lengthProperty(), ANGLE_LENGTH)),
            new KeyFrame(Duration.seconds(1. / 4), new KeyValue(startAngleProperty(), 0.0F)),
            new KeyFrame(Duration.seconds(1. / 4), new KeyValue(lengthProperty(), 360.0F)))
        .cycleCount(Animation.INDEFINITE).autoReverse(true).build();

    public Pacman() {
        setFill(Color.YELLOW);
        setRadiusX(RADIUS);
        setRadiusY(RADIUS);
        setStartAngle(START_ANGLE);
        setLength(ANGLE_LENGTH);
        setType(ArcType.ROUND);
        eatingAnimation.playFromStart();
    }

    public void die() {
        if (eatingAnimation.getStatus() == Status.RUNNING) {
            turn(null);
            eatingAnimation.stop();
            Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(startAngleProperty(), START_ANGLE)),
                new KeyFrame(Duration.ZERO, new KeyValue(lengthProperty(), ANGLE_LENGTH)),
                new KeyFrame(Duration.seconds(2), new KeyValue(startAngleProperty(), 180.0F)),
                new KeyFrame(Duration.seconds(2), new KeyValue(lengthProperty(), 0.0F)));
            timeline.play();
            timeline.setOnFinished(e -> {
                setLayoutX(PacmanModel.SQUARE_SIZE / 2);
                setLayoutY(PacmanModel.SQUARE_SIZE / 2);
                eatingAnimation.play();
            });
        }

    }

    public void move(ObservableList<Node> observableList) {
        if (direction == null) {
            return;
        }

        int step = 2;
        switch (direction) {
            case RIGHT:
                moveSideways(observableList, -step);
                break;
            case UP:
                moveUpAndDown(observableList, step);
                break;
            case DOWN:
                moveUpAndDown(observableList, -step);
                break;
            case LEFT:
                moveSideways(observableList, step);
                break;
            default:
                break;
        }
    }

    @Override
    public String toString() {
        return "Pacman [" + getLayoutX() + "," + getLayoutY() + "]";
    }

    public void turn(PacmanDirection direction1) {
        if (eatingAnimation.getStatus() == Status.RUNNING) {
            direction = direction1;
            if (direction1 != null) {
                setRotate(direction1.angle);
            }
        }
    }

    private boolean checkCollision(ObservableList<Node> observableList) {

        return observableList.stream().filter(Rectangle.class::isInstance).anyMatch(p -> {
            Bounds boundsInParent = getBoundsInParent();

            return p.getBoundsInParent().intersects(boundsInParent.getMinX(), boundsInParent.getMinY(),
                boundsInParent.getWidth(), boundsInParent.getHeight());

        });
    }

    private void moveSideways(ObservableList<Node> observableList, int step) {
        if (!checkCollision(observableList)) {
            setLayoutX(getLayoutX() - step);
            if (checkCollision(observableList)) {
                setLayoutX(getLayoutX() + step);
            }
        }
    }

    private void moveUpAndDown(ObservableList<Node> observableList, int step) {
        if (!checkCollision(observableList)) {
            setLayoutY(getLayoutY() - step);
            if (checkCollision(observableList)) {
                setLayoutY(getLayoutY() + step);
            }
        }
    }

    public enum PacmanDirection {
        DOWN(90),
        LEFT(180),
        RIGHT(0),
        UP(270);
        protected final int angle;

        PacmanDirection(int angle) {
            this.angle = angle;
        }

        public int getAngle() {
            return angle;
        }
    }
}
