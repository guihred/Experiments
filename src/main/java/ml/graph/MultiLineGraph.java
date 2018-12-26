package ml.graph;

import java.util.DoubleSummaryStatistics;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javafx.beans.InvalidationListener;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import ml.data.DataframeML;
import org.slf4j.Logger;
import utils.CommonsFX;
import utils.HasLogging;

public class MultiLineGraph extends Canvas implements HasLogging {
    private static final int SIZE = 550;
    private DoubleProperty layout = new SimpleDoubleProperty(30);
    private double maxLayout = 480;
    private DoubleProperty lineSize = new SimpleDoubleProperty(5);
    private IntegerProperty bins = new SimpleIntegerProperty(20);
    private IntegerProperty ybins = new SimpleIntegerProperty(20);
    private double xProportion;
    private double yProportion;
    private GraphicsContext gc;
    private DataframeML dataframe;
    private ObservableMap<String, DoubleSummaryStatistics> stats = FXCollections.observableHashMap();
    private ObservableMap<String, Color> colors = FXCollections.observableHashMap();
    private IntegerProperty radius = new SimpleIntegerProperty(5);
    private String title;

    public MultiLineGraph() {
        super(SIZE, SIZE);
        gc = getGraphicsContext2D();
        drawGraph();
        InvalidationListener listener = observable -> drawGraph();
        stats.addListener(listener);
        radius.addListener(listener);
        colors.addListener(listener);
        lineSize.addListener(listener);
        layout.addListener(listener);
        bins.addListener(listener);
        ybins.addListener(listener);
    }

    public final IntegerProperty binsProperty() {
        return bins;
    }

    public ObservableMap<String, Color> colorsProperty() {
        return colors;
    }

    public final DoubleProperty layoutProperty() {
        return layout;
    }

    public final DoubleProperty lineSizeProperty() {
        return lineSize;
    }

    public final IntegerProperty radiusProperty() {
        return radius;
    }

    public void setHistogram(DataframeML dataframe) {
        this.dataframe = dataframe;

        dataframe.forEach((col, items) -> {
            if (colors == null || colors.size() < stats.size()) {
                List<Color> generateColors = CommonsFX.generateRandomColors(stats.size());
                Iterator<Color> iterator = generateColors.iterator();
                stats.forEach((col2, itens) -> colors.put(col2, iterator.next()));
            }
            stats.put(col, items.stream().map(Number.class::cast).mapToDouble(Number::doubleValue).summaryStatistics());
        });

    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ObservableMap<String, DoubleSummaryStatistics> statsProperty() {
        return stats;
    }

    public final IntegerProperty ybinsProperty() {
        return ybins;
    }

    private void drawAxis() {

        gc.setTextAlign(TextAlignment.CENTER);
        gc.setLineWidth(1);
        gc.setStroke(Color.BLACK);
        gc.setFill(Color.BLACK);
        gc.fillText(title, layout.get() + (maxLayout - layout.get()) / 2, layout.get() - 20);
        double e = layout.get();
        gc.strokeLine(e, e, e, maxLayout);
        gc.strokeLine(e, maxLayout, maxLayout, maxLayout);
        double j = (maxLayout - e) / bins.get();
        double d = lineSize.get();

        double min = stats.values().stream().mapToDouble(DoubleSummaryStatistics::getMin).min().orElse(0);

        for (int i = 1; i <= bins.get(); i++) {
            double x1 = i * j + e;
            gc.strokeLine(x1, maxLayout, x1, maxLayout + 5);
            String xLabel = String.format("%.0f", i * xProportion + min);
            gc.strokeText(xLabel, x1, maxLayout + 5 * (4 + 3 * (i % 2)));

        }
        j = (maxLayout - e) / ybins.get();
        for (int i = 0; i <= ybins.get(); i++) {
            double y1 = maxLayout - i * j;
            gc.strokeLine(e, y1, e - 5, y1);
            String yLabel = String.format("%.1f", i * yProportion + min);
            gc.strokeText(yLabel, e - d * 2, y1);
        }
    }

    private final void drawGraph() {
        if (dataframe == null) {
            drawAxis();
            return;

        }

        gc.clearRect(0, 0, SIZE, SIZE);
        int max = dataframe.getSize() - 1;
        double min = 0;
        xProportion = (max - min) / bins.get();

        double max2 = stats.entrySet().stream().filter(e -> colors.containsKey(e.getKey()))
                .map(Entry<String, DoubleSummaryStatistics>::getValue)

                .mapToDouble(DoubleSummaryStatistics::getMax).max().orElse(0);
        yProportion = max2 / ybins.get();

        stats.forEach((col, yStats) -> drawLines(col));
        drawAxis();
        Logger logger = getLogger();
        if (logger.isTraceEnabled()) {
            logger.trace(dataframe.toString());
        }
    }

    private void drawLines(String col) {
        Color p = colors.get(col);
        if (p == null) {
            return;
        }
        List<Double> entrySet = dataframe.list(col).stream().map(Number.class::cast).mapToDouble(Number::doubleValue)
                .sorted().boxed().collect(Collectors.toList());
        double d = layout.get();
        double j = (maxLayout - d) / bins.doubleValue();
        double j2 = (maxLayout - d) / ybins.get();
        gc.setFill(p);
        gc.setStroke(p);
        gc.setLineWidth(0.5);
        for (int k = 0; k < entrySet.size(); k++) {
            double i = k / xProportion;
            double x1 = i * j + d;
            Double y = entrySet.get(k);
            double y1 = maxLayout - y / yProportion * j2;
            // gc.strokeLine(x1, maxLayout, x1, y1)
            double h = radius.get();
            gc.fillOval(x1 - h / 2, y1 - h / 2, h, h);
        }
        for (int k = 0; k < entrySet.size(); k++) {
            double x = k;
            double i = x / xProportion;
            double x1 = i * j + d;
            double y = entrySet.get(k);
            double y1 = maxLayout - y / yProportion * j2;
            if (k < entrySet.size() - 1) {
                double x2 = 1D + k;
                double i2 = x2 / xProportion;
                double x12 = i2 * j + d;
                double y2 = entrySet.get(k + 1);
                double y12 = maxLayout - y2 / yProportion * j2;
                gc.strokeLine(x1, y1, x12, y12);

            }
        }
    }
}