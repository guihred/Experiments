package ml;

import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.List;
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
import simplebuilder.HasLogging;

class LineGraph extends Canvas {
    private double layout = 30;
    private double maxLayout = 480;
    private double lineSize = 5;
    private int bins = 20;
    private long ybins = 20;
    private double xProportion;
    private double yProportion;
    private GraphicsContext gc;
    private ObservableMap<Double, Long> histogram = FXCollections.observableHashMap();
    private int radius = 5;

    public LineGraph() {
        super(550, 550);
        this.gc = this.getGraphicsContext2D();
        drawGraph();
        histogram.addListener((InvalidationListener) observable -> drawGraph());
    }

    public void setHistogram(Map<Double, Long> histogram) {
        this.histogram.putAll(histogram);
    }

    public final void drawGraph() {

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
        gc.setLineWidth(0.5);
        for (int k = 0; k < entrySet.size(); k++) {
            Entry<Double, Long> entry = entrySet.get(k);
            Double x = entry.getKey();
            int i = (int) (x / xProportion);
            double x1 = i * j + layout;
            Long y = entry.getValue();
            double y1 = maxLayout - y / yProportion * j2;
            // gc.strokeLine(x1, maxLayout, x1, y1)
            gc.fillOval(x1 - radius / 2, y1 - radius / 2, radius, radius);
        }
        for (int k = 0; k < entrySet.size(); k++) {
            Entry<Double, Long> entry = entrySet.get(k);
            Double x = entry.getKey();
            int i = (int) (x / xProportion);
            double x1 = i * j + layout;
            Long y = entry.getValue();
            double y1 = maxLayout - y / yProportion * j2;
            // gc.strokeLine(x1, maxLayout, x1, y1)
            if (k < entrySet.size() - 1) {
                Entry<Double, Long> entry2 = entrySet.get(k + 1);
                Double x2 = entry2.getKey();
                int i2 = (int) (x2 / xProportion);
                double x12 = i2 * j + layout;
                Long y2 = entry2.getValue();
                double y12 = maxLayout - y2 / yProportion * j2;
                gc.strokeLine(x1, y1, x12, y12);

            }
        }
        drawAxis();
        HasLogging.log().info("{}", histogram);


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
        for (int i = 0; i <= ybins; i++) {
            double y1 = maxLayout - i * j;
            gc.strokeLine(layout, y1, layout - lineSize, y1);
            String yLabel = String.format("%.0f", i * yProportion);
            gc.strokeText(yLabel, layout - lineSize * 4, y1);
        }
    }

}