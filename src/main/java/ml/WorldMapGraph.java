package ml;

import java.util.DoubleSummaryStatistics;

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

class WorldMapGraph extends Canvas {
    private DoubleProperty layout = new SimpleDoubleProperty(30);
    private DoubleProperty lineSize = new SimpleDoubleProperty(5);
    private IntegerProperty bins = new SimpleIntegerProperty(20);
    private IntegerProperty ybins = new SimpleIntegerProperty(20);
    private GraphicsContext gc;
    private ObservableMap<String, DoubleSummaryStatistics> stats = FXCollections.observableHashMap();
    private ObservableMap<String, Color> colors = FXCollections.observableHashMap();
    private DataframeML dataframeML;

    public WorldMapGraph() {
        super(2000, 1200);
        gc = getGraphicsContext2D();
        InvalidationListener listener = observable -> drawGraph();
        stats.addListener(listener);
        colors.addListener(listener);
        lineSize.addListener(listener);
        layout.addListener(listener);
        bins.addListener(listener);
        ybins.addListener(listener);
        drawGraph();
    }

    private final static double BLUE_HUE = Color.BLUE.getHue();
    private final static double RED_HUE = Color.RED.getHue();

    private Color getColorForValue(double value, DoubleSummaryStatistics sum) {
        if (value < sum.getMin() || value > sum.getMax()) {
            return Color.BLACK;
        }
        double hue = BLUE_HUE + (RED_HUE - BLUE_HUE) * (value - sum.getMin()) / (sum.getMax() - sum.getMin());
        return Color.hsb(hue, 1.0, 1.0);
    }

    public void drawGraph() {
        gc.clearRect(0, 0, getWidth(), getHeight());
        Countries[] values = Countries.values();
        gc.setFill(Color.BLACK);
        gc.setStroke(Color.WHITE);
        if (dataframeML != null) {
            DoubleSummaryStatistics summary = dataframeML.summary("Value");
            for (int i = 0; i < values.length; i++) {
                Countries countries = values[i];
                gc.setFill(Color.BLACK);
                gc.beginPath();
                String countryName = countries.getCountryName();
                dataframeML.only("Country", countryName::equalsIgnoreCase, (j) -> {
                    Number object2 = (Number) dataframeML.list("Value").get(j);
                    gc.setFill(getColorForValue(object2.doubleValue(), summary));
                });
                gc.appendSVGPath(countries.getPath());
                gc.fill();
                gc.stroke();
                gc.closePath();

            }
        }
    }

    public void setDataframe(DataframeML x) {
        this.dataframeML = x;
        drawGraph();
    }

}