package ml;

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

class MultiLineGraph extends Canvas {
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


	public MultiLineGraph() {
		super(550, 550);
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
	public void setHistogram(DataframeML dataframe) {
		this.dataframe = dataframe;

		dataframe.forEach((col, items) -> {
            if (colors == null || colors.size() < stats.size()) {
                List<Color> generateColors = PieGraph.generateColors(stats.size());
                Iterator<Color> iterator = generateColors.iterator();
                stats.forEach((col2, itens) -> colors.put(col2, iterator.next()));
            }
            stats.put(col, items.stream().map(Number.class::cast).mapToDouble(Number::doubleValue).summaryStatistics());
        });

	}

	public void drawGraph() {
		if (dataframe == null) {
			drawAxis();
			return;

		}


		gc.clearRect(0, 0, 550, 550);
        int max = dataframe.getSize() - 1;
        double min = 0;
        xProportion = (max - min) / bins.get();

        double max2 = stats.entrySet().stream().filter(e -> colors.containsKey(e.getKey()))
                .map(Entry<String, DoubleSummaryStatistics>::getValue)

                .mapToDouble(DoubleSummaryStatistics::getMax).max().orElse(0);
        yProportion = max2 / ybins.get();

		stats.forEach((col, yStats) -> {
            Color p = colors.get(col);
            if (p == null) {
                return;
            }
			List<Double> entrySet = dataframe.list(col).stream().map(Number.class::cast)
                    .mapToDouble(Number::doubleValue).sorted().boxed().collect(Collectors.toList());
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
		});
		drawAxis();
		System.out.println(dataframe);
	}

	public void drawAxis() {

		gc.setFill(Color.BLACK);
        gc.setStroke(Color.BLACK);
		gc.setLineWidth(1);
		double e = layout.get();
		gc.strokeLine(e, e, e, maxLayout);
		gc.strokeLine(e, maxLayout, maxLayout, maxLayout);
		double j = (maxLayout - e) / bins.get();
		double d = lineSize.get();
		for (int i = 1; i <= bins.get(); i++) {
			double x1 = i * j + e;
			gc.strokeLine(x1, maxLayout, x1, maxLayout + d);
			String xLabel = String.format("%.1f", i * xProportion);
			gc.strokeText(xLabel, x1 - d * xLabel.length() / 2,
					maxLayout + d * (4 + 3 * (i % 2)));

		}
		j = (maxLayout - e) / ybins.get();
		for (int i = 0; i <= ybins.get(); i++) {
			double y1 = maxLayout - i * j;
			gc.strokeLine(e, y1, e - d, y1);
			String yLabel = String.format("%.0f", i * yProportion);
			gc.strokeText(yLabel, e - d * 4, y1);
		}
	}

	public final DoubleProperty layoutProperty() {
		return layout;
	}

	public final double getLayout() {
		return layoutProperty().get();
	}

	public final void setLayout(final double layout) {
		layoutProperty().set(layout);
	}

	public final DoubleProperty lineSizeProperty() {
		return lineSize;
	}

	public final double getLineSize() {
		return lineSizeProperty().get();
	}

	public final void setLineSize(final double lineSize) {
		lineSizeProperty().set(lineSize);
	}

	public final IntegerProperty binsProperty() {
		return bins;
	}

	public final int getBins() {
		return binsProperty().get();
	}

	public final void setBins(final int bins) {
		binsProperty().set(bins);
	}

	public final IntegerProperty ybinsProperty() {
		return ybins;
	}

	public final int getYbins() {
		return ybinsProperty().get();
	}

	public final void setYbins(final int ybins) {
		ybinsProperty().set(ybins);
	}

	public final IntegerProperty radiusProperty() {
		return radius;
	}

	public final int getRadius() {
		return radiusProperty().get();
	}

	public final void setRadius(final int radius) {
		radiusProperty().set(radius);
	}

	public ObservableMap<String, DoubleSummaryStatistics> statsProperty() {
		return stats;
	}

	public ObservableMap<String, Color> colorsProperty() {
		return colors;
	}
}