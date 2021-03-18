package ml.graph;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.scene.text.TextAlignment;
import ml.data.DataframeML;
import ml.data.DataframeUtils;
import utils.ImageFXUtils;

public class PieGraph extends Canvas {
    private static final double TOTAL_CIRCLE = 360.;
    private static final int WIDTH = 550;
    private final GraphicsContext gc;
    private final ObservableMap<String, Long> histogram = FXCollections.observableHashMap();
    private final IntegerProperty radius = new SimpleIntegerProperty(WIDTH / 2);
    private final IntegerProperty bins = new SimpleIntegerProperty(10);
    private final IntegerProperty xOffset = new SimpleIntegerProperty(10);
    private final IntegerProperty start = new SimpleIntegerProperty(90);
    private final DoubleProperty legendsRadius = new SimpleDoubleProperty(3. / 4);
    private final BooleanProperty showLines = new SimpleBooleanProperty(false);
    private List<Color> availableColors;
    private String column = "Region";
    private DataframeML dataframe;

    public PieGraph() {
        super(WIDTH, WIDTH);
        gc = getGraphicsContext2D();
        drawGraph();
        radius.set((int) (gc.getCanvas().getWidth() / 2));
        InvalidationListener listener = e -> drawGraph();
        radius.addListener(listener);
        legendsRadius.addListener(listener);
        xOffset.addListener(listener);
        showLines.addListener(listener);
        start.addListener(listener);
        bins.addListener(e -> {
            Map<String, Long> hist = convertToHistogram();
            histogram.clear();
            histogram.putAll(hist);
            availableColors = ImageFXUtils.generateRandomColors(histogram.size());
            drawGraph();
        });
        widthProperty().addListener(listener);
        Bindings.selectDouble(sceneProperty(), "width").divide(3 / 2.)
                .addListener((ob, o, n) -> setWidth(n.doubleValue()));
        Bindings.selectDouble(sceneProperty(), "height").addListener((ob, o, n) -> setHeight(n.doubleValue()));

    }

    public IntegerProperty binsProperty() {
        return bins;
    }

    public boolean getShowLines() {
        return showLines.get();
    }

    public DoubleProperty legendsRadiusProperty() {
        return legendsRadius;
    }

    public IntegerProperty radiusProperty() {
        return radius;
    }

    public void setDataframe(DataframeML dataframe, String column) {
        this.dataframe = dataframe;
        this.column = column;
        histogram.clear();
        histogram.putAll(convertToHistogram());
        availableColors = ImageFXUtils.generateRandomColors(histogram.size());
        drawGraph();
    }

    public void setHistogram(Map<String, Long> dataframe) {
        histogram.clear();
        histogram.putAll(dataframe);
        availableColors = ImageFXUtils.generateRandomColors(histogram.size());
        drawGraph();
    }

    public void setShowLines(boolean value) {
        showLines.set(value);
    }

    public BooleanProperty showLinesProperty() {
        return showLines;
    }

    public IntegerProperty startProperty() {
        return start;
    }

    public IntegerProperty xOffsetProperty() {
        return xOffset;
    }

    private Map<String, Long> convertToHistogram() {
        if (dataframe == null) {
            return histogram;
        }
        if (column == null) {
            column = dataframe.cols().stream().findFirst().orElse(null);
        }

        if (dataframe.getFormat(column) != String.class) {
            Map<Double, Long> dataframeHistogram = DataframeUtils.histogram(dataframe, column, bins.get());
            return dataframeHistogram.entrySet().stream().collect(Collectors
                    .toMap(t -> String.format("%.0f", t.getKey()), Entry<Double, Long>::getValue, (a, b) -> a + b));
        }
        return dataframe.histogram(column);
    }

