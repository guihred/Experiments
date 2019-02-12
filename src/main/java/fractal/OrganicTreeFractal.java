package fractal;

import java.util.Random;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.StrokeLineCap;

public class OrganicTreeFractal extends Canvas {

    private static final double SIZE = 800;
    private final DoubleProperty thickness = new SimpleDoubleProperty(0.75);
    private final DoubleProperty ratio = new SimpleDoubleProperty(0.75);
    private final DoubleProperty deltaAngle = new SimpleDoubleProperty(Math.PI / 12);
    private final DoubleProperty initialRadius = new SimpleDoubleProperty(100);

    Random random = new Random();

    public OrganicTreeFractal() {
        super(SIZE, SIZE);
        deltaAngle.addListener(e -> drawTree());
        initialRadius.addListener(e -> drawTree());
        ratio.addListener(e -> drawTree());
        thickness.addListener(e -> drawTree());
        drawTree();
    }

    public DoubleProperty deltaAngleProperty() {
        return deltaAngle;
    }

    public void drawTree() {
        GraphicsContext gc = getGraphicsContext2D();
        gc.clearRect(0, 0, SIZE, SIZE);
        drawBranch(gc, SIZE / 2, SIZE, initialRadius.get(), Math.PI, 1);
    }

    public DoubleProperty initialRadiusProperty() {
        return initialRadius;
    }

    public DoubleProperty ratioProperty() {
        return ratio;
    }
    public DoubleProperty thicknessProperty() {
        return thickness;
    }

    private void drawBranch(GraphicsContext gc, double x0, double y0, double radius, double angle, int i) {
        gc.moveTo(x0, y0);
        double y = Math.cos(angle) * radius;
        double x = Math.sin(angle) * radius;
        gc.setLineWidth(radius * thicknessProperty().get());
        gc.setLineCap(StrokeLineCap.ROUND);

        gc.strokeLine(x0, y0, x0 + x, y0 + y);
        if (radius > 10) {
            double d = deltaAngle.get();
            drawBranch(gc, x0 + x, y0 + y, radius * ratioProperty().get(), angle - d, ++i);
            if (i % 2 == 0) {
                drawBranch(gc, x0 + x, y0 + y, radius * ratioProperty().get(), angle + d, ++i);
            }
        }

    }

}
