package ml;

import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Locale;
import java.util.LongSummaryStatistics;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javafx.beans.InvalidationListener;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

class HistogramGraph extends Canvas {
    DoubleProperty layout = new SimpleDoubleProperty(30);
    DoubleProperty maxLayout = new SimpleDoubleProperty(480);
    DoubleProperty lineSize = new SimpleDoubleProperty(5);
    IntegerProperty bins = new SimpleIntegerProperty(20);
    LongProperty ybins = new SimpleLongProperty(20);
    double xProportion;
    double yProportion;
    GraphicsContext gc;
    ObservableMap<Double, Long> histogram = FXCollections.observableHashMap();

    public HistogramGraph() {
        super(550, 550);
        this.gc = this.getGraphicsContext2D();
        drawGraph();
        histogram.addListener((InvalidationListener) observable -> drawGraph());
        maxLayout.addListener((InvalidationListener) observable -> drawGraph());
        lineSize.addListener((InvalidationListener) observable -> drawGraph());
        bins.addListener((InvalidationListener) observable -> drawGraph());
        ybins.addListener((InvalidationListener) observable -> drawGraph());
        layout.addListener((InvalidationListener) observable -> drawGraph());
    }

    public void setHistogram(Map<Double, Long> histogram) {
        this.histogram.putAll(histogram);
    }

    public void drawGraph() {

        gc.clearRect(0, 0, 550, 550);
        DoubleSummaryStatistics xStats = histogram.entrySet().stream().mapToDouble(Entry<Double, Long>::getKey)
                .summaryStatistics();
        LongSummaryStatistics yStats = histogram.entrySet().stream().mapToLong(Entry<Double, Long>::getValue)
                .summaryStatistics();
        double max = xStats.getMax();
        double min = xStats.getMin();
        double bins = this.bins.get();
        xProportion = (max - min) / bins;
        long max2 = yStats.getMax();
        // long min2 = yStats.getMin()
        // ybins.set(Long.min(max2, 10));
        yProportion = max2 / (double) ybins.get();
        List<Entry<Double, Long>> entrySet = histogram.entrySet().stream()
                .sorted(Comparator.comparing(Entry<Double, Long>::getKey)).collect(Collectors.toList());
        double maxLayout = this.maxLayout.get();
        double layout = this.layout.get();

        double j = (maxLayout - layout) / bins;
        double j2 = (maxLayout - layout) / ybins.get();
        gc.setLineWidth(5);
        gc.setFill(Color.GREEN);
        for (Entry<Double, Long> entry : entrySet) {
            Double x = entry.getKey();
            int i = (int) (x / xProportion);
            double x1 = i * j + layout;
            Long y = entry.getValue();
            double y1 = maxLayout - y / yProportion * j2;
            // gc.strokeLine(x1, maxLayout, x1, y1)
            gc.fillRect(x1, y1, 20, maxLayout - y1);
            System.out.printf(Locale.ENGLISH, "x,y=(%.1f,%d)%n", x, y);
        }
        drawAxis();

        System.out.println(histogram);


    }

    public void drawAxis() {
        double layout = this.layout.get();

        double bins = this.bins.get();
        gc.setFill(Color.BLACK);
        gc.setLineWidth(1);
        double maxLayout = this.maxLayout.get();
        double lineSize = this.lineSize.get();
        gc.strokeLine(layout, layout, layout, maxLayout);
        gc.strokeLine(layout, maxLayout, maxLayout, maxLayout);
        double j = (maxLayout - layout) / bins;
        for (int i = 1; i <= bins; i++) {
            double x1 = i * j + layout;
            gc.strokeLine(x1, maxLayout, x1, maxLayout + lineSize);
            String xLabel = String.format("%.1f", i * xProportion);
            gc.strokeText(xLabel, x1 - lineSize * xLabel.length() / 2, maxLayout + lineSize * (4 + 3 * (i % 2)));

        }
        j = (maxLayout - layout) / ybins.get();
        for (int i = 1; i <= ybins.get(); i++) {
            double y1 = maxLayout - i * j;
            gc.strokeLine(layout, y1, layout - lineSize, y1);
            String yLabel = String.format("%.0f", i * yProportion);
            gc.strokeText(yLabel, layout - lineSize * 4, y1);
        }
    }

}