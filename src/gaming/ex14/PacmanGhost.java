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
		EAST(1, 0), NORTH(0, 2), SOUTH(0, -2), WEST(-1, 0);
		private final int x;
		private final int y;

		private GhostDirection(int x, int y) {
			this.x = x;
			this.y = y;
		}
	}

	private GhostDirection direction = GhostDirection.NORTH;
	private Circle leftEye = new Circle(2);
	private Circle rightEye = new Circle(2);

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

	public GhostDirection getDirection() {
		return direction;
	}

	public void move(long now, ObservableList<Node> observableList) {
		shortestMovement(now, observableList);
	}

	private void shortestMovement(long now, ObservableList<Node> observableList) {
		final int STEP = 1;
		Pacman pacman = observableList.stream().filter(Pacman.class::isInstance).map(Pacman.class::cast).findFirst()
				.orElse(null);
		if (pacman == null) {
			randomMovement(now, observableList);
			return;
		}
		double hx = getLayoutX() - pacman.getLayoutX();
		double hy = getLayoutY() - pacman.getLayoutY();
		if (Math.abs(hx) > Math.abs(hy)) {

			if (hx > 0) {
				setDirection(GhostDirection.WEST);
			} else {
				setDirection(GhostDirection.EAST);
			}
		} else {
			if (hy < 0) {
				setDirection(GhostDirection.NORTH);
			} else {
				setDirection(GhostDirection.SOUTH);
			}
		}
		addTranslate(STEP);
		if (checkColision(getBoundsInParent(), observableList)) {
			addTranslate(-STEP);

		}
	}

	private void addTranslate(final int STEP) {
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
	}
	private void randomMovement(long now, ObservableList<Node> observableList) {
		final int STEP = 1;
		addTranslate(STEP);
		if (checkColision(getBoundsInParent(), observableList)) {
			addTranslate(-STEP);
			setDirection(GhostDirection.values()[new Random().nextInt(4)]);
		}

		if (now % 500 == 0) {
			setDirection(GhostDirection.values()[new Random().nextInt(4)]);
		}
	}

	public void setDirection(GhostDirection direction) {
		adjustEyes(-1);
		this.direction = direction;
		adjustEyes(1);

	}

	private void adjustEyes(int mul) {
		rightEye.setLayoutX(rightEye.getLayoutX() + mul * direction.x);
		rightEye.setLayoutY(rightEye.getLayoutY() + mul * direction.y);
		leftEye.setLayoutY(leftEye.getLayoutY() + mul * direction.y);
		leftEye.setLayoutX(leftEye.getLayoutX() + mul * direction.x);
	}

}
