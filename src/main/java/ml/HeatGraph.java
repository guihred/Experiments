package ml;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.HashMap;
import java.util.IntSummaryStatistics;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javafx.beans.InvalidationListener;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;

public class HeatGraph extends Canvas {
    private static final double SQR_ROOT_OF_3 = Math.sqrt(3);
	private final DoubleProperty layout = new SimpleDoubleProperty(30);
	private double maxLayout = 480;
	private final DoubleProperty lineSize = new SimpleDoubleProperty(5);
	private final IntegerProperty bins = new SimpleIntegerProperty(20);
	private final IntegerProperty ybins = new SimpleIntegerProperty(20);
	private double xProportion;
	private double yProportion;
	private final StringProperty xHeader = new SimpleStringProperty();
	private final StringProperty yHeader = new SimpleStringProperty();

	private GraphicsContext gc;
	private ObservableMap<String, DoubleSummaryStatistics> stats = FXCollections.observableHashMap();

	private DoubleProperty radius = new SimpleDoubleProperty(30);
    private DataframeML data;
    private String title;

    public HeatGraph() {
        super(550, 550);
        gc = getGraphicsContext2D();
        drawGraph();
        InvalidationListener listener = observable -> drawGraph();
        stats.addListener(listener);
        xHeader.addListener(listener);
        yHeader.addListener(listener);
        lineSize.addListener(listener);
        bins.addListener(listener);
        ybins.addListener(listener);
        radius.addListener(listener);
        layout.addListener(listener);
    }

	public ObservableMap<String, DoubleSummaryStatistics> statsProperty() {
		return stats;
	}

