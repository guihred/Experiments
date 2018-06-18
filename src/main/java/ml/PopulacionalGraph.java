package ml;

import java.util.DoubleSummaryStatistics;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javafx.beans.InvalidationListener;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

class PopulacionalGraph extends Canvas {
	private DoubleProperty layout = new SimpleDoubleProperty(30);
	private DoubleProperty maxLayout = new SimpleDoubleProperty(480);
	private DoubleProperty lineSize = new SimpleDoubleProperty(1);
	private IntegerProperty bins = new SimpleIntegerProperty(5);
	private LongProperty ybins = new SimpleLongProperty(5);
	private String ageHeader = "Subject", sexHeader = "SEX", yearHeader = "TIME", valueHeader = "Value";
	private StringProperty country = new SimpleStringProperty("Germany");
	private IntegerProperty year = new SimpleIntegerProperty(2000);
	private double xProportion;
	private double yProportion;
	private GraphicsContext gc;
    private DataframeML dataframe;
	private final ObservableMap<String, Color> colors = FXCollections.observableHashMap();
	private String countryHeader = "Country";

    public PopulacionalGraph() {
        super(550, 550);
        gc = getGraphicsContext2D();
        drawGraph();
		lineSize.set(getHeight() / getWidth());
        InvalidationListener listener = observable -> drawGraph();
        colors.addListener(listener);
        maxLayout.addListener(listener);
        lineSize.addListener(listener);
        bins.addListener(listener);
        layout.addListener(listener);
    }

    public void setHistogram(DataframeML dataframe) {
        this.dataframe = dataframe;
		drawGraph();
    }

    public void drawGraph() {
		gc.clearRect(0, 0, getWidth(), getHeight());
        if (dataframe == null) {
            drawAxis();
            return;

        }
		gc.setFill(Color.BLUE);
		System.out.println(dataframe);
		gc.scale(1, lineSize.doubleValue());
		DoubleSummaryStatistics peopleStats = new DoubleSummaryStatistics();
		List<Number> values = dataframe.list(valueHeader, Number.class);
		List<String> sexes = dataframe.list(sexHeader, String.class);
		List<String> ages = dataframe.list(ageHeader, String.class);
		List<Integer> years = dataframe.list(yearHeader, Integer.class);
		Map<String, Number> possibleAgesMA = new HashMap<>();
		Map<String, Number> possibleAgesFE = new HashMap<>();
		dataframe.only(countryHeader, t -> t.equals(country.get()), j -> {
			if (years.get(j) != year.get()) {
				return;
			}
			Number number = values.get(j);
			peopleStats.accept(number.doubleValue());
			String sex = sexes.get(j);
			if ("MA".equals(sex)) {
				possibleAgesMA.put(ages.get(j), number);
			} else {
				possibleAgesFE.put(ages.get(j), number);
			}
		});
		double layout1 = layout.get();
		double maxLayout1 = maxLayout.get();
		double xMA = prop(layout1, maxLayout1, 0.45);
		double xFE = prop(layout1, maxLayout1, 0.55);
		List<String> agesLa = possibleAgesFE.keySet().stream().distinct().sorted().collect(Collectors.toList());
		double j = (maxLayout1 - layout1) / agesLa.size();
		double max = peopleStats.getMax();
		xProportion = max / bins.doubleValue();
		ybins.set(agesLa.size());
		for (int i = 0; i < agesLa.size(); i++) {
			double y1 = maxLayout1 - (i + 1) * j;
			double h = (maxLayout1 - layout1) / agesLa.size() - 2;
			String strip = agesLa.get(i);
			drawRectangle(possibleAgesFE, maxLayout1, xFE, strip, y1, h, max, Color.RED);
			drawRectangle(possibleAgesMA, xMA, layout1, strip, y1, h, max, Color.BLUE);
		}
		gc.scale(1, 1 / lineSize.doubleValue());
		System.out.println(possibleAgesFE);
		System.out.println(possibleAgesMA);
		drawAxis();

    }

	private void drawRectangle(Map<String, Number> possibleAgesFE, double maxLayout1, double xFE, String strip,
			double y1, double h, double max, Color color) {
		Number value = possibleAgesFE.getOrDefault(strip, 0);
		double w = (maxLayout1 - xFE) * value.doubleValue() / max;
		gc.setFill(color);

		if (color.equals(Color.RED)) {
			gc.fillRect(xFE, y1, w, h);
		} else {
			gc.fillRect(maxLayout1-w, y1, w, h);
		}
	}

    public void drawAxis() {
        double layout1 = layout.get();
		gc.scale(1, lineSize.doubleValue());
        double xbins = bins.get();

        gc.setLineWidth(1);
        double maxLayout1 = maxLayout.get();
		double lineSize1 = 5;
        gc.strokeLine(layout1, maxLayout1, maxLayout1, maxLayout1);
		double xMid = prop(layout1, maxLayout1, 0.5);
		double xMA = prop(layout1, maxLayout1, 0.45);
		double xFE = prop(layout1, maxLayout1, 0.55);
		gc.strokeLine(xMA, layout1, xMA, maxLayout1);
		gc.strokeLine(xFE, layout1, xFE, maxLayout1);
		prop(layout1, maxLayout1, 0.5);
		double j = (xMA - layout1) / xbins;
		for (int i = 0; i <= xbins; i++) {
            double x1 = i * j + layout1;
            gc.strokeLine(x1, maxLayout1, x1, maxLayout1 + lineSize1);
            String xLabel = String.format("%.1f", i * xProportion);
			gc.strokeText(xLabel, x1 - lineSize1 * xLabel.length() / 2, maxLayout1 + lineSize1 * (4 + 3 * (i % 2)));

        }
		j = (maxLayout1 - xFE) / xbins;
		for (int i = 0; i <= xbins; i++) {
			double x1 = i * j + xFE;
			gc.strokeLine(x1, maxLayout1, x1, maxLayout1 + lineSize1);
			String xLabel = String.format("%.1f", i * xProportion);
			gc.strokeText(xLabel, x1 - lineSize1 * xLabel.length() / 2, maxLayout1 + lineSize1 * (4 + 3 * (i % 2)));

		}
		// maxLayout1 = getHeight() - layout1;
        j = (maxLayout1 - layout1) / ybins.get();
        for (int i = 1; i <= ybins.get(); i++) {
            double y1 = maxLayout1 - i * j;
			gc.strokeLine(xFE, y1, xFE - lineSize1, y1);
			gc.strokeLine(xMA, y1, xMA + lineSize1, y1);
            String yLabel = String.format("%.0f", i * yProportion);
			gc.strokeText(yLabel, xMid - lineSize1, y1);
        }
		gc.scale(1, 1 / lineSize.doubleValue());
    }

	private double prop(double layout1, double maxLayout1, double d) {
		return layout1 + (maxLayout1 - layout1) * d;
	}

	public ObservableMap<String, Color> colorsProperty() {
		return colors;
	}


	public DoubleProperty lineSizeProperty() {
		return lineSize;
	}

	public DoubleProperty layoutProperty() {
		return layout;
	}

	public DoubleProperty maxLayoutProperty() {
		return maxLayout;
	}

	public IntegerProperty binsProperty() {
		return bins;
	}

	public LongProperty ybinsProperty() {
		return ybins;
	}

}