package ml.graph;

import java.util.DoubleSummaryStatistics;
import java.util.Iterator;
import java.util.List;
import javafx.beans.InvalidationListener;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import ml.data.DataframeML;

public class PointGraph extends Canvas {
    private static final int CANVAS_SIZE = 500;
    private static final double MAX_LAYOUT = 480;
    private final DoubleProperty layout = new SimpleDoubleProperty(60);
    private final DoubleProperty lineSize = new SimpleDoubleProperty(20);
    private final IntegerProperty bins = new SimpleIntegerProperty(20);
    private final IntegerProperty ybins = new SimpleIntegerProperty(20);
    private double xProportion;
    private double yProportion;
    private final StringProperty xHeader = new SimpleStringProperty();
    private final StringProperty yHeader = new SimpleStringProperty();

    private GraphicsContext gc;
    private ObservableMap<String, DoubleSummaryStatistics> stats = FXCollections.observableHashMap();

    private DoubleProperty radius = new SimpleDoubleProperty(1);
    private DataframeML data;

    public PointGraph() {
        super(CANVAS_SIZE, CANVAS_SIZE);
        gc = getGraphicsContext2D();
        drawGraph();
        InvalidationListener listener = observable -> drawGraph();
        stats.addListener(listener);
        lineSize.addListener(listener);
        bins.addListener(listener);
        radius.addListener(listener);
        ybins.addListener(listener);
        xHeader.addListener(listener);
        yHeader.addListener(listener);
        layout.addListener(listener);
    }

    public final IntegerProperty binsProperty() {
        return bins;
    }

    public void drawAxis(DoubleSummaryStatistics xStats, DoubleSummaryStatistics yStats) {
        String title = xHeader.get() + " X " + yHeader.get();
        gc.setFill(Color.BLACK);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(title, layout.get() + (MAX_LAYOUT - layout.get()) / 2, layout.get() - 20);
        double e = layout.get();
        gc.strokeLine(e, MAX_LAYOUT, MAX_LAYOUT, MAX_LAYOUT);
        gc.strokeLine(e, e, e, MAX_LAYOUT);
        double j = (MAX_LAYOUT - e) / bins.get();
        double d = lineSize.get();
        for (int i = 1; i <= bins.get(); i++) {
            double x1 = i * j + e;
            gc.strokeLine(x1, MAX_LAYOUT, x1, MAX_LAYOUT + 5);
            String xLabel = String.format("%.0f", i * xProportion + xStats.getMin());
            gc.strokeText(xLabel, x1, MAX_LAYOUT + 5 * (4 + 3 * (i % 2)));

        }
        j = (MAX_LAYOUT - e) / ybins.get();
        for (int i = 0; i <= ybins.get(); i++) {
            double y1 = MAX_LAYOUT - i * j;
            gc.strokeLine(e, y1, e - 5, y1);
            String yLabel = String.format("%.1f", i * yProportion + yStats.getMin());
            gc.strokeText(yLabel, e - d * 2, y1);
        }
    }

    public final void drawGraph() {
        DoubleSummaryStatistics xStats = stats.get(xHeader.get());
        DoubleSummaryStatistics yStats = stats.get(yHeader.get());
        if (xStats == null || yStats == null) {
            return;
        }
        gc.clearRect(0, 0, CANVAS_SIZE, CANVAS_SIZE);
        double max = xStats.getMax();
        double min = xStats.getMin();
        xProportion = (max - min) / bins.intValue();
        final double pad = 0.1;
        double max2 = yStats.getMax() + pad;
        double min2 = yStats.getMin() - pad;
        yProportion = (max2 - min2) / ybins.intValue();

        List<Object> entrySetX = data.list(xHeader.get());
        List<Object> entrySetY = data.list(yHeader.get());
        double j = (MAX_LAYOUT - layout.doubleValue()) / bins.intValue();
        double j2 = (MAX_LAYOUT - layout.doubleValue()) / ybins.intValue();
        gc.setLineWidth(5);
        gc.setFill(Color.GREEN);
        gc.setLineWidth(0.5);
        for (int k = 0; k < data.getSize(); k++) {
            double x = ((Number) entrySetX.get(k)).doubleValue();
            double x1 = (x - xStats.getMin()) / xProportion * j + layout.doubleValue();
            double y = ((Number) entrySetY.get(k)).doubleValue();
            double y1 = MAX_LAYOUT - (y - yStats.getMin()) / yProportion * j2;
            // gc.strokeLine(x1, maxLayout, x1, y1)
            double rad = radius.doubleValue();
            gc.fillOval(x1 - rad / 2, y1 - rad / 2, rad, rad);
        }
        drawAxis(xStats, yStats);
    }

    public final int getBins() {
        return binsProperty().get();
    }

    public final double getLayout() {
        return layoutProperty().get();
    }

    public final double getLineSize() {
        return lineSizeProperty().get();
    }

    public final double getRadius() {
        return radiusProperty().get();
    }

    public final String getXHeader() {
        return xHeaderProperty().get();
    }

    public final int getYbins() {
        return ybinsProperty().get();
    }

    public final String getYHeader() {
        return yHeaderProperty().get();
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

    public final void setBins(final int bins) {
        binsProperty().set(bins);
    }

    public void setDatagram(DataframeML x) {
        data = x;
        data.forEach((col, items) -> {
            DoubleSummaryStatistics summaryStatistics = items.stream().map(Number.class::cast)
                .mapToDouble(Number::doubleValue).summaryStatistics();
            stats.put(col, summaryStatistics);
        });
        Iterator<String> iterator = data.cols().iterator();
        if (iterator.hasNext()) {
            xHeader.set(iterator.next());
        }
        if (iterator.hasNext()) {
            yHeader.set(iterator.next());
        }

    }

    public final void setLayout(final double layout) {
        layoutProperty().set(layout);
    }

    public final void setLineSize(final double lineSize) {
        lineSizeProperty().set(lineSize);
    }

    public final void setRadius(final double radius) {
        radiusProperty().set(radius);
    }

    public final void setXHeader(final String xHeader) {
        xHeaderProperty().set(xHeader);
    }

    public final void setYbins(final int ybins) {
        ybinsProperty().set(ybins);
    }

    public final void setYHeader(final String yHeader) {
        yHeaderProperty().set(yHeader);
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

}