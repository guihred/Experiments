package ml;

import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.Iterator;
import java.util.List;
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
import javafx.scene.text.TextAlignment;

class HistogramGraph extends Canvas {
	private DoubleProperty layout = new SimpleDoubleProperty(30);
	private DoubleProperty maxLayout = new SimpleDoubleProperty(480);
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
        super(550, 550);
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

    public void setHistogram(DataframeML dataframe) {
        this.dataframe = dataframe;

		dataframe.forEach((col, items) -> {
            List<Color> generateColors = PieGraph.generateRandomColors(stats.size());
            Iterator<Color> iterator = generateColors.iterator();
            colors.put(col, iterator.next());
            Map<Double, Long> histogram = dataframe.histogram(col, bins.get());

            stats.put(col, histogram.values().stream().mapToLong(e -> e).summaryStatistics());
            xstats.put(col, histogram.keySet().stream().mapToDouble(Number::doubleValue).summaryStatistics());
            stats.forEach((col2, itens) -> {
                if (iterator.hasNext()) {
                    colors.put(col2, iterator.next());
                }
            });
        });

    }

    public void drawGraph() {
        if (dataframe == null) {
            drawAxis();
            return;

        }
        gc.clearRect(0, 0, 550, 550);
        List<Entry<String, LongSummaryStatistics>> collect = stats.entrySet().stream()
                .filter(e -> colors.containsKey(e.getKey())).collect(Collectors.toList());
        List<Entry<String, DoubleSummaryStatistics>> collect2 = xstats.entrySet().stream()
                .filter(e -> colors.containsKey(e.getKey())).collect(Collectors.toList());

        long max = collect.stream().map(e -> e.getValue()).mapToLong(e -> e.getMax()).max().orElse(0);
        double min = collect.stream().map(e -> e.getValue()).mapToLong(e -> e.getMin()).min().orElse(0);
        yProportion = (max - min) / ybins.get();
        double xmax = collect2.stream().map(e -> e.getValue()).mapToDouble(e -> e.getMax()).max().orElse(0);
        double xmin = collect2.stream().map(e -> e.getValue()).mapToDouble(e -> e.getMin()).min().orElse(0);
        double xbins = bins.get();
        xProportion = (xmax - xmin) / xbins;

        collect.forEach(entryS -> {

            String key = entryS.getKey();
            Map<Double, Long> histogram = dataframe.histogram(key, bins.get());

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
                // gc.strokeLine(x1, maxLayout, x1, y1)
                gc.fillRect(x1, y1, 20, maxLayout1 - y1);
                // System.out.printf(Locale.ENGLISH, "x,y=(%.1f,%d)%n", x, y)
            }
            // System.out.println(histogram)
        });
        drawAxis();

    }
    public void drawAxis() {

        gc.setFill(Color.BLACK);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(title, layout.get() + (maxLayout.get() - layout.get()) / 2, layout.get() - 20);
        double e = layout.get();
        gc.strokeLine(e, e, e, maxLayout.get());
        gc.strokeLine(e, maxLayout.get(), maxLayout.get(), maxLayout.get());
        double j = (maxLayout.get() - e) / bins.get();
        double d = lineSize.get();

        double min = stats.values().stream().mapToDouble(f -> f.getMin()).min().orElse(0);

        for (int i = 1; i <= bins.get(); i++) {
            double x1 = i * j + e;
            gc.strokeLine(x1, maxLayout.get(), x1, maxLayout.get() + 5);
            String xLabel = String.format("%.0f", i * xProportion + min);
            gc.strokeText(xLabel, x1, maxLayout.get() + 5 * (4 + 3 * (i % 2)));

        }
        j = (maxLayout.get() - e) / ybins.get();
        for (int i = 0; i <= ybins.get(); i++) {
            double y1 = maxLayout.get() - i * j;
            gc.strokeLine(e, y1, e - 5, y1);
            String yLabel = String.format("%.1f", i * yProportion + min);
            gc.strokeText(yLabel, e - d * 2, y1);
        }
    }

    public void setTitle(String title) {
        this.title = title;
    }

	public ObservableMap<String, Color> colorsProperty() {
		return colors;
	}

	public ObservableMap<String, LongSummaryStatistics> statsProperty() {
		return stats;
	}

	public DoubleProperty lineSizeProperty() {
		return lineSize;
	}

	public DoubleProperty layoutProperty() {
		return layout;
	}

	public IntegerProperty binsProperty() {
		return bins;
	}

	public LongProperty ybinsProperty() {
		return ybins;
	}

}