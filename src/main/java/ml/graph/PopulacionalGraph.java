package ml.graph;

import java.util.*;
import java.util.stream.Collectors;
import javafx.beans.InvalidationListener;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import ml.data.DataframeML;

public class PopulacionalGraph extends Canvas {
    private static final double BORDER_LEFT = 0.45;
    private static final double BORDER_RIGHT = 0.55;
    public static final int CANVAS_SIZE = 550;
    private final DoubleProperty layout = new SimpleDoubleProperty(30);
    private final DoubleProperty maxLayout = new SimpleDoubleProperty(CANVAS_SIZE);
    private final DoubleProperty lineSize = new SimpleDoubleProperty(1);
    private final IntegerProperty bins = new SimpleIntegerProperty(5);
    private String ageHeader = "Subject";
    private String sexHeader = "SEX";
    private String yearHeader = "TIME";
    private String valueHeader = "Value";
    private String countryHeader = "Country";
    private final StringProperty country = new SimpleStringProperty("Germany");
    private final IntegerProperty year = new SimpleIntegerProperty(2000);
    private double xProportion;
    private GraphicsContext gc;
    private DataframeML dataframe;
    private List<String> agesSteps = Collections.emptyList();
    private final ObservableList<Integer> yearsOptions = FXCollections.observableArrayList();

    public PopulacionalGraph() {
        super(CANVAS_SIZE, CANVAS_SIZE);
        prefWidth(CANVAS_SIZE);
        prefHeight(CANVAS_SIZE);
        gc = getGraphicsContext2D();
        drawGraph();
        lineSize.set(getHeight() / getWidth());
        InvalidationListener listener = observable -> drawGraph();
        maxLayout.addListener(listener);
        lineSize.addListener(listener);
        year.addListener(listener);
        bins.addListener(listener);
        layout.addListener(listener);
        country.addListener(listener);
    }

    public IntegerProperty binsProperty() {
        return bins;
    }

    public StringProperty countryProperty() {
        return country;
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
        double xMA = prop(layout1, maxLayout1, BORDER_LEFT);
        double xFE = prop(layout1, maxLayout1, BORDER_RIGHT);
        gc.strokeLine(xMA, layout1, xMA, maxLayout1);
        gc.strokeLine(xFE, layout1, xFE, maxLayout1);
        prop(layout1, maxLayout1, 0.5);
        gc.setTextAlign(TextAlignment.CENTER);
        double j = (xMA - layout1) / xbins;
        for (int i = 0; i <= xbins; i++) {
            double x1 = -i * j + xMA;
            gc.strokeLine(x1, maxLayout1, x1, maxLayout1 + lineSize1);
            String xLabel = String.format("%.0f", i * xProportion);
            gc.strokeText(xLabel, x1, maxLayout1 + lineSize1 * (4 + 3 * (i % 2)));

        }
        j = (maxLayout1 - xFE) / xbins;
        for (int i = 0; i <= xbins; i++) {
            double x1 = i * j + xFE;
            gc.strokeLine(x1, maxLayout1, x1, maxLayout1 + lineSize1);
            String xLabel = String.format("%.0f", i * xProportion);
            gc.strokeText(xLabel, x1, maxLayout1 + lineSize1 * (4 + 3 * (i % 2)));

        }
        double h = (maxLayout1 - layout1) / agesSteps.size() - 2;
        j = (maxLayout1 - layout1) / agesSteps.size();
        for (int i = 0; i < agesSteps.size(); i++) {
            double y1 = maxLayout1 - i * j;
            gc.strokeLine(xFE, y1, xFE - lineSize1, y1);
            gc.strokeLine(xMA, y1, xMA + lineSize1, y1);
            String yLabel = agesSteps.get(i);
            gc.strokeText(yLabel, xMid, y1 - h / 2);
        }
        gc.scale(1, 1 / lineSize.doubleValue());
    }

    public final void drawGraph() {
        gc.clearRect(0, 0, getWidth(), getHeight());
        if (dataframe == null) {
            drawAxis();
            return;

        }
        gc.setFill(Color.BLUE);
        gc.scale(1, lineSize.doubleValue());
        DoubleSummaryStatistics peopleStats = new DoubleSummaryStatistics();
        List<Number> values = dataframe.list(valueHeader, Number.class);
        List<String> sexes = dataframe.list(sexHeader, String.class);
        List<String> ages = dataframe.list(ageHeader, String.class);
        List<Integer> years = dataframe.list(yearHeader, Integer.class);
        Map<String, Number> possibleAgesMA = new HashMap<>();
        Map<String, Number> possibleAgesFE = new HashMap<>();

        dataframe.only(countryHeader, t -> t.equals(country.get()), j -> {
            if (!yearsOptions.contains(years.get(j))) {
                yearsOptions.add(years.get(j));
                yearsOptions.sorted();
            }
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
        double xMA = prop(layout1, maxLayout1, BORDER_LEFT);
        double xFE = prop(layout1, maxLayout1, BORDER_RIGHT);
        agesSteps = possibleAgesFE.keySet().stream().distinct().sorted().collect(Collectors.toList());
        double j = (maxLayout1 - layout1) / agesSteps.size();
        double max = peopleStats.getMax();
        xProportion = max / bins.doubleValue();
        double h = (maxLayout1 - layout1) / agesSteps.size() - 2;

        for (int i = 0; i < agesSteps.size(); i++) {
            double y1 = maxLayout1 - (i + 1) * j;
            String strip = agesSteps.get(i);
            drawRectangle(possibleAgesFE.getOrDefault(strip, 0), Color.RED, maxLayout1, xFE, y1, h, max);
            drawRectangle(possibleAgesMA.getOrDefault(strip, 0), Color.BLUE, xMA, layout1, y1, h, max);
        }
        gc.scale(1, 1 / lineSize.doubleValue());
        drawAxis();

    }

    public DoubleProperty layoutProperty() {
        return layout;
    }

    public DoubleProperty lineSizeProperty() {
        return lineSize;
    }

    public DoubleProperty maxLayoutProperty() {
        return maxLayout;
    }

    public void setHistogram(DataframeML dataframe) {
        this.dataframe = dataframe;
        drawGraph();
    }

    public IntegerProperty yearProperty() {
        return year;
    }

    public ObservableList<Integer> yearsOptionsProperty() {
        return yearsOptions;
    }

    private void drawRectangle(Number value, Color color, double maxLayout1, double xFE, double y1, double h,
        double max) {
        double w = (maxLayout1 - xFE) * value.doubleValue() / max;
        gc.setFill(color);
        if (color == Color.RED) {
            gc.fillRect(xFE, y1, w, h);
        } else {
            gc.fillRect(maxLayout1 - w, y1, w, h);
        }
    }

    private static double prop(double layout1, double maxLayout1, double d) {
        return layout1 + (maxLayout1 - layout1) * d;
    }

}