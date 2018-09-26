package gaming.ex13;

import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

import java.util.function.Supplier;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Shape;

public enum SolitaireSuit {
	SPADES(Color.BLACK, () -> Shape.union(getCircles(getTriangle(8, -PI / 2, -4), -PI / 2), getTriangle(6, -PI / 2, 8))),
	DIAMONDS(Color.RED, () -> fill(Shape.union(getTriangle(8, PI / 2, 4), getTriangle(8, -PI / 2, -4)),Color.RED)),
	CLUBS(Color.BLACK, () -> getCircles(getTriangle(6, -PI / 2, 8), -PI / 2)),
	HEARTS(Color.RED, () -> fill(getCircles(getTriangle(8, PI / 2, 4), -PI / 6),Color.RED)),
	;

	private final transient Supplier<Shape> creator;
	private final transient Color color;

    SolitaireSuit(Color color, Supplier<Shape> creator) {
		this.color = color;
		this.creator = creator;
	}

	public Color getColor() {
		return color;
	}

	public Shape getShape() {
		return creator.get();
	}
	private static Shape fill(Shape s, Color angle) {
		s.setFill(angle);
		return s;
	}
	private static Shape getCircles(Shape s, double angle) {
		Shape shape = s;
		for (int i = 0; i < 3; i++) {
			double a = 4 * cos(i * 2 * PI / 3 + angle);
			double b = 4 * sin(i * 2 * PI / 3 + angle);
			Circle circle = new Circle(a, b, 4);
			shape = Shape.union(shape, circle);
		}
		return shape;
	}

	private static Polygon getTriangle(double size, double angle, double y) {
		Polygon polygon = new Polygon();
		for (int i = 0; i < 3; i++) {
			double a = size * cos(i * 2 * PI / 3 + angle);
			double b = size * sin(i * 2 * PI / 3 + angle);
			polygon.getPoints().add(a);
			polygon.getPoints().add(b + y);
		}
		return polygon;
	}
}
