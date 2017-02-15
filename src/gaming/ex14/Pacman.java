package gaming.ex14;

import javafx.animation.Animation;
import javafx.animation.Animation.Status;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import simplebuilder.SimpleTimelineBuilder;

public class Pacman extends Arc {
	public enum PacmanDirection {
		DOWN(90), LEFT(180), RIGHT(0), UP(270);
		private final int angle;

		private PacmanDirection(int angle) {
			this.angle = angle;
		}

		public int getAngle() {
			return angle;
		}
	}

	private PacmanDirection direction = PacmanDirection.RIGHT;
	private Timeline eatingAnimation = new SimpleTimelineBuilder()
			.keyFrames(new KeyFrame(Duration.ZERO, new KeyValue(startAngleProperty(), 45.0f)),
					new KeyFrame(Duration.ZERO, new KeyValue(lengthProperty(), 270.0f)),
					new KeyFrame(Duration.seconds(0.25), new KeyValue(startAngleProperty(), 0.0f)),
					new KeyFrame(Duration.seconds(0.25), new KeyValue(lengthProperty(), 360.0f)))
			.cycleCount(Animation.INDEFINITE).autoReverse(true).build();
	public Pacman() {
		setFill(Color.YELLOW);
		setRadiusX(15.0f);
		setRadiusY(15.0f);
		setStartAngle(45.0f);
		setLength(270.0f);
		setType(ArcType.ROUND);
		eatingAnimation.playFromStart();
	}

	private boolean checkCollision(ObservableList<Node> observableList) {
		return observableList.stream().filter(Rectangle.class::isInstance)
				.anyMatch(p -> p.getBoundsInParent().intersects(getBoundsInParent()));
	}

	public void move(ObservableList<Node> observableList) {
		int step = 2;
		if (direction == null) {
			return;
		}

		switch (direction) {
		case RIGHT:
			setLayoutX(getLayoutX() + step);
			if (checkCollision(observableList)) {
				setLayoutX(getLayoutX() - 2 * step);
			}
			break;
		case UP:
			setLayoutY(getLayoutY() - step);
			if (checkCollision(observableList)) {
				setLayoutY(getLayoutY() + 2 * step);
			}
			break;
		case DOWN:
			setLayoutY(getLayoutY() + step);
			if (checkCollision(observableList)) {
				setLayoutY(getLayoutY() - 2 * step);
			}
			break;
		case LEFT:
			setLayoutX(getLayoutX() - step);
			if (checkCollision(observableList)) {
				setLayoutX(getLayoutX() + 2 * step);
			}
			break;
		default:
			break;
		}
	}

	public void turn(PacmanDirection direction) {
		if (eatingAnimation.getStatus() == Status.RUNNING) {
			this.direction = direction;
			if (direction != null) {
				setRotate(direction.angle);
			}
		}
	}

	public void die() {
		if (eatingAnimation.getStatus() == Status.RUNNING) {
			turn(null);
			eatingAnimation.stop();
			Timeline timeline = new Timeline(new KeyFrame(Duration.ZERO, new KeyValue(startAngleProperty(), 45.0f)),
					new KeyFrame(Duration.ZERO, new KeyValue(lengthProperty(), 270.0f)),
					new KeyFrame(Duration.seconds(2), new KeyValue(startAngleProperty(), 180.0f)),
					new KeyFrame(Duration.seconds(2), new KeyValue(lengthProperty(), 0.0f)));
			timeline.play();
			timeline.setOnFinished(e -> {
				setLayoutX(PacmanModel.SQUARE_SIZE / 2);
				setLayoutY(PacmanModel.SQUARE_SIZE / 2);
				eatingAnimation.play();
			});
		}

	}

}
