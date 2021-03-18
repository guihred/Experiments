package ml.graph;

import java.util.*;
import javafx.beans.InvalidationListener;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import ml.data.DataframeML;
import utils.ex.SupplierEx;

public class HeatGraph extends Canvas {
    private static final int CANVAS_SIZE = 550;
    private static final double SQR_ROOT_OF_3 = Math.sqrt(3);
    private static final double MAX_LAYOUT = 480;
    private final DoubleProperty layout = new SimpleDoubleProperty(90);
    private double xProportion;
    private double yProportion;
    private final DoubleProperty lineSize = new SimpleDoubleProperty(20);
    private final ObjectProperty<ColorPattern> colorPattern = new SimpleObjectProperty<>(ColorPattern.HUE);
    private final IntegerProperty bins = new SimpleIntegerProperty(20);
    private final IntegerProperty ybins = new SimpleIntegerProperty(20);
    private DoubleProperty radius = new SimpleDoubleProperty(30);

    private final StringProperty xHeader = new SimpleStringProperty();
    private final StringProperty yHeader = new SimpleStringProperty();
    private final StringProperty zHeader = new SimpleStringProperty();

    private GraphicsContext gc;
    private ObservableMap<String, DoubleSummaryStatistics> stats = FXCollections.observableHashMap();

    private DataframeML data;

    private String title;

    public HeatGraph() {
        super(CANVAS_SIZE, CANVAS_SIZE);
        gc = getGraphicsContext2D();
        InvalidationListener listener = observable -> drawGraph();
        stats.addListener(listener);
        ybins.addListener(listener);
        bins.addListener(listener);
        xHeader.addListener(listener);
        yHeader.addListener(listener);
        zHeader.addListener(listener);
        lineSize.addListener(listener);
        radius.addListener(listener);
        layout.addListener(listener);
        colorPattern.addListener(listener);
        drawGraph();
    }

    public final IntegerProperty binsProperty() {
        return bins;
    }

    public Property<ColorPattern> colorPatternProperty() {
        return colorPattern;
    }

    public final DoubleProperty layoutProperty() {
        return layout;
    }

    public final DoubleProperty lineSizeProperty() {
        return lineSize;
    }

    public final DoubleProperty radiusProperty() {
        return radius;
    }

