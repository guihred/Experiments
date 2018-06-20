package ml;

import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javafx.beans.InvalidationListener;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import simplebuilder.CommonsFX;

class WorldMapGraph extends Canvas {
    //    private final static double BLUE_HUE = Color.BLUE.getHue();
    private static final double RED_HUE = Color.RED.getHue();
    private StringProperty valueHeader = new SimpleStringProperty("Value");
    private IntegerProperty bins = new SimpleIntegerProperty(7);
    private GraphicsContext gc;
    private ObservableMap<String, DoubleSummaryStatistics> stats = FXCollections.observableHashMap();
    private DataframeML dataframeML;
    private boolean showNeighbors = false;
    private DoubleSummaryStatistics summary;
    private String header = "Country";
    private Map<String, Predicate<Object>> filters = new HashMap<>();

    //    private double delta = 0.1;
    //    private double iniX;
    //    private double iniY;
    //    private Scale scale = new Scale(1, 1, 0, 0);
    //    private Translate translate = new Translate(0, 0);
    public WorldMapGraph() {
        super(2000, 1200);
        gc = getGraphicsContext2D();
        InvalidationListener listener = observable -> drawGraph();
        stats.addListener(listener);
        valueHeader.addListener(listener);
        bins.addListener(listener);
        drawGraph();
        CommonsFX.setZoomable(this);
        //        getTransforms().addAll(scale, translate);
        //        setOnScroll(scrollEvent -> {
        //            double scaleValue = scale.getX();
        //            double s = scaleValue;
        //            if (scrollEvent.getDeltaY() < 0) {
        //                scaleValue -= delta;
        //            } else {
        //                scaleValue += delta;
        //            }
        //            if (scaleValue <= 0.1) {
        //                scaleValue = s;
        //            }
        //            scale.setX(scaleValue);
        //            scale.setY(scaleValue);
        //            scrollEvent.consume();
        //        });
        //
        //        setOnMousePressed(evt -> {
        //            iniX = evt.getX();
        //            iniY = evt.getY();
        //        });
        //
        //        setOnMouseDragged(evt -> {
        //            double deltaX = evt.getX() - iniX;
        //            double deltaY = evt.getY() - iniY;
        //            translate.setX(translate.getX() + deltaX);
        //            translate.setY(translate.getY() + deltaY);
        //        });

    }

    public List<Country> anyAdjacents(Country c) {
        Country[] values = Country.values();
        return Stream.of(values).filter(e -> e.neighbors().contains(c)).flatMap(e -> Stream.of(e, c))
                .filter(e -> e != c).distinct()
                .collect(Collectors.toList());
    }

    public IntegerProperty binsProperty() {
        return bins;
    }

    public void coloring() {

        Country[] values = Country.values();
        List<Color> availableColors = PieGraph.generateColors(10);
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
        if (summary == null && dataframeML != null) {
            summary = dataframeML.summary(valueHeader.get());
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
                    Number object2 = (Number) dataframeML.list(valueHeader.get()).get(j);
                    countries.setColor(getColorForValue(object2.doubleValue(), summary));
                });
            }
            gc.setFill(countries.getColor() != null ? countries.getColor() : Color.GRAY);
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



    private void drawLabels() {
        gc.setFill(Color.GRAY);
        double x = 50;
        double y = getHeight() / 2;
        double step = 20;
        gc.fillRect(x - 5, y - 5, getWidth() / 20, step * bins.get() + step);
        double max = summary.getMax();
        double min = summary.getMin();
        int millin = 100000;
        summary.accept(Math.floor(min / millin) * millin);
        summary.accept(Math.ceil(max / millin) * millin);
        max = summary.getMax();
        min = summary.getMin();
        double h = (max - min) / bins.get();
        gc.setFill(Color.GRAY);
        gc.setStroke(Color.BLACK);
        for (int i = 0; i <= bins.get(); i++) {
            double s = Math.floor((min + i * h) / millin) * millin;
            gc.setFill(getColorForValue(s, summary));
            gc.fillRect(x, y + step * i, 10, 10);
            gc.strokeText(String.format("%11.0f", s), x + 15, y + step * i + 10);
        }

    }

    public void filter(String h, Predicate<Object> pred) {
        filters.put(h, pred);
    }

    private Color getColorForValue(double value, DoubleSummaryStatistics sum) {
        if (value < sum.getMin() || value > sum.getMax()) {
            return Color.BLACK;
        }
//        double hue = BLUE_HUE + (RED_HUE - BLUE_HUE) * (value - sum.getMin()) / (sum.getMax() - sum.getMin());
        //        return Color.hsb(hue, 1.0, 1.0);
//        double brightness = 1 - (value - sum.getMin()) / (sum.getMax() - sum.getMin());
//        return Color.hsb(RED_HUE, 1.0, brightness);
        double saturation = 0.25 + (value - sum.getMin()) / (sum.getMax() - sum.getMin()) * 0.75;
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

}