package fractal;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

public class TreeFractal extends Canvas {

    private static final double SIZE = 500;
    private final DoubleProperty ratio = new SimpleDoubleProperty(0.75);
    private final DoubleProperty deltaAngle = new SimpleDoubleProperty(Math.PI / 12);
    private final DoubleProperty initialRadius = new SimpleDoubleProperty(100);

    public TreeFractal() {
        super(SIZE, SIZE);
        deltaAngle.addListener(e -> drawTree());
        initialRadius.addListener(e -> drawTree());
        ratio.addListener(e -> drawTree());
        drawTree();
    }

    public DoubleProperty deltaAngleProperty() {
        return deltaAngle;
    }

    public DoubleProperty initialRadiusProperty() {
        return initialRadius;
    }

    public DoubleProperty ratioProperty() {
        return ratio;
    }

    private void drawBranch(GraphicsContext gc, double x0, double y0, double radius, double angle) {
        double y = Math.cos(angle) * radius;
        double x = Math.sin(angle) * radius;
        gc.strokeLine(x0, y0, x0 + x, y0 + y);
        if (radius > 10) {
            drawBranch(gc, x0 + x, y0 + y, radius * ratioProperty().get(), angle + deltaAngle.get());
            drawBranch(gc, x0 + x, y0 + y, radius * ratioProperty().get(), angle - deltaAngle.get());
        }

    }

    private final void drawTree() {
        GraphicsContext gc = getGraphicsContext2D();
        gc.clearRect(0, 0, SIZE, SIZE);
        drawBranch(gc, SIZE / 2, SIZE, initialRadius.get(), Math.PI);
    }

}