    public void setDatagram(DataframeML x) {
        data = x;
        data.forEach((col, items) -> stats.put(col, items.stream().filter(Number.class::isInstance)
                .map(Number.class::cast).mapToDouble(Number::doubleValue).summaryStatistics()));
        Iterator<String> iterator = data.cols().iterator();
        if (iterator.hasNext()) {
            xHeader.set(iterator.next());
        }
        if (iterator.hasNext()) {
            yHeader.set(iterator.next());
        }
        if (iterator.hasNext()) {
            zHeader.set(iterator.next());
        }
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ObservableMap<String, DoubleSummaryStatistics> statsProperty() {
        return stats;
    }

    public final StringProperty xHeaderProperty() {
        return xHeader;
    }

    public final IntegerProperty ybinsProperty() {
        return ybins;
    }

    public final StringProperty yHeaderProperty() {
        return yHeader;
    }

    public final StringProperty zHeaderProperty() {
        return zHeader;
    }

    private void drawAxis(DoubleSummaryStatistics xStats, DoubleSummaryStatistics yStats) {

        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFill(Color.BLACK);
        gc.setLineWidth(1);
        gc.setStroke(Color.BLACK);
        gc.fillText(title, layout.get() + (MAX_LAYOUT - layout.get()) / 2, layout.get() - 20);
        double e = layout.get();
        gc.strokeLine(e, e, e, MAX_LAYOUT);
        gc.strokeLine(e, MAX_LAYOUT, MAX_LAYOUT, MAX_LAYOUT);
        double j = (MAX_LAYOUT - e) / bins.get();
        double d = lineSize.get();

        double min = xStats.getMin();

        for (int i = 1; i <= bins.get(); i++) {
            double x1 = i * j + e;
            gc.strokeLine(x1, MAX_LAYOUT, x1, MAX_LAYOUT + 5);
            String xLabel = String.format("%.0f", i * xProportion + min);
            gc.strokeText(xLabel, x1, MAX_LAYOUT + 5 * (4 + 3 * (i % 2)));

        }
        min = yStats.getMin();
        j = (MAX_LAYOUT - e) / ybins.get();
        for (int i = 0; i <= ybins.get(); i++) {
            double y1 = MAX_LAYOUT - i * j;
            gc.strokeLine(e, y1, e - 5, y1);
            String yLabel = String.format("%.1f", i * yProportion + min);
            gc.strokeText(yLabel, e - d * 2, y1);
        }
    }

    private final void drawGraph() {
        DoubleSummaryStatistics yStats = stats.get(yHeader.get());
        DoubleSummaryStatistics xStats = stats.get(xHeader.get());
        if (xStats == null || yStats == null) {
            return;
        }
        gc.clearRect(0, 0, CANVAS_SIZE, CANVAS_SIZE);
        double min = xStats.getMin();
        double max = xStats.getMax();
        xProportion = (max - min) / bins.intValue();
        double min2 = yStats.getMin() - 1. / 10;
        double max2 = yStats.getMax() + 1. / 10;
        yProportion = (max2 - min2) / ybins.intValue();

        gc.setLineWidth(5);
        gc.setFill(Color.GREEN);
        gc.setLineWidth(1. / 2);
        List<Object> entrySetX = data.list(xHeader.get());
        List<Object> entrySetY = data.list(yHeader.get());
        List<Object> entrySetZ = data.list(zHeader.get());
        Map<double[], Double> pointHistogram = new HashMap<>();
        List<double[]> triangles = triangles();
        for (int k = 0; k < data.getSize(); k++) {
            double finalX = finalX(xStats, entrySetX.get(k));
            double finalY = finalY(yStats, entrySetY.get(k));
            double[] orElse = triangles.stream().min(Comparator.comparing(d -> {
                double e = d[0] - finalX;
                double f = d[1] - finalY;
                return e * e + f * f;
            })).orElse(triangles.get(0));
            double finalZ = finalZ(entrySetZ != null ? entrySetZ.get(k) : 1);
            pointHistogram.compute(orElse, (key, v) -> SupplierEx.nonNull(v, 0.) + finalZ);
        }

        DoubleSummaryStatistics stats1 = pointHistogram.values().stream().mapToDouble(e -> e).summaryStatistics();

        for (double[] es : triangles) {
            gc.setFill(getColorForValue(pointHistogram.getOrDefault(es, 0.), stats1.getMin(), stats1.getMax()));
            gc.fillOval(es[0], es[1], radius.doubleValue(), radius.doubleValue());
        }

        drawAxis(xStats, yStats);
    }

    private double finalX(DoubleSummaryStatistics xStats, Object object) {
        double j = (MAX_LAYOUT - layout.doubleValue()) / bins.intValue();
        double x = ((Number) object).doubleValue();
        double x1 = (x - xStats.getMin()) / xProportion * j + layout.doubleValue();
        return x1 - radius.doubleValue() / 2;
    }

    private double finalY(DoubleSummaryStatistics yStats, Object object2) {
        double j2 = (MAX_LAYOUT - layout.doubleValue()) / ybins.intValue();
        double y = ((Number) object2).doubleValue();
        double y1 = MAX_LAYOUT - (y - yStats.getMin()) / yProportion * j2;
        return y1 - radius.doubleValue() / 2;
    }

    private Color getColorForValue(double value, double min, double max) {
        if (value < min || value > max) {
            return Color.TRANSPARENT;
        }
        ColorPattern saturation = SupplierEx.nonNull(colorPattern.getValue(), ColorPattern.HUE);
        return saturation.getColorForValue(value, min, max);
    }

    private List<double[]> triangles() {
        List<double[]> arrayList = new ArrayList<>();

        double width = MAX_LAYOUT - layout.doubleValue();
        int sqrt = (int) (width / radius.get());
        double triangleSide = width / sqrt;
        int m = (int) (width / triangleSide / SQR_ROOT_OF_3 * 2) + 1;
        int size = sqrt * m;
        for (int i = 0; i < size; i++) {

            int o = i / sqrt;
            double x = i % sqrt * triangleSide + (o % 2 == 0 ? 0 : -triangleSide / 2) + radius.get() / 2 + layout.get();
            int j = i / sqrt;
            double k = j * triangleSide;
            double y = k * SQR_ROOT_OF_3 / 2 + layout.get() - radius.get() / 2;
            arrayList.add(new double[] { x, y });
        }
        return arrayList;
    }

    private static double finalZ(Object object) {
        if (!(object instanceof Number)) {
            return 0;
        }
        return ((Number) object).doubleValue();
    }

}