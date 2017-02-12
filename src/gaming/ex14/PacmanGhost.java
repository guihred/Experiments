package gaming.ex14;

import java.util.Random;
import java.util.stream.Stream;

import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;

public class PacmanGhost extends Group {
	public enum GhostDirection {
		NORTH(0, 2), WEST(-1, 0), SOUTH(0, -2), EAST(1, 0);
		private final int x;
		private final int y;

		private GhostDirection(int x, int y) {
			this.x = x;
			this.y = y;
		}
	}

	private GhostDirection direction = GhostDirection.NORTH;
	private Circle rightEye = new Circle(2);
	private Circle leftEye = new Circle(2);

	public PacmanGhost(Color color) {
		Polygon polygon = new Polygon();
		for (int i = 180; i <= 360; i += 5) {
			double x = Math.cos(Math.toRadians(i)) * 12;
			double y = Math.sin(Math.toRadians(i)) * 12;
			polygon.getPoints().addAll(x, y);
		}

		polygon.setFill(color);
		polygon.getPoints().addAll(-12d, 0d, -12d, 20d, -8d, 10d, -4d, 20d, 0d, 10d, 4d, 20d, 8d, 10d, 12d, 20d, 12d,
				0d);

		Ellipse ellipse = new Ellipse(4, 6);
		ellipse.setFill(Color.WHITE);
		ellipse.setLayoutX(-5);
		Ellipse ellipse2 = new Ellipse(4, 6);
		ellipse2.setFill(Color.WHITE);
		ellipse2.setLayoutX(5);
		rightEye.setLayoutX(5);
		rightEye.setLayoutY(2);
		leftEye.setLayoutX(-5);
		leftEye.setLayoutY(2);
		getChildren().add(polygon);
		getChildren().add(ellipse);
		getChildren().add(ellipse2);
		getChildren().add(rightEye);
		getChildren().add(leftEye);
	}

	boolean checkColision(Bounds boundsInParent, ObservableList<Node> observableList) {
		Stream<Bounds> walls = observableList.stream().filter(Rectangle.class::isInstance).map(Node::getBoundsInParent);
		return walls.anyMatch(b -> b.intersects(boundsInParent.getMinX(), boundsInParent.getMinY(),
				boundsInParent.getWidth(), boundsInParent.getHeight()));
	}

	public void move(long now, ObservableList<Node> observableList) {
		final int STEP = 1;
		if (getDirection() == GhostDirection.NORTH) {// NORTH
			setTranslateY(getTranslateY() + STEP);
		}
		if (getDirection() == GhostDirection.WEST) {// WEST
			setTranslateX(getTranslateX() - STEP);
		}
		if (getDirection() == GhostDirection.SOUTH) {// SOUTH
			setTranslateY(getTranslateY() - STEP);
		}
		if (getDirection() == GhostDirection.EAST) {// EAST
			setTranslateX(getTranslateX() + STEP);
		}
		if (checkColision(getBoundsInParent(), observableList)

		) {
			if (getDirection() == GhostDirection.NORTH) {// NORTH
				setTranslateY(getTranslateY() - STEP);
			}
			if (getDirection() == GhostDirection.WEST) {// WEST
				setTranslateX(getTranslateX() + STEP);
			}
			if (getDirection() == GhostDirection.SOUTH) {// SOUTH
				setTranslateY(getTranslateY() + STEP);
			}
			if (getDirection() == GhostDirection.EAST) {// EAST
				setTranslateX(getTranslateX() - STEP);
			}
			setDirection(GhostDirection.values()[new Random().nextInt(4)]);

		}

		if (now % 500 == 0) {
			setDirection(GhostDirection.values()[new Random().nextInt(4)]);
		}

	}

	public GhostDirection getDirection() {
		return direction;
	}

	public void setDirection(GhostDirection direction) {
		rightEye.setLayoutX(rightEye.getLayoutX() - this.direction.x);
		rightEye.setLayoutY(rightEye.getLayoutY() - this.direction.y);
		leftEye.setLayoutY(leftEye.getLayoutY() - this.direction.y);
		leftEye.setLayoutX(leftEye.getLayoutX() - this.direction.x);
		this.direction = direction;
		rightEye.setLayoutX(rightEye.getLayoutX() + this.direction.x);
		rightEye.setLayoutY(rightEye.getLayoutY() + this.direction.y);
		leftEye.setLayoutY(leftEye.getLayoutY() + this.direction.y);
		leftEye.setLayoutX(leftEye.getLayoutX() + this.direction.x);

	}

}
