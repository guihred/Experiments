package ml;

import java.util.DoubleSummaryStatistics;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

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

class TimelineGraph extends Canvas {
	private DoubleProperty layout = new SimpleDoubleProperty(30);
	private double maxLayout = 480;
	private DoubleProperty lineSize = new SimpleDoubleProperty(5);
	private IntegerProperty bins = new SimpleIntegerProperty(20);
	private IntegerProperty ybins = new SimpleIntegerProperty(20);
	private double xProportion;
	private double yProportion;
	private GraphicsContext gc;
	private DataframeML dataframe;
    private DoubleSummaryStatistics stats = new DoubleSummaryStatistics();
    private IntSummaryStatistics colStats = new IntSummaryStatistics();
	private ObservableMap<String, Color> colors = FXCollections.observableHashMap();
	private IntegerProperty radius = new SimpleIntegerProperty(5);
    private String label;


	public TimelineGraph() {
		super(550, 550);
		gc = getGraphicsContext2D();
		drawGraph();
		InvalidationListener listener = observable -> drawGraph();
		radius.addListener(listener);

		lineSize.addListener(listener);
		layout.addListener(listener);
		bins.addListener(listener);
		ybins.addListener(listener);
	}

    public void setHistogram(DataframeML dataframe, String label) {
		this.dataframe = dataframe;
        this.label = label;

		dataframe.forEach((col, items) -> {


            if (StringUtils.isNumeric(col)) {
                DoubleSummaryStatistics summaryStatistics = items.stream().filter(Objects::nonNull).map(Number.class::cast)
                        .mapToDouble(Number::doubleValue).summaryStatistics();
                if (summaryStatistics.getCount() > 0) {
                    colStats.accept(Integer.valueOf(col));
                    stats.combine(
                            summaryStatistics);
                }
            }
        });
        drawGraph();
	}

	public void drawGraph() {
        gc.clearRect(0, 0, 550, 550);
		if (dataframe == null) {
			drawAxis();
			return;
		}

        int maxYear = colStats.getMax();
        int minYear = colStats.getMin();
        xProportion = (maxYear - minYear) / (double) bins.get();

        double max2 = stats.getMax();

        yProportion = (max2 - stats.getMin()) / ybins.get();

        List<String> list = dataframe.list("﻿Country Name", String.class);
        double d = layout.get();
        double j = (maxLayout - d) / bins.doubleValue();
        double j2 = (maxLayout - d) / ybins.get();
        List<Color> generateRandomColors = PieGraph.generateRandomColors(list.size());
        for (int i = 0; i < list.size(); i++) {
            String labelRow = list.get(i);
            Color value = generateRandomColors.get(i);
            colors.put(labelRow, value);
            Map<String, Object> row = dataframe.rowMap(i);
            gc.setFill(value);
            gc.setStroke(value);
            for (int year = minYear; year <= maxYear; year++) {
                Number object = (Number) row.get("" + year);
                if (object == null) {
                    continue;
                }

                double k = (year - minYear) / xProportion;
                double x1 = k * j + d;
                double y = object.doubleValue() - stats.getMin();
                double y1 = maxLayout - y / yProportion * j2;
                // gc.strokeLine(x1, maxLayout, x1, y1)
                double h = radius.get();
                gc.fillOval(x1 - h / 2, y1 - h / 2, h, h);
            }
            for (int year = minYear; year <= maxYear; year++) {
                double x = year - minYear;
                double m = x / xProportion;
                double x1 = m * j + d;
                Number object = (Number) row.get("" + year);
                if (object == null) {
                    continue;
                }
                double y = object.doubleValue() - stats.getMin();
                double y1 = maxLayout - y / yProportion * j2;
                int searchYear = year;
                while (searchYear < maxYear) {
                    double x2 = ++searchYear - minYear;
                    double i2 = x2 / xProportion;
                    double x12 = i2 * j + d;
                    Number object2 = (Number) row.get("" + searchYear);
                    if (object2 != null) {
                        double y2 = object2.doubleValue() - stats.getMin();
                        double y12 = maxLayout - y2 / yProportion * j2;
                        gc.strokeLine(x1, y1, x12, y12);
                        break;
                    }
                }
            }
        }
		drawAxis();
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
            String xLabel = String.format("%.0f", i * xProportion + colStats.getMin());
			gc.strokeText(xLabel, x1 - d * xLabel.length() / 2,
					maxLayout + d * (4 + 3 * (i % 2)));

		}
		j = (maxLayout - e) / ybins.get();
		for (int i = 0; i <= ybins.get(); i++) {
			double y1 = maxLayout - i * j;
			gc.strokeLine(e, y1, e - d, y1);
            String yLabel = String.format("%.1f", i * yProportion + stats.getMin());
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


	public ObservableMap<String, Color> colorsProperty() {
		return colors;
	}
}