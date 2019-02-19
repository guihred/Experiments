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
    private DoubleProperty deltaAngle = new SimpleDoubleProperty(360);
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

    public void drawSnowflake() {
        GraphicsContext gc = getGraphicsContext2D();
        gc.clearRect(0, 0, SIZE, SIZE);
        final double radius = 0.01;
        gc.beginPath();
        for (int i = 0; i < spirals.get(); i++) {
            gc.moveTo(SIZE / 2, SIZE / 2);
            drawCircle(gc, SIZE / 2, SIZE / 2, radius, 0, i * 360. / spirals.get() + deltaAngle.get());
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
        gc.arc(x0, y0, r, r, angle, 90);
        if (r < limit.get()) {
            double x = Math.sin(Math.toRadians(angle)) * r0;
            double y = Math.cos(Math.toRadians(angle)) * r0;
            drawCircle(gc, x0 + x, y0 + y, r + r0, r, (angle + 90) % 360);
        }
    }

}
