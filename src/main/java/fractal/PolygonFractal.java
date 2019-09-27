package fractal;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

public class PolygonFractal extends Canvas {

    private static final double SIZE = 500;
    private DoubleProperty limit = new SimpleDoubleProperty(10);
    private DoubleProperty ratio = new SimpleDoubleProperty(.5);
    private IntegerProperty spirals = new SimpleIntegerProperty(3);

    public PolygonFractal() {
        super(SIZE, SIZE);
        limit.addListener(e -> drawPolygon());
        spirals.addListener(e -> drawPolygon());
        ratio.addListener(e -> drawPolygon());
        drawPolygon();
    }

	public final void drawPolygon() {
        GraphicsContext gc = getGraphicsContext2D();
        gc.clearRect(0, 0, SIZE, SIZE);
        final double radius = SIZE / 2;
        double off = 0;
        drawPolygon(gc, radius, off);

    }

    public DoubleProperty limitProperty() {
        return limit;
    }

    public DoubleProperty ratioProperty() {
        return ratio;
    }

    public IntegerProperty spiralsProperty() {
        return spirals;
    }

    private void drawPolygon(GraphicsContext gc, final double radius, double off) {
        double ang = Math.PI * 2 / spirals.get();

        int k = spirals.get() / 2 + (spirals.get() + 1) % 2;
        for (int j = 0; j <= spirals.get() % 2; j++) {
            for (int i = 0; i < spirals.get(); i++) {

                double x1 = radius * Math.cos(i * ang + off) + SIZE / 2;
                double y1 = radius * Math.sin(i * ang + off) + SIZE / 2;
                double x2 = radius * Math.cos((i + k) * ang + off) + SIZE / 2;
                double y2 = radius * Math.sin((i + k) * ang + off) + SIZE / 2;
                gc.strokeLine(x1, y1, x2, y2);
            }
        }
        if (radius > limit.get()) {
            double a = 2 * Math.PI / spirals.get() * (k - ratio.get());
            drawPolygon(gc, radius * ratio.get(), off + a);
        }
    }

}
