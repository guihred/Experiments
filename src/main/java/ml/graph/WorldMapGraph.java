package ml.graph;

import static ml.graph.ColorPattern.getColorForValue;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javafx.beans.property.*;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.Scale;
import ml.data.Country;
import ml.data.DataframeML;
import ml.data.DataframeUtils;
import org.apache.commons.lang3.StringUtils;
import utils.ImageFXUtils;
import utils.fx.RotateUtils;

public class WorldMapGraph extends Canvas {
    public static final int HEIGHT = 1200;
    public static final int WIDTH = 2000;
    protected static final String NO_INFO = "No info";
    public static final double BLUE_HUE = Color.BLUE.getHue();
    protected static final double RED_HUE = Color.RED.getHue();
    private final ObjectProperty<ColorPattern> pattern = new SimpleObjectProperty<>(ColorPattern.HUE);
    protected StringProperty valueHeader = new SimpleStringProperty("Value");
    protected IntegerProperty bins = new SimpleIntegerProperty(7);
    protected GraphicsContext gc;
    protected DataframeML dataframeML;
    private final BooleanProperty showNeighbors = new SimpleBooleanProperty(false);
    protected DoubleSummaryStatistics summary;
    protected String header = "Country";
    protected Map<String, Predicate<Object>> filters = new HashMap<>();
    protected Map<String, Color> categoryMap = new HashMap<>();
    protected double max;
    protected double min;
    private Scale scale;
    private final IntegerProperty fontSize = new SimpleIntegerProperty(30);

    private String indicatorName = "Indicator Name";

    public WorldMapGraph() {
        super(WIDTH, HEIGHT);
		linkListeners();

	}

	public IntegerProperty binsProperty() {
        return bins;
    }


	public void drawGraph() {

        gc.clearRect(0, 0, getWidth(), getHeight());
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
            drawTitle();
        }
        gc.setTextAlign(TextAlignment.LEFT);
        Country[] values = Country.values();
        for (int i = 0; i < values.length; i++) {
            drawCountry(values[i]);
        }