    public void drawAxis(DoubleSummaryStatistics xStats, DoubleSummaryStatistics yStats) {

        gc.setFill(Color.BLACK);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(title, layout.get() + (maxLayout - layout.get()) / 2, layout.get() - 20);
        double e = layout.get();
        gc.strokeLine(e, e, e, maxLayout);
        gc.strokeLine(e, maxLayout, maxLayout, maxLayout);
        double j = (maxLayout - e) / bins.get();
        double d = lineSize.get();

        double min = xStats.getMin();

        for (int i = 1; i <= bins.get(); i++) {
            double x1 = i * j + e;
            gc.strokeLine(x1, maxLayout, x1, maxLayout + 5);
            String xLabel = String.format("%.0f", i * xProportion + min);
            gc.strokeText(xLabel, x1, maxLayout + 5 * (4 + 3 * (i % 2)));

        }
        min = yStats.getMin();
        j = (maxLayout - e) / ybins.get();
        for (int i = 0; i <= ybins.get(); i++) {
            double y1 = maxLayout - i * j;
            gc.strokeLine(e, y1, e - 5, y1);
            String yLabel = String.format("%.1f", i * yProportion + min);
            gc.strokeText(yLabel, e - d * 2, y1);
        }
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void drawGraph() {
        DoubleSummaryStatistics xStats = stats.get(xHeader.get());
        DoubleSummaryStatistics yStats = stats.get(yHeader.get());
        if (xStats == null || yStats == null) {
            return;
        }
        gc.clearRect(0, 0, 550, 550);
        double max = xStats.getMax();
        double min = xStats.getMin();
        xProportion = (max - min) / bins.intValue();
        double max2 = yStats.getMax() + 0.1;
        double min2 = yStats.getMin() - 0.1;
        yProportion = (max2 - min2) / ybins.intValue();

        List<Object> entrySetX = data.list(xHeader.get());
        List<Object> entrySetY = data.list(yHeader.get());
        gc.setLineWidth(5);
        gc.setFill(Color.GREEN);
        gc.setLineWidth(0.5);
		Map<double[], Integer> hashMap = new HashMap<>();
        List<double[]> triangles = triangles();
        for (int k = 0; k < data.getSize(); k++) {
			double finalX = finalX(xStats, entrySetX.get(k));
			double finalY = finalY(yStats, entrySetY.get(k));
			double[] orElse = triangles.stream().min(Comparator.comparing(d -> {
				double e = d[0] - finalX;
				double f = d[1] - finalY;
				return e * e + f * f;
			})).orElse(triangles.get(0));

			hashMap.put(orElse, hashMap.getOrDefault(orElse, 0) + 1);

        }

		IntSummaryStatistics stats1 = hashMap.values().stream().mapToInt(e -> e).summaryStatistics();

		for (double[] es : triangles) {
			gc.setFill(getColorForValue(hashMap.getOrDefault(es, 0), stats1.getMin(), stats1.getMax()));
			gc.fillOval(es[0], es[1], radius.doubleValue(), radius.doubleValue());
		}

        drawAxis(xStats, yStats);
    }

	private static final double RED_HUE = Color.RED.getHue();
	public static final double BLUE_HUE = Color.BLUE.getHue();

	private Color getColorForValue(double value, int min, int max) {
		if (value < min || value > max) {
            return Color.TRANSPARENT;
		}
		double hue = BLUE_HUE + (RED_HUE - BLUE_HUE) * (value - min) / (max - min);
		return Color.hsb(hue, 0.5, 1.0);
        // double brightness = 1 - (value - sum.getMin()) / (sum.getMax() - sum.getMin())
        // return Color.hsb(RED_HUE, 1.0, brightness)
        // double saturation = (value - min) / (max - min)
        // return Color.hsb(RED_HUE, saturation, 1.0)
	}

	private List<double[]> triangles() {
		List<double[]> arrayList = new ArrayList<>();
		
		double width = maxLayout - layout.doubleValue();
		int sqrt = (int) (width / radius.get());
		double triangleSide = width/ sqrt;
		int m = (int) (width/ triangleSide / SQR_ROOT_OF_3 * 2) + 1;
		int size = sqrt * m;
		for (int i = 0; i < size; i++) {

			double x = i % sqrt * triangleSide + (i / sqrt % 2 == 0 ? 0 : -triangleSide / 2) + radius.get() / 2
					+ layout.get();
			int j = i / sqrt;
			double k = j * triangleSide;
			double y = k * SQR_ROOT_OF_3 / 2 + layout.get() - radius.get() / 2;
			arrayList.add(new double[] {x,y});
		}
		return arrayList;
	}

	private double finalX(DoubleSummaryStatistics xStats, Object object) {
		double j = (maxLayout - layout.doubleValue()) / bins.intValue();
		double x = ((Number) object).doubleValue();
		double x1 = (x - xStats.getMin()) / xProportion * j + layout.doubleValue();
		return x1 - radius.doubleValue() / 2;
	}

	private double finalY(DoubleSummaryStatistics yStats, Object object2) {
		double j2 = (maxLayout - layout.doubleValue()) / ybins.intValue();
		double y = ((Number) object2).doubleValue();
		double y1 = maxLayout - (y - yStats.getMin()) / yProportion * j2;
		return y1 - radius.doubleValue() / 2;
	}


    public void setDatagram(DataframeML x) {
        data = x;
		data.forEach((col, items) -> {
            DoubleSummaryStatistics summaryStatistics = items.stream().map(Number.class::cast)
                    .mapToDouble(Number::doubleValue).summaryStatistics();
            stats.put(col, summaryStatistics);
        });
		Iterator<String> iterator = data.cols().iterator();
        if (iterator.hasNext()) {
            xHeader.set(iterator.next());
        }
        if (iterator.hasNext()) {
            yHeader.set(iterator.next());
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

	public final StringProperty xHeaderProperty() {
		return xHeader;
	}

	public final String getXHeader() {
		return xHeaderProperty().get();
	}

	public final void setXHeader(final String xHeader) {
		xHeaderProperty().set(xHeader);
	}

	public final StringProperty yHeaderProperty() {
		return yHeader;
	}

	public final String getYHeader() {
		return yHeaderProperty().get();
	}

	public final void setYHeader(final String yHeader) {
		yHeaderProperty().set(yHeader);
	}

	public final DoubleProperty radiusProperty() {
		return radius;
	}

	public final double getRadius() {
		return radiusProperty().get();
	}

	public final void setRadius(final double radius) {
		radiusProperty().set(radius);
	}

}