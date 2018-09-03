package ml;

import java.util.*;
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
import org.apache.commons.lang3.StringUtils;

class TimelineGraph extends Canvas {
    private String countryNameColumn = "﻿Country Name";
    private DoubleProperty layout = new SimpleDoubleProperty(30);
	private double maxLayout = 480;
	private DoubleProperty lineSize = new SimpleDoubleProperty(5);
	private IntegerProperty bins = new SimpleIntegerProperty(20);
	private IntegerProperty ybins = new SimpleIntegerProperty(20);
    private final DoubleProperty xProportion = new SimpleDoubleProperty();
	private double yProportion;
	private GraphicsContext gc;
	private DataframeML dataframe;
    private DoubleSummaryStatistics stats = new DoubleSummaryStatistics();
    private IntSummaryStatistics colStats = new IntSummaryStatistics();
	private ObservableMap<String, Color> colors = FXCollections.observableHashMap();
	private IntegerProperty radius = new SimpleIntegerProperty(5);
    private String title;

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

    public void setHistogram(DataframeML dataframe, String countryNameColumn) {
		this.dataframe = dataframe;
        this.countryNameColumn = countryNameColumn;
        stats = new DoubleSummaryStatistics();
        colStats = new IntSummaryStatistics();
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

    public final void drawGraph() {
        gc.clearRect(0, 0, 550, 550);
		if (dataframe == null) {
			drawAxis();
			return;
		}

        int maxYear = colStats.getMax();
        int minYear = colStats.getMin();
        xProportion.set((maxYear - minYear) / (double) bins.get());

        double max2 = stats.getMax();

        yProportion = (max2 - stats.getMin()) / ybins.get();

        List<String> list = dataframe.list(countryNameColumn, String.class);
        double j = (maxLayout - layout.get()) / bins.doubleValue();
        double j2 = (maxLayout - layout.get()) / ybins.get();
        boolean colorEmpty = colors.isEmpty();

        List<Color> generateRandomColors = PieGraph.generateRandomColors(list.size());
        for (int i = 0; i < list.size(); i++) {
            String labelRow = list.get(i);
            Color value = colors.get(labelRow);
            if (!colors.containsKey(labelRow)) {
                if (!colorEmpty) {
                    continue;
                }
                value = generateRandomColors.get(i);
                colors.put(labelRow, value);
            }

            Map<String, Object> row = dataframe.rowMap(i);
            drawPoints(maxYear, minYear, layout.get(), j, j2, labelRow, value, row);

            drawLines(maxYear, minYear, layout.get(), j, j2, row);
        }
		drawAxis();
	}

    private boolean drawPoints(int maxYear, int minYear, double d, double j, double j2, String labelRow, Color value,
            Map<String, Object> row) {
        gc.setFill(value);
        gc.setStroke(value);
        boolean hasPoint = false;
        for (int year = minYear; year <= maxYear; year++) {
            Number object = (Number) row.get("" + year);
            if (object == null) {
                continue;
            }
            hasPoint = true;
            double k = (year - minYear) / xProportion.get();
            double x1 = k * j + d;
            double y = object.doubleValue() - stats.getMin();
            double y1 = maxLayout - y / yProportion * j2;
            // gc.strokeLine(x1, maxLayout, x1, y1)
            double h = radius.get();
            gc.fillOval(x1 - h / 2, y1 - h / 2, h, h);
        }
        if (!hasPoint) {
            colors.remove(labelRow);
        }
        return hasPoint;
    }

    private void drawLines(int maxYear, int minYear, double d, double j, double j2, Map<String, Object> row) {
        for (int year = minYear; year <= maxYear; year++) {
            double x = (double) year - minYear;
            double m = x / xProportion.get();
            double x1 = m * j + d;
            Number object = (Number) row.get("" + year);
            if (object == null) {
                continue;
            }
            double y = object.doubleValue() - stats.getMin();
            double y1 = maxLayout - y / yProportion * j2;
            int searchYear = year;
            while (searchYear < maxYear) {
                double x2 = ++searchYear - (double) minYear;
                double i2 = x2 / xProportion.get();
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

	public void drawAxis() {

        gc.setLineWidth(1);
        gc.setStroke(Color.BLACK);
        gc.setFill(Color.BLACK);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(title, layout.get() + (maxLayout - layout.get()) / 2, layout.get() - 20);
		double e = layout.get();
		gc.strokeLine(e, e, e, maxLayout);
		gc.strokeLine(e, maxLayout, maxLayout, maxLayout);
		double j = (maxLayout - e) / bins.get();
		double d = lineSize.get();
		for (int i = 1; i <= bins.get(); i++) {
			double x1 = i * j + e;
            gc.strokeLine(x1, maxLayout, x1, maxLayout + 5);
            String xLabel = String.format("%.0f", i * xProportion.get() + colStats.getMin());
            gc.strokeText(xLabel, x1,
                    maxLayout + 5 * (4 + 3 * (i % 2)));

		}
		j = (maxLayout - e) / ybins.get();
		for (int i = 0; i <= ybins.get(); i++) {
			double y1 = maxLayout - i * j;
            gc.strokeLine(e, y1, e - 5, y1);
            String yLabel = String.format("%.1f", i * yProportion + stats.getMin());
            gc.strokeText(yLabel, e - d * 2, y1);
		}
	}

	public final DoubleProperty layoutProperty() {
		return layout;
	}

	public final DoubleProperty lineSizeProperty() {
		return lineSize;
	}

	public final IntegerProperty binsProperty() {
		return bins;
	}

    public DoubleProperty xProportionProperty() {
        return xProportion;
    }

	public final IntegerProperty ybinsProperty() {
		return ybins;
	}

	public final IntegerProperty radiusProperty() {
		return radius;
	}

	public ObservableMap<String, Color> colorsProperty() {
		return colors;
	}

    public void setTitle(String title) {
        this.title = title;
    }
}