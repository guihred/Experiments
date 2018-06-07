package ml;

import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

class PointGraph extends Canvas {
    double layout = 30;
    double maxLayout = 480;
    double lineSize = 5;
    int bins = 20;
    int ybins = 20;
    double xProportion;
    double yProportion;
    GraphicsContext gc;
    ObservableList<Entry<? extends Number, ? extends Number>> points = FXCollections.observableArrayList();

    public PointGraph() {
        super(550, 550);
        this.gc = this.getGraphicsContext2D();
        drawGraph();
        points.addListener((InvalidationListener) observable -> drawGraph());
    }

    public void setPoints2(List<Entry<? extends Number, ? extends Number>> points) {
        this.points.addAll(points);
    }

    public void setPoints(List<Entry<Number, Number>> points2) {
        this.points.addAll(points2);
    }

    double radius = 1;
    public void drawGraph() {
        if (points.isEmpty()) {
            return;
        }
        gc.clearRect(0, 0, 550, 550);
        DoubleSummaryStatistics xStats = points.stream().map(Entry<? extends Number, ? extends Number>::getKey)
                .mapToDouble(Number::doubleValue)
                .summaryStatistics();
        DoubleSummaryStatistics yStats = points.stream().map(Entry<? extends Number, ? extends Number>::getValue)
                .mapToDouble(Number::doubleValue)
                .summaryStatistics();
        double max = xStats.getMax();
        double min = xStats.getMin();
        xProportion = (max - min) / bins;
        double max2 = yStats.getMax() + 0.1;
        double min2 = yStats.getMin() - 0.1;
        ybins = 20;
        yProportion = (max2 - min2) / ybins;
        xProportion = Double.max(xProportion, yProportion);
        yProportion = Double.max(xProportion, yProportion);

        List<Entry<? extends Number, ? extends Number>> entrySet = points.stream()
                .sorted(Comparator.comparing((Entry<? extends Number, ? extends Number> e) -> e.getKey().doubleValue()))
                .collect(Collectors.toList());
        double j = (maxLayout - layout) / bins;
        double j2 = (maxLayout - layout) / ybins;
        gc.setLineWidth(5);
        gc.setFill(Color.GREEN);
        gc.setLineWidth(0.5);
        for (int k = 0; k < entrySet.size(); k++) {
            Entry<? extends Number, ? extends Number> entry = entrySet.get(k);
            double x = entry.getKey().doubleValue();
            double x1 = (x - xStats.getMin()) / xProportion * j + layout;
            double y = entry.getValue().doubleValue();
            double y1 = maxLayout - (y - yStats.getMin()) / yProportion * j2;
            // gc.strokeLine(x1, maxLayout, x1, y1)
            System.out.println(entry);
            gc.fillOval(x1 - radius / 2, y1 - radius / 2, radius, radius);
        }
        drawAxis(xStats, yStats);

        System.out.println(points);


    }

    public void drawAxis(DoubleSummaryStatistics xStats, DoubleSummaryStatistics yStats) {

        gc.setFill(Color.BLACK);
        gc.setLineWidth(1);
        gc.strokeLine(layout, layout, layout, maxLayout);
        gc.strokeLine(layout, maxLayout, maxLayout, maxLayout);
        double j = (maxLayout - layout) / bins;
        for (int i = 1; i <= bins; i++) {
            double x1 = i * j + layout;
            gc.strokeLine(x1, maxLayout, x1, maxLayout + lineSize);
            String xLabel = String.format("%.1f", i * xProportion + xStats.getMin());
            gc.strokeText(xLabel, x1 - lineSize * xLabel.length() / 2, maxLayout + lineSize * (4 + 3 * (i % 2)));

        }
        j = (maxLayout - layout) / ybins;
        for (int i = 0; i <= ybins; i++) {
            double y1 = maxLayout - i * j;
            gc.strokeLine(layout, y1, layout - lineSize, y1);
            String yLabel = String.format("%.1f", i * yProportion + yStats.getMin());
            gc.strokeText(yLabel, layout - lineSize * 4, y1);
        }
    }


}