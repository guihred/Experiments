package fractal;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

public class ShellFractal extends Canvas {

	private static final double SIZE = 500;
	private DoubleProperty limit = new SimpleDoubleProperty(10);
    private final DoubleProperty deltaAngle = new SimpleDoubleProperty(360);
	private IntegerProperty spirals = new SimpleIntegerProperty(1);

	public ShellFractal() {
		super(SIZE, SIZE);
		limit.addListener(e -> drawSnowflake());
		deltaAngle.addListener(e -> drawSnowflake());
		spirals.addListener(e -> drawSnowflake());
		drawSnowflake();
	}

	public DoubleProperty deltaAngleProperty() {
		return deltaAngle;
	}

	public final void drawSnowflake() {
        final double fullCircle = 360.;
		GraphicsContext gc = getGraphicsContext2D();
		gc.clearRect(0, 0, SIZE, SIZE);
		final double radius = 0.01;
		gc.beginPath();
		for (int i = 0; i < spirals.get(); i++) {
			double dest = SIZE / 2;
			gc.moveTo(dest, dest);
            drawCircle(gc, dest, dest, radius, 0, i * fullCircle / spirals.get() + deltaAngle.get());
		}

		gc.stroke();
		gc.closePath();
	}

	public DoubleProperty limitProperty() {
		return limit;
	}

	public IntegerProperty spiralsProperty() {
		return spirals;
	}

	private void drawCircle(GraphicsContext gc, double x0, double y0, double r, double r0, double angle) {
        final int fullCircle = 360;
        gc.arc(x0, y0, r, r, angle, fullCircle / 4);
		if (r < limit.get()) {
			double x = Math.sin(Math.toRadians(angle)) * r0;
			double y = Math.cos(Math.toRadians(angle)) * r0;
            drawCircle(gc, x0 + x, y0 + y, r + r0, r, (angle + fullCircle / 4.) % fullCircle);
		}
	}

}
