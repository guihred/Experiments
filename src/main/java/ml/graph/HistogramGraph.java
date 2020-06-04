package ml.graph;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javafx.beans.InvalidationListener;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import ml.data.DataframeML;
import ml.data.DataframeUtils;
import utils.CommonsFX;

public class HistogramGraph extends Canvas {
    private static final int SIZE = 550;
    private DoubleProperty layout = new SimpleDoubleProperty(90);
    private DoubleProperty maxLayout = new SimpleDoubleProperty(SIZE);
    private DoubleProperty lineSize = new SimpleDoubleProperty(5);
    private IntegerProperty bins = new SimpleIntegerProperty(20);
    private LongProperty ybins = new SimpleLongProperty(20);
    private double xProportion;
    private double yProportion;
    private GraphicsContext gc;
    private DataframeML dataframe;
    private final ObservableMap<String, LongSummaryStatistics> stats = FXCollections.observableHashMap();
    private final ObservableMap<String, DoubleSummaryStatistics> xstats = FXCollections.observableHashMap();
    private final ObservableMap<String, Color> colors = FXCollections.observableHashMap();

    private String title;

    public HistogramGraph() {
        super(SIZE, SIZE);
        gc = getGraphicsContext2D();
        drawGraph();
        InvalidationListener listener = observable -> drawGraph();
        stats.addListener(listener);
        colors.addListener(listener);
        maxLayout.addListener(listener);
        lineSize.addListener(listener);
        bins.addListener(listener);
        ybins.addListener(listener);
        layout.addListener(listener);
    }

    public IntegerProperty binsProperty() {
        return bins;
    }

    public ObservableMap<String, Color> colorsProperty() {
        return colors;
    }

    public void drawAxis() {

        gc.setFill(Color.BLACK);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        gc.setTextAlign(TextAlignment.CENTER);
        double maxL = maxLayout.get();
        gc.fillText(title, layout.get() + (maxL - layout.get()) / 2, layout.get() - 20);
        double e = layout.get();
        gc.strokeLine(e, e, e, maxL);
        gc.strokeLine(e, maxL, maxL, maxL);
        double j = (maxL - e) / bins.get();
        double d = lineSize.get();

        double min = stats.values().stream().mapToDouble(LongSummaryStatistics::getMin).min().orElse(0);

        for (int i = 1; i <= bins.get(); i++) {
            double x1 = i * j + e;
            gc.strokeLine(x1, maxL, x1, maxL + 5);
            String xLabel = String.format("%.0f", i * xProportion + min);
            gc.strokeText(xLabel, x1, maxL + 5 * (4 + 3 * (i % 2)));

        }
        j = (maxL - e) / ybins.get();
        for (int i = 0; i <= ybins.get(); i++) {
            double y1 = maxL - i * j;
            gc.strokeLine(e, y1, e - 5, y1);
            String yLabel = String.format("%.1f", i * yProportion + min);
            gc.strokeText(yLabel, e - d * 2, y1);
        }
    }

    public final void drawGraph() {
        if (dataframe == null) {
            drawAxis();
            return;

        }
        gc.clearRect(0, 0, SIZE, SIZE);
        List<Entry<String, LongSummaryStatistics>> yHistogram = stats.entrySet().stream()
            .filter(e -> colors.containsKey(e.getKey())).collect(Collectors.toList());
        List<Entry<String, DoubleSummaryStatistics>> xHistogram = xstats.entrySet().stream()
            .filter(e -> colors.containsKey(e.getKey())).collect(Collectors.toList());

        long max = yHistogram.stream().map(Entry<String, LongSummaryStatistics>::getValue)
            .mapToLong(LongSummaryStatistics::getMax).max().orElse(0);
        double min = yHistogram.stream().map(Entry<String, LongSummaryStatistics>::getValue)
            .mapToLong(LongSummaryStatistics::getMin).min().orElse(0);
        yProportion = (max - min) / ybins.get();
        double xmax = xHistogram.stream().map(Entry<String, DoubleSummaryStatistics>::getValue)
            .mapToDouble(DoubleSummaryStatistics::getMax).max().orElse(0);
        double xmin = xHistogram.stream().map(Entry<String, DoubleSummaryStatistics>::getValue)
            .mapToDouble(DoubleSummaryStatistics::getMin).min().orElse(0);
        double xbins = bins.get();
        xProportion = (xmax - xmin) / xbins;

        yHistogram.forEach(entryS -> {

            String key = entryS.getKey();
            Map<Double, Long> histogram = DataframeUtils.histogram(dataframe, key, bins.get());

            List<Entry<Double, Long>> entrySet = histogram.entrySet().stream()
                .sorted(Comparator.comparing(Entry<Double, Long>::getKey)).collect(Collectors.toList());
            double maxLayout1 = maxLayout.get();
            double layout1 = layout.get();

            double j = (maxLayout1 - layout1) / bins.get();
            double j2 = (maxLayout1 - layout1) / ybins.get();
            gc.setLineWidth(5);
            gc.setFill(colors.get(key));
            for (Entry<Double, Long> entry : entrySet) {
                Double x = entry.getKey();
                int i = (int) (x / xProportion);
                double x1 = i * j + layout1;
                Long y = entry.getValue();
                double y1 = maxLayout1 - y / yProportion * j2;
                gc.fillRect(x1, y1, 20, maxLayout1 - y1);
            }
        });
        drawAxis();

    }

    public DoubleProperty layoutProperty() {
        return layout;
    }

    public DoubleProperty lineSizeProperty() {
        return lineSize;
    }

    public void setHistogram(DataframeML dataframe) {
        this.dataframe = dataframe;

        dataframe.forEach((col, items) -> {
            List<Color> generateColors = CommonsFX.generateRandomColors(stats.size());
            Iterator<Color> iterator = generateColors.iterator();
            colors.put(col, iterator.next());
            Map<Double, Long> histogram = DataframeUtils.histogram(dataframe, col, bins.get());

            stats.put(col, histogram.values().stream().mapToLong(e -> e).summaryStatistics());
            xstats.put(col, histogram.keySet().stream().mapToDouble(Number::doubleValue).summaryStatistics());
            stats.forEach((col2, itens) -> {
                if (iterator.hasNext()) {
                    colors.put(col2, iterator.next());
                }
            });
        });

    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ObservableMap<String, LongSummaryStatistics> statsProperty() {
        return stats;
    }

    public LongProperty ybinsProperty() {
        return ybins;
    }

}