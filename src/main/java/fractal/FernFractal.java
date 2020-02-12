package fractal;

import java.util.Random;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class FernFractal extends Canvas {

    private static final double RIGHT_LEAF = 0.93;

    private static final double LEFT_LEAF = 0.86;

    private static final double STEM_LIMIT = 0.01;

    public static final double SIZE = 100;

    protected static final double[][] MATRIX_X = {
            { 0, 0 },
            { 0.85, 0.04 },
            { 0.2, -0.26 },
            { -0.15, 0.28 }
            
    };
    protected static final double[][] MATRIX_Y = {
            { 0, 0.16 },
            { -0.04, 0.85, 1.6 },
            { 0.23, 0.22, 1.6 },
            { 0.26, 0.24, 0.44 }
    };

    private double x;
    private double y;

    private DoubleProperty coef = new SimpleDoubleProperty(0);

    private final DoubleProperty scale = new SimpleDoubleProperty(SIZE);


    private final GraphicsContext gc = getGraphicsContext2D();

    private final Random random = new Random();

    private final IntegerProperty limit = new SimpleIntegerProperty(10000);

    public FernFractal() {
        super(SIZE, SIZE);
        limit.addListener(e -> draw());
        scale.addListener(e -> {
            setWidth(scale.get());
            setHeight(scale.get());
            draw();
        });
        coef.addListener(e -> draw());
        draw();
    }
    public DoubleProperty coefProperty () {
        return coef;
    }

    // creating canvas
    /* iterate the plotting and calculation
    functions over a loop */
	public final void draw() {
        gc.clearRect(0, 0, getWidth(), getHeight());
        for (int i = 0; i < limit.get(); i++) {
            drawPoint();
            nextPoint();
        }
    }

    public IntegerProperty limitProperty() {
        return limit;
    }

    public DoubleProperty scaleProperty() {
        return scale;
    }

    /* setting stroke,  mapping canvas and then
    plotting the points */
    private void drawPoint() {
        gc.setFill(Color.GREEN);
        double px = map(x, -3, 3, 0, scale.get());
        double py = map(y, 0, 10, scale.get(), 0);
        point(px, py);
    }
    /* algorithm for calculating value of (n+1)th
    term of x and y based on the transformation
    matrices */
    private void nextPoint() {
        double r = random(1);
        double nextY = nextY(r);
        x = nextX(r);
        y = nextY;
    }
    private double nextX(double r) {
        if (r < STEM_LIMIT) {
            return MATRIX_X[0][0] * x + MATRIX_X[0][1] * y;
        }
        if (r < LEFT_LEAF) {
            return MATRIX_X[1][0] * x + MATRIX_X[1][1] * y;
        }
        if (r < RIGHT_LEAF) {
            return MATRIX_X[2][0] * x + MATRIX_X[2][1] * y;

        }
        return MATRIX_X[3][0] * x + MATRIX_X[3][1] * y;
    }
    private double nextY(double r) {
        if (r < STEM_LIMIT) {
            return MATRIX_Y[0][0] * x + MATRIX_Y[0][1] * y;
        }
        if (r < LEFT_LEAF) {
            return MATRIX_Y[1][0] * x + MATRIX_Y[1][1] * y+MATRIX_Y[1][2];
        }
        if (r < RIGHT_LEAF) {
            return MATRIX_Y[2][0] * x + MATRIX_Y[2][1] * y+MATRIX_Y[2][2];
        }
        return MATRIX_Y[3][0] * x + MATRIX_Y[3][1] * y + MATRIX_Y[3][2];
    }

    private void point(double px, double py) {
        gc.fillOval(px, py, 1, 1);
    }

    private double random(int i) {
        return random.nextDouble() * i;
    }

    private static double map(double value, double start1, double stop1, double start2, double stop2) {
        return (value - start1) / (stop1 - start1) * (stop2 - start2) + start2;
    }
}