        if (showNeighbors.get()) {
            drawLinks(values);
        }
    }

	public void filter(String h, Predicate<Object> pred) {
        filters.put(h, pred);
    }

    public IntegerProperty fontSizeProperty() {
        return fontSize;
    }

    public Scale getScale() {
        return scale;
    }

    public ObjectProperty<ColorPattern> patternProperty() {
        return pattern;
    }

    public void setDataframe(DataframeML x, String header) {
        this.header = header;
        dataframeML = x;
        summary = null;
        drawGraph();
    }

    public BooleanProperty showNeighborsProperty() {
        return showNeighbors;
    }

    public void takeSnapshot() {
        ImageFXUtils.take(this, getScale().getX() * getWidth(), getScale().getY() * getHeight());
    }

    public StringProperty valueHeaderProperty() {
        return valueHeader;
    }

	protected void createCategoryLabels(double x, double y0, double step) {
		double y = y0;
        gc.setFill(Color.GRAY);
        gc.setStroke(Color.BLACK);
        int size = fontSize.get();
        List<Entry<String, Color>> colorsByCategory = categoryMap.entrySet().stream()
            .sorted(Comparator.comparing(Entry<String, Color>::getKey)).collect(Collectors.toList());
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setTextBaseline(VPos.CENTER);

        double a = y + step * colorsByCategory.size() + size / 2.;
        double s = Math.max(0, a - getHeight());
        y -= s;
        for (int i = 0; i < colorsByCategory.size(); i++) {
            Entry<String, Color> entry = colorsByCategory.get(i);
            gc.setFill(entry.getValue());
            gc.fillRect(x, y + step * i, size, size);
            gc.setFill(Color.BLACK);
            gc.fillText(entry.getKey(), x + size + 5, y + step * i + size / 2.);
        }
    }

    protected void createCategoryMap() {

        if (dataframeML == null) {
            categoryMap.put(NO_INFO, Color.GRAY);
            return;
        }
        Set<String> categorize = dataframeML.categorize(valueHeader.get());
        categorize.removeIf(StringUtils::isBlank);

        if (categorize.size() == categoryMap.size() && categoryMap.keySet().equals(categorize)) {
            return;
        }

        List<Color> generateColors = ImageFXUtils.generateRandomColors(categorize.size() + 1);
        int k = 0;
        for (String label : categorize) {
            categoryMap.put(label, generateColors.get(k));
            k++;
        }
        categoryMap.put(NO_INFO, generateColors.get(k));
    }

    protected void createNumberLabels(double x, double y, double step) {
        int millin = 1;
        min = Math.floor(summary.getMin() / millin) * millin;
        max = Math.ceil(summary.getMax() / millin) * millin;
        double h = (max - min) / bins.get();
        int maxLength = 0;
        for (int i = 0; i <= bins.get(); i++) {
            double s = Math.floor((min + i * h) / millin) * millin;
            if (h < 2) {
                maxLength = Math.max(maxLength, String.format("%.2f", s).length());
            } else {
                maxLength = Math.max(maxLength, String.format("%.0f", s).length());
            }
        }
        double w = step * (maxLength + 2);
        gc.fillRect(x - 5, y - step / 3, w, step * (bins.get() + 3. / 2));
        gc.setFill(Color.GRAY);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.setStroke(Color.BLACK);
        for (int i = 0; i <= bins.get(); i++) {
            double s = Math.floor((min + i * h) / millin) * millin;
            int size = fontSize.get();
            if (h < 2) {
                s = min + i * h;
                gc.setFill(Color.BLACK);
                gc.fillText(String.format("%11.2f", s), x + w / 2, y + step * i + size / 2.);
                gc.setFill(pattern.get().getColorForValue(s, min, max));
                gc.fillRect(x, y + step * i, size, size);
            } else {
                gc.setFill(pattern.get().getColorForValue(s, min, max));
                gc.fillRect(x, y + step * i, size, size);
                gc.setFill(Color.BLACK);
                gc.fillText(String.format("%11.0f", s), x + w / 2, y + step * i + size / 2.);
            }
        }

        categoryMap.put(NO_INFO, Color.GRAY);
    }

    protected void drawCountry(Country countries) {
        gc.beginPath();
        Map<Country, Color> countriesColors = new EnumMap<>(Country.class);
        if (dataframeML != null) {
            dataframeML.only(header, countries::matches, j -> {
                Set<Entry<String, Predicate<Object>>> entrySet = filters.entrySet();
                for (Entry<String, Predicate<Object>> fil : entrySet) {
                    Object t = dataframeML.list(fil.getKey()).get(j);
                    if (!fil.getValue().test(t)) {
                        return;
                    }
                }
                List<Object> list = dataframeML.list(valueHeader.get());
                Object object = DataframeUtils.getFromList(j, list);
                countriesColors.put(countries, getColor(object));
            });
        }
        gc.setFill(countriesColors.getOrDefault(countries, categoryMap.get(NO_INFO)));
        String path = countries.getPath();
        gc.appendSVGPath(path);
        gc.fill();
        gc.stroke();
        gc.closePath();
    }

    protected void drawLabels() {
        gc.setFill(Color.GRAY);
        double x = 50;
		double y = getHeight() / 2;
		double step = fontSize.get() + 2.;
        if (summary != null) {
            createNumberLabels(x, y, step);
        } else {
            createCategoryLabels(x, y, step);
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

    private void drawTitle() {
        String title = getTitle();
        gc.setFill(Color.BLACK);
        gc.setFont(Font.font(fontSizeProperty().get()));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(title, gc.getCanvas().getWidth() / 2, fontSizeProperty().get());
    }

    private Color getColor(Object object) {
        if (object instanceof Number) {
            return getColorForValue(pattern.get(), ((Number) object).doubleValue(), min, max);
        } else if (object instanceof String) {
            return categoryMap.get(object);
        }
        return null;
    }

    private String getTitle() {
        if (isSuitableForSummary()) {
            List<Object> list = dataframeML.list(indicatorName);
            return list.get(0).toString();
        }
        return Optional.ofNullable(dataframeML.list(indicatorName)).map(e -> e.get(0)).orElse("") + " "
            + valueHeader.get();
    }

    private boolean isSuitableForCategory() {
        return dataframeML != null && dataframeML.getFormat(valueHeader.get()) == String.class;
    }

    private boolean isSuitableForSummary() {
        return summary == null && dataframeML != null && dataframeML.getFormat(valueHeader.get()) != String.class;
    }

    private final void linkListeners() {
		gc = getGraphicsContext2D();
        valueHeader.addListener(s -> {
            summary = null;
            categoryMap.clear();
            drawGraph();
        });
        bins.addListener(ob -> drawGraph());
        showNeighbors.addListener(ob -> drawGraph());
        fontSize.addListener(ob -> drawGraph());
        pattern.addListener(ob -> drawGraph());
        drawGraph();
        scale = RotateUtils.setZoomable(this);
        scale.setX(1. / 2);
        scale.setY(1. / 2);
	}
}