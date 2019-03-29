package fractal;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;

public class OrganicTreeFractal extends Canvas {

    private static final double SIZE = 500;
    private final DoubleProperty thickness = new SimpleDoubleProperty(0.25);
    private final DoubleProperty ratio = new SimpleDoubleProperty(0.75);
    private final DoubleProperty deltaAngle = new SimpleDoubleProperty(Math.PI / 8.5);
    private final DoubleProperty initialRadius = new SimpleDoubleProperty(100);
    private final DoubleProperty leaf = new SimpleDoubleProperty(5);
    public OrganicTreeFractal() {
        super(SIZE, SIZE);
        deltaAngle.addListener(e -> drawTree());
        initialRadius.addListener(e -> drawTree());
        ratio.addListener(e -> drawTree());
        thickness.addListener(e -> drawTree());
        leaf.addListener(e -> drawTree());
        drawTree();
    }

    public DoubleProperty deltaAngleProperty() {
        return deltaAngle;
    }

    public void drawTree() {
        GraphicsContext gc = getGraphicsContext2D();
        gc.clearRect(0, 0, SIZE, SIZE);

        List<double[]> leaves = new ArrayList<>();
        gc.setStroke(Color.BLACK);
        drawBranch(gc, SIZE / 2, SIZE, 2, Math.PI, 0, leaves);
        gc.setFill(Color.GREEN);

        gc.setLineWidth(1);
        leaves.forEach(e -> drawLeaf(gc, e));
    }

    public DoubleProperty initialRadiusProperty() {
        return initialRadius;
    }


    public DoubleProperty leafProperty() {
        return leaf;
    }

    public DoubleProperty ratioProperty() {
        return ratio;
    }

    public DoubleProperty thicknessProperty() {
        return thickness;
    }

    private void drawBranch(GraphicsContext gc, double x0, double y0, int r, double angle, int i,
            List<double[]> leaves) {
        double radius = initialRadius.get() * Math.pow(ratio.get(), r);
        double y = Math.cos(angle) * radius;
        double x = Math.sin(angle) * radius;
        gc.moveTo(x0, y0);

        gc.setLineWidth(radius * thickness.get());
        gc.setLineCap(StrokeLineCap.ROUND);
        gc.strokeLine(x0, y0, x0 + x, y0 + y);
        if (radius < 10) {
            leaves.add(new double[] { x0 + x, y0 + y, Math.toDegrees(angle) });
            return;
        }
        double d = deltaAngle.get();
        drawBranch(gc, x0 + x, y0 + y, r + 1, angle + d, i + 1, leaves);
        if (i % 2 == 0) {
            drawBranch(gc, x0 + x, y0 + y, r + 1, angle - d, i + 2, leaves);
        }

    }

	private void drawLeaf(GraphicsContext gc, double[] e) {
        gc.save();
        double h = leaf.get();
        double degrees = e[2];
        Rotate rotate = Transform.rotate(degrees + 180, e[0], e[1]);
        Affine affine = new Affine(rotate);
        gc.fillOval(e[0] - h / 6, e[1] - h / 4, h, h / 2);
        gc.transform(affine);
        gc.restore();
    }

}
