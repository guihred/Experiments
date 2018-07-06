package ml;

import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.DoubleBinaryOperator;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import javafx.beans.InvalidationListener;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import simplebuilder.CommonsFX;

class WorldPointMapGraph2 extends Canvas {
    private static final String NO_INFO = "No info";
    public static final double BLUE_HUE = Color.BLUE.getHue();
    private static final double RED_HUE = Color.RED.getHue();
    private StringProperty valueHeader = new SimpleStringProperty("Value");
    private IntegerProperty bins = new SimpleIntegerProperty(7);
    private GraphicsContext gc;
    private DataframeML dataframeML;
    private boolean showNeighbors = false;
    private DoubleSummaryStatistics summary;
    private String header = "Country";
    private Map<String, Predicate<Object>> filters = new HashMap<>();
    private Map<String, Color> categoryMap = new HashMap<>();
    private double max;
    private double min;

    public WorldPointMapGraph2() {
        super(2000, 1200);
        gc = getGraphicsContext2D();
        InvalidationListener listener = observable -> drawGraph();
        valueHeader.addListener(listener);
        bins.addListener(listener);
        drawGraph();
        CommonsFX.setZoomable(this);

    }

    public List<Country> anyAdjacents(Country c) {
        Country[] values = Country.values();
        return Stream.of(values).filter(e -> e.neighbors().contains(c)).flatMap(e -> Stream.of(e, c))
                .filter(e -> e != c).distinct().collect(Collectors.toList());
    }

    public void coloring() {

        Country[] values = Country.values();
        List<Color> availableColors = PieGraph.generateRandomColors(10);
        int i = 0;
        List<Country> vertices = Stream.of(values)
                .sorted(Comparator.comparing((Country e) -> e.neighbors().size()).reversed())
                .peek(p -> p.setColor(null)).collect(Collectors.toList());
        while (vertices.stream().anyMatch(v -> v.getColor() == null)) {
            List<Country> v = vertices.stream().filter(c -> c.getColor() == null).collect(Collectors.toList());
            Color color = availableColors.get(i);
            for (int j = 0; j < v.size(); j++) {
                if (anyAdjacents(v.get(j)).stream().noneMatch(c -> c.getColor() == color)) {
                    v.get(j).setColor(color);
                }
            }
            i = (i + 1) % availableColors.size();
        }
        drawGraph();
    }

    public void drawGraph() {
        gc.clearRect(0, 0, getWidth(), getHeight());
        Country[] values = Country.values();
        gc.setFill(Color.BLACK);
        gc.setStroke(Color.BLACK);
        if (summary == null && dataframeML != null && dataframeML.getFormat(valueHeader.get()) != String.class) {
            summary = dataframeML.summary(valueHeader.get());
        } else if (dataframeML != null && dataframeML.getFormat(valueHeader.get()) == String.class) {
            createCategoryMap();
        }
        if (dataframeML != null) {
            drawLabels();
        }
        for (int i = 0; i < values.length; i++) {
            Country countries = values[i];
            gc.beginPath();
            if (dataframeML != null) {
                dataframeML.only(header, t -> countries.matches(t), j -> {
                    Set<Entry<String, Predicate<Object>>> entrySet = filters.entrySet();
                    for (Entry<String, Predicate<Object>> fil : entrySet) {
                        Object t = dataframeML.list(fil.getKey()).get(j);
                        if (!fil.getValue().test(t)) {
                            return;
                        }
                    }
                    List<Object> list = dataframeML.list(valueHeader.get());
                    Object object = DataframeML.getFromList(j, list);
                    if (object instanceof Number) {
                        countries.setColor(getColorForValue(((Number) object).doubleValue()));
                    } else if (object instanceof String) {
                        countries.setColor(categoryMap.get(object));
                    }
                });
            }
            gc.setFill(countries.getColor() != null ? countries.getColor() : categoryMap.get(NO_INFO));
            gc.appendSVGPath(countries.getPath());
            gc.fill();
            gc.stroke();
            gc.closePath();
        }

        if (showNeighbors) {
            gc.setStroke(Color.RED);
            gc.setLineWidth(1);
            for (int i = 0; i < values.length; i++) {
                Country countries = values[i];
                for (Country country : countries.neighbors()) {
                    gc.strokeLine(countries.getCenterX(), countries.getCenterY(), country.getCenterX(),
                            country.getCenterY());
                }
            }
        }
    }

