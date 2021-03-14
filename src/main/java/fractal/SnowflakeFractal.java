package fractal;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

public class SnowflakeFractal extends Canvas {

    private static final double DEFAULT_SIZE = 500;
    private DoubleProperty size = new SimpleDoubleProperty(DEFAULT_SIZE);
    private DoubleProperty limit = new SimpleDoubleProperty(2);

    public SnowflakeFractal() {
        super(DEFAULT_SIZE, DEFAULT_SIZE);
        limit.addListener(e -> drawSnowflake());
        size.addListener(e -> drawSnowflake());
        drawSnowflake();
    }

    public DoubleProperty limitProperty() {
        return limit;
    }

    public DoubleProperty sizeProperty() {
        return size;
    }

    private void drawSnow(GraphicsContext gc, double x0, double y0, double radius, double angle) {
        double y = Math.sin(angle) * radius;
        double x = Math.cos(angle) * radius;
        if (Math.log(radius / size.get()) / Math.log(1. / 3) < limit.get()) {
            y /= 3;
            x /= 3;
            drawSnow(gc, x0, y0, radius / 3, angle);
            drawSnow(gc, x0 + x, y0 + y, radius / 3, angle + Math.PI / 3);
            double x2 = Math.cos(angle + Math.PI / 3) * radius / 3;
            double y2 = Math.sin(angle + Math.PI / 3) * radius / 3;
            drawSnow(gc, x0 + x + x2, y0 + y + y2, radius / 3, angle - Math.PI / 3);
            drawSnow(gc, x0 + 2 * x, y0 + 2 * y, radius / 3, angle);
            return;
        }
        gc.strokeLine(x0, y0, x0 + x, y0 + y);

    }

    private final void drawSnowflake() {
        GraphicsContext gc = getGraphicsContext2D();
        gc.clearRect(0, 0, getWidth(), getHeight());
        double radius = size.get() / 3;
        double top = size.get() * Math.sqrt(3) / 3;
        double y = Math.sin(-2 * Math.PI / 3) * radius;
        drawSnow(gc, size.get() / 3, top, radius, 0);
        drawSnow(gc, size.get() * 2 / 3, top, radius, -2 * Math.PI / 3);
        drawSnow(gc, size.get() / 2, top + y, radius, 2 * Math.PI / 3);

    }

}
