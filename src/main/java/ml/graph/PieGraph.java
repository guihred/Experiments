package ml.graph;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.scene.text.TextAlignment;
import ml.data.DataframeML;
import utils.CommonsFX;

public class PieGraph extends Canvas {
    private static final int WIDTH = 550;
    private GraphicsContext gc;
    private final ObservableMap<String, Long> histogram = FXCollections.observableHashMap();
    private IntegerProperty radius = new SimpleIntegerProperty(WIDTH / 2);
    private IntegerProperty bins = new SimpleIntegerProperty(10);
    private IntegerProperty xOffset = new SimpleIntegerProperty(10);
    private DoubleProperty legendsRadius = new SimpleDoubleProperty(3. / 4);
    private List<Color> availableColors;
    private String column = "Region";
    private DataframeML dataframe;

    public PieGraph() {
        super(WIDTH, WIDTH);
        gc = getGraphicsContext2D();
        drawGraph();
        radius.set((int) (gc.getCanvas().getWidth() / 2));
        radius.addListener(e -> drawGraph());
        legendsRadius.addListener(e -> drawGraph());
        xOffset.addListener(e -> drawGraph());
        bins.addListener(e -> {
            Map<String, Long> hist = convertToHistogram();
            histogram.clear();
            histogram.putAll(hist);
            availableColors = CommonsFX.generateRandomColors(histogram.size());
            drawGraph();
        });
    }

    public IntegerProperty binsProperty() {
        return bins;
    }
    public final void drawGraph() {
        long sum = histogram.values().stream().mapToLong(e -> e).sorted().sum();
        gc.clearRect(0, 0, WIDTH, WIDTH);
        gc.setTextAlign(TextAlignment.CENTER);

        double centerX = gc.getCanvas().getWidth() / 4 + xOffset.get();
        double centerY = gc.getCanvas().getHeight() / 4 + xOffset.get();
        double startAngle = 90;
        gc.setLineWidth(0.5);
        List<Entry<String, Long>> histogramLevels = histogram.entrySet().stream()
                .sorted(Comparator.comparing(Entry<String, Long>::getValue)).collect(Collectors.toList());
        int radius2 = radius.get();
        for (int i = 0; i < histogramLevels.size(); i++) {
            Entry<String, Long> entry = histogramLevels.get(i);
            double arcExtent = entry.getValue() * 360. / sum;
            gc.setFill(availableColors.get(i));

            gc.fillArc(centerX, centerY, radius2, radius2, startAngle, arcExtent, ArcType.ROUND);
            gc.strokeArc(centerX, centerY, radius2, radius2, startAngle, arcExtent, ArcType.ROUND);

            startAngle += arcExtent;
        }
        startAngle = 90;
        for (int i = 0; i < histogramLevels.size(); i++) {
            Entry<String, Long> entry = histogramLevels.get(i);
            double arcExtent = entry.getValue() * 360. / sum;
            int j = radius2 / 2;
            double d = legendsRadius.get();
            double x = Math.sin(Math.toRadians(arcExtent / 2 + startAngle + 90)) * radius2 * d + centerX + j;
            double y = Math.cos(Math.toRadians(arcExtent / 2 + startAngle + 90)) * radius2 * d + centerY + j;

            gc.setFill(Color.BLACK);
            gc.fillText(entry.getKey(), x, y);

            startAngle += arcExtent;
        }
        drawLegend(histogramLevels, availableColors);
    }

    public void drawLegend(List<Entry<String, Long>> histogramLevels, List<Color> availableColors1) {
        double x = gc.getCanvas().getWidth() / 10;
        double y = gc.getCanvas().getHeight() * 7 / 8;
        int columns = (int) Math.sqrt(histogramLevels.size()) + 1;
        int maxLetter = histogramLevels.stream().map(Entry<String, Long>::getKey).mapToInt(String::length).max()
                .orElse(0);
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
        availableColors = CommonsFX.generateRandomColors(histogram.size());
        drawGraph();
    }

    public void setHistogram(Map<String, Long> dataframe) {
        histogram.clear();
        histogram.putAll(dataframe);
        availableColors = CommonsFX.generateRandomColors(histogram.size());
        drawGraph();
    }

    public IntegerProperty xOffsetProperty() {
        return xOffset;
    }

    private Map<String, Long> convertToHistogram() {
        if (dataframe == null) {
            return histogram;
        }

        if (dataframe.getFormat(column) != String.class) {
            Map<Double, Long> dataframeHistogram = dataframe.histogram(column, bins.get());
            return dataframeHistogram.entrySet().stream()
                    .collect(Collectors.toMap(t -> String.format("%.0f", t.getKey()),
                            Entry<Double, Long>::getValue, (a, b) -> a + b));
        }
        return dataframe.histogram(column);
    }


}