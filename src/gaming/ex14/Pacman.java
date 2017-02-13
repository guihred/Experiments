package gaming.ex14;

import javafx.animation.Animation;
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
	public Pacman() {
		setFill(Color.YELLOW);
		setRadiusX(15.0f);
		setRadiusY(15.0f);
		setStartAngle(45.0f);
		setLength(270.0f);
		setType(ArcType.ROUND);
		Timeline timeline = new Timeline(new KeyFrame(Duration.ZERO, new KeyValue(startAngleProperty(), 45.0f)),
				new KeyFrame(Duration.ZERO, new KeyValue(lengthProperty(), 270.0f)),
				new KeyFrame(Duration.seconds(0.25), new KeyValue(startAngleProperty(), 0.0f)),
				new KeyFrame(Duration.seconds(0.25), new KeyValue(lengthProperty(), 360.0f)));
		timeline.setCycleCount(Animation.INDEFINITE);
		timeline.setAutoReverse(true);
		timeline.playFromStart();
	}

	private boolean checkCollision(ObservableList<Node> observableList) {
		boolean anyMatch = observableList.stream().filter(Rectangle.class::isInstance)
				.anyMatch(
				p -> this != p && p.getBoundsInParent().intersects(getBoundsInParent()));
		return anyMatch;
	}

	public void move(long now, ObservableList<Node> observableList) {
		int step = 2;
		if (direction == null) {
			return;
		}

		switch (direction) {
		case RIGHT:
			setLayoutX(getLayoutX() + step);
			if (checkCollision(observableList)) {
				setLayoutX(getLayoutX() - 5);
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
				setLayoutX(getLayoutX() + step);
			}
			break;
		default:
			break;
		}
	}

	public void turn(double angle) {
		setRotate(angle);
	}

	public void turn(PacmanDirection direction) {
		this.direction = direction;
		if (direction != null) {
			setRotate(direction.angle);
		}
	}

}
