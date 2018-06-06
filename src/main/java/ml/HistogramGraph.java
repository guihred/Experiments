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
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

class HistogramGraph extends Canvas {
    double layout = 30;
    double maxLayout = 480;
    double lineSize = 5;
    int bins = 20;
    long ybins = 20;
    double xProportion;
    double yProportion;
    GraphicsContext gc;
    ObservableMap<Double, Long> histogram = FXCollections.observableHashMap();

    public HistogramGraph() {
        super(550, 550);
        this.gc = this.getGraphicsContext2D();
        drawGraph();
        histogram.addListener((InvalidationListener) observable -> drawGraph());
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
        xProportion = (max - min) / bins;
        long max2 = yStats.getMax();
        // long min2 = yStats.getMin()
        ybins = Long.min(max2, 10);
        yProportion = max2 / (double) ybins;
        List<Entry<Double, Long>> entrySet = histogram.entrySet().stream()
                .sorted(Comparator.comparing(Entry<Double, Long>::getKey)).collect(Collectors.toList());
        double j = (maxLayout - layout) / bins;
        double j2 = (maxLayout - layout) / ybins;
        gc.setLineWidth(5);
        gc.setFill(Color.GREEN);
        for (Entry<Double, Long> entry : entrySet) {
            Double x = entry.getKey();
            int i = (int) (x / xProportion);
            double x1 = i * j + layout;
            Long y = entry.getValue();
            double y1 = maxLayout - y / yProportion * j2 + layout;
            // gc.strokeLine(x1, maxLayout, x1, y1)
            gc.fillRect(x1, y1, 20, maxLayout - y1);
            System.out.printf(Locale.ENGLISH, "x,y=(%.1f,%d)%n", x, y);
        }
        drawAxis();

        System.out.println(histogram);


    }

    public void drawAxis() {

        gc.setFill(Color.BLACK);
        gc.setLineWidth(1);
        gc.strokeLine(layout, layout, layout, maxLayout);
        gc.strokeLine(layout, maxLayout, maxLayout, maxLayout);
        double j = (maxLayout - layout) / bins;
        for (int i = 1; i <= bins; i++) {
            double x1 = i * j + layout;
            gc.strokeLine(x1, maxLayout, x1, maxLayout + lineSize);
            String xLabel = String.format("%.1f", i * xProportion);
            gc.strokeText(xLabel, x1 - lineSize * xLabel.length() / 2, maxLayout + lineSize * (4 + 3 * (i % 2)));

        }
        j = (maxLayout - layout) / ybins;
        for (int i = 1; i <= ybins; i++) {
            double y1 = maxLayout - i * j + layout;
            gc.strokeLine(layout, y1, layout - lineSize, y1);
            String yLabel = String.format("%.0f", i * yProportion);
            gc.strokeText(yLabel, layout - lineSize * 4, y1);
        }
    }

}