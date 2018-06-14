package ml;

import java.util.DoubleSummaryStatistics;
import java.util.Iterator;
import java.util.List;
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
	DoubleProperty layout = new SimpleDoubleProperty(30);
	double maxLayout = 480;
	DoubleProperty lineSize = new SimpleDoubleProperty(5);
	IntegerProperty bins = new SimpleIntegerProperty(20);
	IntegerProperty ybins = new SimpleIntegerProperty(20);
	double xProportion;
	double yProportion;
	GraphicsContext gc;
	private DataframeML dataframe;
	ObservableMap<String, DoubleSummaryStatistics> stats = FXCollections.observableHashMap();
	IntegerProperty radius = new SimpleIntegerProperty(5);
	private List<Color> colors;

	public MultiLineGraph() {
		super(550, 550);
		gc = getGraphicsContext2D();
		drawGraph();
		InvalidationListener listener = observable -> drawGraph();
		stats.addListener(listener);
		radius.addListener(listener);
		lineSize.addListener(listener);
		layout.addListener(listener);
		bins.addListener(listener);
		ybins.addListener(listener);
	}

	public void setHistogram(DataframeML dataframe) {
		this.dataframe = dataframe;

		dataframe.dataframe.forEach((col, items) -> {
			stats.put(col,
					items.stream().map(Number.class::cast).mapToDouble(e -> e.doubleValue()).summaryStatistics());
		});

	}

	public void drawGraph() {
		if (dataframe == null) {
			drawAxis();
			return;

		}
		if (colors == null || colors.size() < stats.size()) {
			colors = PieGraph.generateColors(stats.size());
		}

		Iterator<Color> iterator = colors.iterator();

		gc.clearRect(0, 0, 550, 550);
		stats.forEach((col, yStats) -> {

			// DoubleSummaryStatistics xStats =
			// histogram.entrySet().stream().mapToDouble(Entry<Double, Long>::getKey)
			// .summaryStatistics();
			double max = dataframe.size - 1;// xStats.getMax();
			double min = 0;
			xProportion = (max - min) / bins.get();
			double max2 = yStats.getMax();
			// long min2 = yStats.getMin()
			// ybins = 10;
			yProportion = max2 / ybins.get();
			List<Double> entrySet = dataframe.list(col).stream().map(Number.class::cast)
					.mapToDouble(e -> e.doubleValue()).sorted().boxed().collect(Collectors.toList());
			double d = layout.get();
			double j = (maxLayout - d) / bins.doubleValue();
			double j2 = (maxLayout - d) / ybins.get();
			if (iterator.hasNext()) {
				gc.setFill(iterator.next());
			}
			gc.setLineWidth(0.5);
			for (int k = 0; k < entrySet.size(); k++) {
				// Double x = entry.getKey();
				// Entry<Double, Long> entry = entrySet.get(k);
				double i = k / xProportion;
				double x1 = i * j + d;
				Double y = entrySet.get(k);
				double y1 = maxLayout - y / yProportion * j2;
				// gc.strokeLine(x1, maxLayout, x1, y1)
				int h = radius.get();
				gc.fillOval(x1 - h / 2, y1 - h / 2, h, h);
			}
			for (int k = 0; k < entrySet.size(); k++) {
				// Entry<Double, Long> entry = entrySet.get(k);
				double x = k;
				double i = x / xProportion;
				double x1 = i * j + d;
				double y = entrySet.get(k);
				double y1 = maxLayout - y / yProportion * j2;
				// gc.strokeLine(x1, maxLayout, x1, y1)
				if (k < entrySet.size() - 1) {
					Double entry2 = entrySet.get(k + 1);
					double x2 = k + 1;
					double i2 = x2 / xProportion;
					double x12 = i2 * j + d;
					double y2 = entry2;
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

}