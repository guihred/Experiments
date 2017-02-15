package gaming.ex14;

import java.util.Random;
import java.util.stream.Stream;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
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
	public enum GhostColor {
		RED(Color.RED), BLUE(Color.BLUE), ORANGE(Color.ORANGE), GREEN(Color.GREEN);

		private final transient Color color;

		private GhostColor(Color color) {
			this.color = color;
		}

		public Color getColor() {
			return color;
		}
	}

	public enum GhostDirection {
		EAST(1, 0), NORTH(0, 2), SOUTH(0, -2), WEST(-1, 0), NORTHEAST(1, 1), SOUTHEAST(1, -1), NORTHWEST(-1,
				1), SOUTHWEST(-1, -1);
		private final int x;
		private final int y;

		private GhostDirection(int x, int y) {
			this.x = x;
			this.y = y;
		}
	}

	public enum GhostStatus {
		ALIVE, AFRAID, DEAD;
	}

	private ObjectProperty<GhostStatus> status = new SimpleObjectProperty<>(GhostStatus.ALIVE);
	private GhostDirection direction = GhostDirection.NORTH;
	private Circle leftEye = new Circle(2);
	private Circle rightEye = new Circle(2);
	private double startX;
	private double startY;

	public PacmanGhost(GhostColor color) {
		Polygon polygon = new Polygon();
		for (int i = 180; i <= 360; i += 5) {
			double x = Math.cos(Math.toRadians(i)) * 12;
			double y = Math.sin(Math.toRadians(i)) * 12;
			polygon.getPoints().addAll(x, y);
		}

		polygon.setFill(color.color);
		polygon.fillProperty()
				.bind(Bindings.when(status.isEqualTo(GhostStatus.ALIVE)).then(color.color)
						.otherwise(Bindings.when(status.isEqualTo(GhostStatus.AFRAID)).then(Color.BLUEVIOLET)
								.otherwise(Color.TRANSPARENT)));
		polygon.getPoints().addAll(-12D, 0D, -12D, 20D, -8D, 10D, -4D, 20D, 0D, 10D, 4D, 20D, 8D, 10D, 12D, 20D, 12D,
				0D);

		Ellipse ellipse = new Ellipse(4, 6);
		ellipse.setFill(Color.WHITE);
		ellipse.setLayoutX(-5);
		Ellipse ellipse2 = new Ellipse(4, 6);
		ellipse2.setFill(Color.WHITE);
		ellipse2.setLayoutX(5);
		ellipse.fillProperty().bind(
				Bindings.when(status.isEqualTo(GhostStatus.AFRAID)).then(Color.TRANSPARENT).otherwise(Color.WHITE));
		ellipse2.fillProperty().bind(
				Bindings.when(status.isEqualTo(GhostStatus.AFRAID)).then(Color.TRANSPARENT).otherwise(Color.WHITE));

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
		if (status.get() == GhostStatus.ALIVE) {
			shortestMovement(now, observableList);
		} else if (status.get() == GhostStatus.DEAD) {
			int step = 1;
			if (startX > getLayoutX()) {
				setLayoutX(getLayoutX() + step);
			} else if (startX < getLayoutX()) {
				setLayoutX(getLayoutX() - step);
			}
			if (startY > getLayoutY()) {
				setLayoutY(getLayoutY() + step);
			} else if (startY < getLayoutY()) {
				setLayoutY(getLayoutY() - step);
			}
			if (Math.abs(startX - getLayoutX()) < 3 && Math.abs(startY - getLayoutY()) < 3) {
				setStatus(GhostStatus.ALIVE);
			}
		} else {
			randomMovement(now, observableList);
		}
	}

	void shortestMovement(long now, ObservableList<Node> otherNodes) {
		Pacman pacman = otherNodes.stream().filter(Pacman.class::isInstance).map(Pacman.class::cast).findFirst()
				.orElse(null);
		if (pacman == null) {
			randomMovement(now, otherNodes);
			return;
		}
		double hx = getLayoutX() - pacman.getLayoutX();
		double hy = getLayoutY() - pacman.getLayoutY();
		final int step = 1;
		addTranslate(step);
		if (checkColision(getBoundsInParent(), otherNodes) || now % 200 == 0) {
			addTranslate(-step);
			setDirection(changeDirection(hx, hy));
			addTranslate(step);
			if (checkColision(getBoundsInParent(), otherNodes)) {
				addTranslate(-step);
				randomMovement(now, otherNodes);
			}

		}
	}

	public void setStartPosition(double startX, double startY) {
		setLayoutX(startX);
		setLayoutY(startY);
		this.startX = startX;
		this.startY = startY;
	}

	private GhostDirection changeDirection(double hx, double hy) {
		if (Math.abs(Math.abs(hx) - Math.abs(hy)) < 30) {
			if (hx > 0) {
				return hy < 0 ? GhostDirection.NORTHWEST : GhostDirection.SOUTHWEST;
			}
			return hy > 0 ? GhostDirection.SOUTHEAST : GhostDirection.NORTHEAST;
		}
		if (Math.abs(hx) > Math.abs(hy)) {
			return hx > 0 ? GhostDirection.WEST : GhostDirection.EAST;
		}
		return hy < 0 ? GhostDirection.NORTH : GhostDirection.SOUTH;
	}

	private void addTranslate(final int step) {
		if (getDirection() == GhostDirection.NORTH) {// NORTH
			setLayoutY(getLayoutY() + step);
		}
		if (getDirection() == GhostDirection.WEST) {// WEST
			setLayoutX(getLayoutX() - step);
		}
		if (getDirection() == GhostDirection.SOUTH) {// SOUTH
			setLayoutY(getLayoutY() - step);
		}
		if (getDirection() == GhostDirection.EAST) {// EAST
			setLayoutX(getLayoutX() + step);
		}
		if (getDirection() == GhostDirection.NORTHEAST) {// WEST
			setLayoutY(getLayoutY() + step);
			setLayoutX(getLayoutX() + step);
		}
		if (getDirection() == GhostDirection.SOUTHEAST) {// WEST
			setLayoutY(getLayoutY() - step);
			setLayoutX(getLayoutX() + step);
		}
		if (getDirection() == GhostDirection.SOUTHWEST) {// WEST
			setLayoutY(getLayoutY() - step);
			setLayoutX(getLayoutX() - step);
		}
		if (getDirection() == GhostDirection.NORTHWEST) {// WEST
			setLayoutY(getLayoutY() + step);
			setLayoutX(getLayoutX() - step);
		}
	}

	private void randomMovement(long now, ObservableList<Node> observableList) {
		final int step = 1;
		GhostDirection[] values = GhostDirection.values();
		addTranslate(step);
		if (checkColision(getBoundsInParent(), observableList)) {
			addTranslate(-step);
			setDirection(values[new Random().nextInt(values.length)]);
		}

		if (now % 500 == 0) {
			setDirection(values[new Random().nextInt(values.length)]);
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

	public final ObjectProperty<GhostStatus> statusProperty() {
		return status;
	}

	public final GhostStatus getStatus() {
		return status.get();
	}

	public final void setStatus(final GhostStatus status) {
		this.status.set(status);
	}

}
