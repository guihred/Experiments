package ml;

import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javafx.beans.InvalidationListener;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;

class WorldMapGraph extends Canvas {
    private StringProperty valueHeader = new SimpleStringProperty("Value");
    private GraphicsContext gc;
    private ObservableMap<String, DoubleSummaryStatistics> stats = FXCollections.observableHashMap();
    private ObservableMap<String, Color> colors = FXCollections.observableHashMap();
    private DataframeML dataframeML;
    private boolean showNeighbors = false;
    private double scaleValue = 1;
    private double iniX, iniY;
    private double delta = 0.1;
    private Scale scale = new Scale(scaleValue, scaleValue, 0, 0);

    private Translate translate = new Translate(0, 0);
    public WorldMapGraph() {
        super(2000, 1200);
        gc = getGraphicsContext2D();
        InvalidationListener listener = observable -> drawGraph();
        stats.addListener(listener);
        colors.addListener(listener);
        valueHeader.addListener(listener);
        drawGraph();
        getTransforms().addAll(scale, translate);
        setOnScroll(scrollEvent -> {
            double s = scaleValue;
            if (scrollEvent.getDeltaY() < 0) {
                scaleValue -= delta;
            } else {
                scaleValue += delta;
            }
            if (scaleValue <= 0.1) {
                scaleValue = s;
            }
            scale.setX(scaleValue);
            scale.setY(scaleValue);
            scrollEvent.consume();
        });

        setOnMousePressed(evt -> {
            iniX = evt.getX();
            iniY = evt.getY();
        });

        setOnMouseDragged(evt -> {
            double deltaX = evt.getX() - iniX;
            double deltaY = evt.getY() - iniY;
            translate.setX(translate.getX() + deltaX);
            translate.setY(translate.getY() + deltaY);
        });

    }



    private final static double BLUE_HUE = Color.BLUE.getHue();
    private final static double RED_HUE = Color.RED.getHue();
    private DoubleSummaryStatistics summary;
    private String header = "Country";

    private Color getColorForValue(double value, DoubleSummaryStatistics sum) {
        if (value < sum.getMin() || value > sum.getMax()) {
            return Color.BLACK;
        }
        double hue = BLUE_HUE + (RED_HUE - BLUE_HUE) * (value - sum.getMin()) / (sum.getMax() - sum.getMin());
        return Color.hsb(hue, 1.0, 1.0);
    }

    public void drawGraph() {
        gc.clearRect(0, 0, getWidth(), getHeight());
        Country[] values = Country.values();
        gc.setFill(Color.BLACK);
        gc.setStroke(Color.WHITE);
        if (summary == null && dataframeML != null) {
            summary = dataframeML.summary(valueHeader.get());
        }
        for (int i = 0; i < values.length; i++) {
            Country countries = values[i];
            gc.beginPath();
            if (dataframeML != null) {
                dataframeML.only(header, t -> countries.matches(t), j -> {
                    Number object2 = (Number) dataframeML.list(valueHeader.get()).get(j);
                    countries.setColor(getColorForValue(object2.doubleValue(), summary));
                });
            }
            gc.setFill(countries.getColor() != null ? countries.getColor() : Color.BLACK);
            //            if (gc.getFill().equals(Color.BLACK)) {
            //                System.out.println("COUNTRY NOT FOUND: " + countries.getCountryName());
            //            }
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

    public List<Country> anyAdjacents(Country c) {
        Country[] values = Country.values();
        return Stream.of(values).filter(e -> e.neighbors().contains(c)).flatMap(e -> Stream.of(e, c))
                .filter(e -> e != c).distinct()
                .collect(Collectors.toList());
    }


    public void setDataframe(DataframeML x, String header) {
        this.header = header;
        this.dataframeML = x;
        drawGraph();
    }

}