package ml;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import utils.CommonsFX;

public class PieGraph extends Canvas {
	private GraphicsContext gc;
	private ObservableMap<String, Long> histogram = FXCollections.observableHashMap();
	private IntegerProperty radius = new SimpleIntegerProperty(275);
    private IntegerProperty bins = new SimpleIntegerProperty(10);
    private List<Color> availableColors;
    private String column = "Region";
    private DataframeML dataframe;

    public PieGraph() {
        super(550, 550);
        gc = getGraphicsContext2D();
        drawGraph();
        radius.set((int) (gc.getCanvas().getWidth() / 2));
        radius.addListener(e -> drawGraph());
        bins.addListener(e -> {

            Map<String, Long> collect = convertToHistogram();
            histogram.clear();
            histogram.putAll(collect);
            availableColors = CommonsFX.generateRandomColors(histogram.size());
            drawGraph();
        });
    }

    public void setDataframe(DataframeML dataframe, String column) {
        this.dataframe = dataframe;
        this.column = column;
        histogram.putAll(convertToHistogram());
        availableColors = CommonsFX.generateRandomColors(histogram.size());
        drawGraph();
    }

    private Map<String, Long> convertToHistogram() {
        Map<String, Long> collect;
        if (dataframe.getFormat(column) != String.class) {
            Map<Double, Long> dataframeHistogram = dataframe.histogram(column, bins.get());
            collect = dataframeHistogram.entrySet().stream()
                    .collect(Collectors.toMap(t -> String.format("%.0f", t.getKey()),
                            Entry<Double, Long>::getValue, (a, b) -> a + b));
        } else {
            collect = dataframe.histogram(column);
        }
        return collect;
    }

    public final void drawGraph() {
        long sum = histogram.values().stream().mapToLong(e -> e).sorted().sum();
        gc.clearRect(0, 0, 550, 550);

        double centerX = gc.getCanvas().getWidth() / 4;
        double centerY = gc.getCanvas().getHeight() / 4;
        double startAngle = 90;
        gc.setLineWidth(0.5);
        List<Entry<String, Long>> histogramLevels = histogram.entrySet().stream()
                .sorted(Comparator.comparing(Entry<String, Long>::getValue)).collect(Collectors.toList());
        int radius2 = radius.get();
        for (int i = 0; i < histogramLevels.size(); i++) {
            Entry<String, Long> entry = histogramLevels.get(i);
            double arcExtent = entry.getValue() * 360d / sum;
            gc.setFill(availableColors.get(i));

            gc.fillArc(centerX, centerY, radius2, radius2, startAngle, arcExtent, ArcType.ROUND);
            gc.strokeArc(centerX, centerY, radius2, radius2, startAngle, arcExtent, ArcType.ROUND);

            startAngle += arcExtent;
        }
        startAngle = 90;
        for (int i = 0; i < histogramLevels.size(); i++) {
            Entry<String, Long> entry = histogramLevels.get(i);
            double arcExtent = entry.getValue() * 360d / sum;
            int j = radius2 / 2;
            double x = Math.sin(Math.toRadians(arcExtent / 2 + startAngle + 90)) * radius2 / 1.5 + centerX + j
                    - 4 * entry.getKey().length();
            double y = Math.cos(Math.toRadians(arcExtent / 2 + startAngle + 90)) * radius2 / 1.5 + centerY + j;

            gc.setFill(Color.BLACK);
            gc.fillText(entry.getKey(), x, y);

            startAngle += arcExtent;
        }
        drawLegend(histogramLevels, availableColors);
    }

	public static List<Color> generateColors(int size) {
        List<Color> availableColors = new ArrayList<>();
		int cubicRoot = Integer.max((int) Math.ceil(Math.pow(size, 1.0 / 3.0)), 2);
        for (int i = 0; i < cubicRoot * cubicRoot * cubicRoot; i++) {
            Color rgb = Color.rgb(Math.abs(255 - i / cubicRoot / cubicRoot % cubicRoot * 256 / cubicRoot) % 256,
                    Math.abs(255 - i / cubicRoot % cubicRoot * 256 / cubicRoot) % 256,
                    Math.abs(255 - i % cubicRoot * 256 / cubicRoot) % 256);

            availableColors.add(rgb);
        }
        return availableColors;
    }

    public void drawLegend(List<Entry<String, Long>> collect, List<Color> availableColors1) {
        double x = gc.getCanvas().getWidth() / 10;
        double y = gc.getCanvas().getHeight() * 7 / 8;
        int columns = (int) Math.sqrt(collect.size()) + 1;
        int maxLetter = collect.stream().map(Entry<String, Long>::getKey).mapToInt(String::length).max().orElse(0);
        double a = gc.getCanvas().getWidth() / columns / 4 + maxLetter * 4;
        double b = gc.getCanvas().getHeight() / columns / 8;
        for (int i = 0; i < collect.size(); i++) {
            int index = collect.size() - i - 1;
            Entry<String, Long> entry = collect.get(index);
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

    public IntegerProperty radiusProperty() {
        return radius;
    }

    public IntegerProperty binsProperty() {
        return bins;
    }

}