    private final void drawGraph() {
        long sum = histogram.values().stream().mapToLong(e -> e).sorted().sum();
        gc.clearRect(0, 0, getWidth(), getHeight());

        double centerX = getWidth() / 4 + xOffset.get();
        double centerY = getHeight() / 4 + xOffset.get();
        final int realStartAngle = start.get();
        double startAngle = start.get();
        gc.setLineWidth(1. / 2);
        List<Entry<String, Long>> histogramLevels = histogram.entrySet().stream()
                .sorted(Comparator.comparing(Entry<String, Long>::getValue)).collect(Collectors.toList());
        int radius2 = radius.get();
        for (int i = 0; i < histogramLevels.size(); i++) {
            Entry<String, Long> entry = histogramLevels.get(i);
            double arcExtent = entry.getValue() * TOTAL_CIRCLE / sum;
            gc.setFill(availableColors.get(i));

            gc.fillArc(centerX, centerY, radius2, radius2, startAngle, arcExtent, ArcType.ROUND);
            gc.strokeArc(centerX, centerY, radius2, radius2, startAngle, arcExtent, ArcType.ROUND);

            startAngle += arcExtent;
        }
        startAngle = realStartAngle;
        gc.setTextAlign(TextAlignment.CENTER);
        for (int i = 0; i < histogramLevels.size(); i++) {
            Entry<String, Long> entry = histogramLevels.get(i);
            double arcExtent = entry.getValue() * TOTAL_CIRCLE / sum;
            int j = radius2 / 2;
            double d = legendsRadius.get();
            double angdeg = arcExtent / 2 + startAngle + TOTAL_CIRCLE / 4;
            double x = Math.sin(Math.toRadians(angdeg)) * radius2 * d + centerX + j;
            double y = Math.cos(Math.toRadians(angdeg)) * radius2 * d + centerY + j;

            gc.setFill(Color.BLACK);
            if (showLines.get()) {
                double x2 = Math.sin(Math.toRadians(angdeg)) * radius2 / 3 + centerX + j;
                double y2 = Math.cos(Math.toRadians(angdeg)) * radius2 / 3 + centerY + j;
                gc.strokeLine(x, y, x2, y2);

                double ang = (angdeg + TOTAL_CIRCLE * 2) % TOTAL_CIRCLE;
                gc.setTextAlign(
                        ang > TOTAL_CIRCLE / 2 && ang < TOTAL_CIRCLE ? TextAlignment.RIGHT : TextAlignment.LEFT);
                gc.fillText(entry.getKey(), x, y);

            } else {
                gc.save();
                gc.translate(x, y);
                double degrees = TOTAL_CIRCLE - startAngle - arcExtent / 2;
                double degrees2 = degrees > TOTAL_CIRCLE / 4 ? degrees + TOTAL_CIRCLE / 2 : degrees;
                gc.rotate(degrees2);
                gc.fillText(entry.getKey(), 0, 0);
                gc.restore();
            }

            startAngle += arcExtent;
        }
        drawLegend(histogramLevels, availableColors);
    }

    private void drawLegend(List<Entry<String, Long>> histogramLevels, List<Color> availableColors1) {
        double x = gc.getCanvas().getWidth() / 10;
        double y = gc.getCanvas().getHeight() * 7 / 8;
        int columns = (int) Math.sqrt(histogramLevels.size()) + 1;
        int maxLetter =
                histogramLevels.stream().map(Entry<String, Long>::getKey).mapToInt(String::length).max().orElse(0);
        double a = gc.getCanvas().getWidth() / columns / 4 + maxLetter * 4;
        double b = gc.getCanvas().getHeight() / columns / 8;

        gc.setTextAlign(TextAlignment.LEFT);
        for (int i = 0; i < histogramLevels.size(); i++) {
            int index = histogramLevels.size() - i - 1;
            Entry<String, Long> entry = histogramLevels.get(index);
            int j = i / columns;
            double x2 = x + a * (i % columns);
            double y2 = y + b * j;
            gc.setFill(Color.BLACK);
            gc.fillText(entry.getKey(), x2, y2);
            gc.setFill(availableColors1.get(index));
            gc.fillRect(x2 - 10, y2 - 8, 8, 8);
            gc.strokeRect(x2 - 10, y2 - 8, 8, 8);
        }
    }

}