    private void latToXY(double phi, double lambda) {
        double lambda0 = 0;
        double a = 6378.137;
        double f = 1 / 298.257223563;
        double n = f / (2 - f);
        double a1 = n / 2 - 2 * n * n / 3 + 5 * n * n * n / 16;
        double a2 = 13 * n * n / 48 - 3 * n * n * n / 3;
        double a3 = 61 * n * n * n / 240;
        double t = Math.sinh(
                atanh(Math.sin(phi)) - 2 * Math.sqrt(n) / (1 + n) * atanh(2 * Math.sqrt(n) / (1 + n) * Math.sin(phi)));
        double xsi = Math.atan(t / Math.cos(lambda - lambda0));
        double eta = atanh(Math.sin(lambda - lambda0) / Math.sqrt(1 + t * t));

        DoubleBinaryOperator o = (j, b) -> 2 * j * b * Math.cos(2 * j * xsi) * Math.cosh(2 * j * eta);
        double sigma = 1 + sum(o.applyAsDouble(1, a1), o.applyAsDouble(2, a2), o.applyAsDouble(3, a3));
        DoubleBinaryOperator i = (j, b) -> 2 * j * b * Math.sin(2 * j * xsi) * Math.sinh(2 * j * eta);
        double tau = sum(i.applyAsDouble(1, a1), i.applyAsDouble(2, a2), i.applyAsDouble(3, a3));
        //        double E = 

        sum(sigma, tau, a);
    }

    private double sum(double... x) {
        double sum0 = 0;
        for (int i = 0; i < x.length; i++) {
            sum0 += x[i];
        }
        return sum0;
    }

    double atanh(double x) {
        return 0.5 * Math.log((x + 1.0) / (x - 1.0));
    }

    private void createCategoryMap() {
        Set<String> categorize = dataframeML.categorize(valueHeader.get());
        categorize.removeIf(StringUtils::isBlank);

        if (categorize.size() == categoryMap.size() && categoryMap.keySet().equals(categorize)) {
            return;
        }

        List<Color> generateColors = PieGraph.generateColors(categorize.size() + 1);
        int k = 0;
        List<String> collect = categorize.stream().sorted().collect(Collectors.toList());
        for (String label : collect) {
            categoryMap.put(label, generateColors.get(k));
            k++;
        }
        categoryMap.put(NO_INFO, generateColors.get(k));
    }

    private void drawLabels() {
        gc.setFill(Color.GRAY);
        double x = 50;
        double y = getHeight() / 2;
        double step = 20;
        if (summary != null) {
            createNumberLabels(x, y, step);
        } else {
            createCategoryLabels(x, y, step);
        }
    }

    private void createCategoryLabels(double x, double y, double step) {

        gc.setFill(Color.GRAY);
        gc.setStroke(Color.BLACK);
        List<Entry<String, Color>> collect = categoryMap.entrySet().stream()
                .sorted(Comparator.comparing(Entry<String, Color>::getKey)).collect(Collectors.toList());
        for (int i = 0; i < collect.size(); i++) {
            Entry<String, Color> entry = collect.get(i);
            gc.setFill(entry.getValue());
            gc.fillRect(x, y + step * i, 10, 10);
            gc.strokeRect(x, y + step * i, 10, 10);
            gc.strokeText(entry.getKey(), x + 15, y + step * i + 10);
        }
    }

    private void createNumberLabels(double x, double y, double step) {
        gc.fillRect(x - 5, y - 5, getWidth() / 20, step * bins.get() + step);
        int millin = 100000;
        min = Math.floor(summary.getMin() / millin) * millin;
        max = Math.ceil(summary.getMax() / millin) * millin;
        double h = (max - min) / bins.get();
        gc.setFill(Color.GRAY);
        gc.setStroke(Color.BLACK);
        for (int i = 0; i <= bins.get(); i++) {
            double s = Math.floor((min + i * h) / millin) * millin;
            gc.setFill(getColorForValue(s));
            gc.fillRect(x, y + step * i, 10, 10);
            gc.strokeText(String.format("%11.0f", s), x + 15, y + step * i + 10);
        }

        categoryMap.put(NO_INFO, Color.GRAY);
    }

    public void filter(String h, Predicate<Object> pred) {
        filters.put(h, pred);
    }

    private Color getColorForValue(double value) {
        if (value < min || value > max) {
            return Color.BLACK;
        }
        //        double hue = BLUE_HUE + (RED_HUE - BLUE_HUE) * (value - sum.getMin()) / (sum.getMax() - sum.getMin());
        //        return Color.hsb(hue, 1.0, 1.0);
        //        double brightness = 1 - (value - sum.getMin()) / (sum.getMax() - sum.getMin());
        //        return Color.hsb(RED_HUE, 1.0, brightness);
        double saturation = (value - min) / (max - min);
        return Color.hsb(RED_HUE, saturation, 1.0);
    }

    public void setDataframe(DataframeML x, String header) {
        this.header = header;
        this.dataframeML = x;
        drawGraph();
    }

    public StringProperty valueHeaderProperty() {
        return valueHeader;
    }

    public IntegerProperty binsProperty() {
        return bins;
    }
}