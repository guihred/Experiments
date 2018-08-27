package ml;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.beans.InvalidationListener;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.apache.commons.lang3.StringUtils;
import simplebuilder.CommonsFX;

class WorldMapGraph extends Canvas {
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

    public WorldMapGraph() {
        super(2000, 1200);
        gc = getGraphicsContext2D();
        InvalidationListener listener = observable -> drawGraph();
        valueHeader.addListener(s -> {
            summary = null;
            categoryMap.clear();
            drawGraph();
        });
        bins.addListener(listener);
        drawGraph();
        CommonsFX.setZoomable(this);

    }

    public List<Country> anyAdjacents(Country c) {
        return Stream.of(Country.values()).filter(e -> e.neighbors().contains(c)).flatMap(e -> Stream.of(e, c))
                .filter(e -> e != c).distinct().collect(Collectors.toList());
    }

    public final void drawGraph() {
        gc.clearRect(0, 0, getWidth(), getHeight());
        Country[] values = Country.values();
        gc.setStroke(Color.BLACK);
        gc.setFill(Color.BLACK);
        if (dataframeML != null) {
            dataframeML.filterString(header, Country::hasName);
        }

        if (isSuitableForSummary()) {
            summary = dataframeML.summary(valueHeader.get());
        } else if (isSuitableForCategory()) {
            createCategoryMap();
        }
        if (dataframeML != null) {
            drawLabels();
        }
        for (int i = 0; i < values.length; i++) {
            Country countries = values[i];
            gc.beginPath();
            if (dataframeML != null) {
                countries.setColor(null);
                dataframeML.only(header, countries::matches, j -> {
                    Set<Entry<String, Predicate<Object>>> entrySet = filters.entrySet();
                    for (Entry<String, Predicate<Object>> fil : entrySet) {
                        Object t = dataframeML.list(fil.getKey()).get(j);
                        if (!fil.getValue().test(t)) {
                            return;
                        }
                    }
                    List<Object> list = dataframeML.list(valueHeader.get());
                    Object object = DataframeML.getFromList(j, list);
                    countries.setColor(getColor(countries, object));
                });
            }
            gc.setFill(countries.getColor() != null ? countries.getColor() : categoryMap.get(NO_INFO));
            gc.appendSVGPath(countries.getPath());
            gc.fill();
            gc.stroke();
            gc.closePath();
        }

        if (showNeighbors) {
            drawLinks(values);
        }
    }

    private void drawLinks(Country[] values) {
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

    private Color getColor(Country countries, Object object) {
        Color color = countries.getColor();
        if (object instanceof Number) {
            color = getColorForValue(((Number) object).doubleValue());
        } else if (object instanceof String) {
            color = categoryMap.get(object);
        }
        return color;
    }

    private boolean isSuitableForCategory() {
        return dataframeML != null && dataframeML.getFormat(valueHeader.get()) == String.class;
    }

    private boolean isSuitableForSummary() {
        return summary == null && dataframeML != null && dataframeML.getFormat(valueHeader.get()) != String.class;
    }


    private void createCategoryMap() {
        Set<String> categorize = dataframeML.categorize(valueHeader.get());
        categorize.removeIf(StringUtils::isBlank);

        if (categorize.size() == categoryMap.size() && categoryMap.keySet().equals(categorize)) {
            return;
        }

        List<Color> generateColors = PieGraph.generateRandomColors(categorize.size() + 1);
        int k = 0;
        for (String label : categorize) {
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
        int millin = 1;
        min = Math.floor(summary.getMin() / millin) * millin;
        max = Math.ceil(summary.getMax() / millin) * millin;
        double h = (max - min) / bins.get();
        gc.setFill(Color.GRAY);
        gc.setStroke(Color.BLACK);
        for (int i = 0; i <= bins.get(); i++) {
            double s = Math.floor((min + i * h) / millin) * millin;
            if (h < 2) {
                s = min + i * h;
                gc.strokeText(String.format("%11.2f", s), x + 15, y + step * i + 10);
                gc.setFill(getColorForValue(s));
                gc.fillRect(x, y + step * i, 10, 10);
            } else {
                gc.setFill(getColorForValue(s));
                gc.fillRect(x, y + step * i, 10, 10);
                gc.strokeText(String.format("%11.0f", s), x + 15, y + step * i + 10);
            }
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
        //        double hue = BLUE_HUE + (RED_HUE - BLUE_HUE) * (value - sum.getMin()) / (sum.getMax() - sum.getMin())
        //        return Color.hsb(hue, 1.0, 1.0)
        //        double brightness = 1 - (value - sum.getMin()) / (sum.getMax() - sum.getMin())
        //        return Color.hsb(RED_HUE, 1.0, brightness)
        double saturation = (value - min) / (max - min);
        return Color.hsb(RED_HUE, saturation, 1.0);
    }

    public void setDataframe(DataframeML x, String header) {
        this.header = header;
        this.dataframeML = x;
        this.summary = null;
        drawGraph();
    }

    public StringProperty valueHeaderProperty() {
        return valueHeader;
    }

    public IntegerProperty binsProperty() {
        return bins;
    }
}