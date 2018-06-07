package ml;

import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javafx.beans.InvalidationListener;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class PointGraph extends Canvas {
    DoubleProperty layout = new SimpleDoubleProperty(30);
    double maxLayout = 480;
    DoubleProperty lineSize = new SimpleDoubleProperty(5);
    IntegerProperty bins = new SimpleIntegerProperty(20);
    IntegerProperty ybins = new SimpleIntegerProperty(20);
    double xProportion;
    double yProportion;
    GraphicsContext gc;
    ObservableList<Entry<? extends Number, ? extends Number>> points = FXCollections.observableArrayList();

    DoubleProperty radius = new SimpleDoubleProperty(1);;

    public PointGraph() {
        super(550, 550);
        this.gc = this.getGraphicsContext2D();
        drawGraph();
        InvalidationListener listener = observable -> drawGraph();
        points.addListener(listener);
        lineSize.addListener(listener);
        bins.addListener(listener);
        ybins.addListener(listener);
        radius.addListener(listener);
        layout.addListener(listener);
    }

    public void drawAxis(DoubleSummaryStatistics xStats, DoubleSummaryStatistics yStats) {

        gc.setFill(Color.BLACK);
        gc.setLineWidth(1);
        gc.strokeLine(layout.doubleValue(), layout.doubleValue(), layout.doubleValue(), maxLayout);
        gc.strokeLine(layout.doubleValue(), maxLayout, maxLayout, maxLayout);
        double j = (maxLayout - layout.doubleValue()) / bins.intValue();
        for (int i = 1; i <= bins.intValue(); i++) {
            double x1 = i * j + layout.doubleValue();
            gc.strokeLine(x1, maxLayout, x1, maxLayout + lineSize.doubleValue());
            String xLabel = String.format("%.1f", i * xProportion + xStats.getMin());
            gc.strokeText(xLabel, x1 - lineSize.doubleValue() * xLabel.length() / 2,
                    maxLayout + lineSize.doubleValue() * (4 + 3 * (i % 2)));

        }
        j = (maxLayout - layout.doubleValue()) / ybins.intValue();
        for (int i = 0; i <= ybins.intValue(); i++) {
            double y1 = maxLayout - i * j;
            gc.strokeLine(layout.doubleValue(), y1, layout.doubleValue() - lineSize.doubleValue(), y1);
            String yLabel = String.format("%.1f", i * yProportion + yStats.getMin());
            gc.strokeText(yLabel, layout.doubleValue() - lineSize.doubleValue() * 4, y1);
        }
    }

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
        xProportion = (max - min) / bins.intValue();
        double max2 = yStats.getMax() + 0.1;
        double min2 = yStats.getMin() - 0.1;
        yProportion = (max2 - min2) / ybins.intValue();
        xProportion = Double.max(xProportion, yProportion);
        yProportion = Double.max(xProportion, yProportion);

        List<Entry<? extends Number, ? extends Number>> entrySet = points.stream()
                .sorted(Comparator.comparing((Entry<? extends Number, ? extends Number> e) -> e.getKey().doubleValue()))
                .collect(Collectors.toList());
        double j = (maxLayout - layout.doubleValue()) / bins.intValue();
        double j2 = (maxLayout - layout.doubleValue()) / ybins.intValue();
        gc.setLineWidth(5);
        gc.setFill(Color.GREEN);
        gc.setLineWidth(0.5);
        for (int k = 0; k < entrySet.size(); k++) {
            Entry<? extends Number, ? extends Number> entry = entrySet.get(k);
            double x = entry.getKey().doubleValue();
            double x1 = (x - xStats.getMin()) / xProportion * j + layout.doubleValue();
            double y = entry.getValue().doubleValue();
            double y1 = maxLayout - (y - yStats.getMin()) / yProportion * j2;
            // gc.strokeLine(x1, maxLayout, x1, y1)
            System.out.println(entry);
            gc.fillOval(x1 - radius.doubleValue() / 2, y1 - radius.doubleValue() / 2, radius.doubleValue(),
                    radius.doubleValue());
        }
        drawAxis(xStats, yStats);

        System.out.println(points);


    }
    public void setPoints(List<Entry<Number, Number>> points2) {
        this.points.addAll(points2);
    }

    public void setPoints2(List<Entry<? extends Number, ? extends Number>> points) {
        this.points.addAll(points);
    }


}