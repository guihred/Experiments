package ml.graph;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.Predicate;
import javafx.beans.InvalidationListener;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import ml.data.Country;
import ml.data.DataframeML;
import ml.data.DataframeUtils;
import org.slf4j.Logger;
import utils.ex.HasLogging;
import utils.fx.RotateUtils;

public class WorldMapGraph2 extends WorldMapGraph {
    private static final Logger LOG = HasLogging.log();
    private final DoubleProperty radius = new SimpleDoubleProperty(1);
    private final DoubleProperty xScale = new SimpleDoubleProperty(0);
    private final DoubleProperty yScale = new SimpleDoubleProperty(0);
    private String cityHeader = "City";
    private String latHeader;

    private String lonHeader;

    public WorldMapGraph2() {

        addListeners();

    }

    @Override
    public IntegerProperty binsProperty() {
        return bins;
    }

    @Override
    public final void drawGraph() {
        getGc().clearRect(0, 0, getWidth(), getHeight());
        Country[] countries = Country.values();
        getGc().setFill(Color.BLACK);
        getGc().setStroke(Color.BLACK);
        if (dataframeML != null) {
            dataframeML.filterString(header, Country::hasName);
            if (summary == null && dataframeML.getFormat(valueHeader.get()) != String.class) {
                summary = dataframeML.summary(valueHeader.get());
            } else if (dataframeML.getFormat(valueHeader.get()) == String.class) {
                createCategoryMap();
            }
            drawLabels();
        }
        for (int i = 0; i < countries.length; i++) {
            drawCountry(countries[i]);
        }
        if (dataframeML != null && lonHeader != null && latHeader != null) {
            drawPoints();
        }
        if (showNeighborsProperty().get()) {
            drawNeighbors(countries);
        }
    }

    @Override
    public void filter(String h, Predicate<Object> pred) {
        filters.put(h, pred);
    }

    public Map<String, Color> getCategoryMap() {
        if (categoryMap == null) {
            categoryMap = new HashMap<>();
        }

        return categoryMap;
    }

    public GraphicsContext getGc() {
        if (gc == null) {
            gc = getGraphicsContext2D();
        }
        return gc;
    }

    public DoubleProperty radiusProperty() {
        return radius;
    }

    public void setCategoryMap(Map<String, Color> categoryMap) {
        this.categoryMap = categoryMap;
    }

    @Override
    public void setDataframe(DataframeML x, String header) {
        this.header = header;
        dataframeML = x;
        summary = null;
        createCategoryMap();
        drawGraph();
    }

    public void setPoints(String latHeader, String lonHeader) {
        this.latHeader = latHeader;
        this.lonHeader = lonHeader;
        drawGraph();
    }

    @Override
    public StringProperty valueHeaderProperty() {
        return valueHeader;
    }

    public DoubleProperty xScaleProperty() {
        return xScale;
    }

    public DoubleProperty yScaleProperty() {
        return yScale;
    }

    @Override
    protected void drawCountry(Country country) {
        getGc().beginPath();
        Map<Country, Color> colorMap = new EnumMap<>(Country.class);
        if (dataframeML != null) {
            colorMap = getCountriesColor(country);
        }
        getGc().setFill(colorMap.getOrDefault(country, getCategoryMap().get(NO_INFO)));
        getGc().appendSVGPath(country.getPath());
        getGc().fill();
        getGc().stroke();
        getGc().closePath();
    }

    private final void addListeners() {
        InvalidationListener listener = observable -> drawGraph();
        valueHeader.addListener(s -> {
            summary = null;
            getCategoryMap().clear();
            drawGraph();
        });
        radius.addListener(listener);
        xScale.addListener(listener);
        yScale.addListener(listener);
        bins.addListener(listener);
        drawGraph();
        RotateUtils.setZoomable(this);
    }

    private void drawNeighbors(Country[] values) {
        getGc().setStroke(Color.RED);
        getGc().setLineWidth(1);
        for (int i = 0; i < values.length; i++) {
            Country countries = values[i];
            for (Country country : countries.neighbors()) {
                getGc().strokeLine(countries.getCenterX(), countries.getCenterY(), country.getCenterX(),
                    country.getCenterY());
            }
        }
    }

    private void drawPoints() {
        getGc().setFill(Color.RED);
        MercatorMap mercatorMap = new MercatorMap(getWidth(), getHeight());
        List<Double> list = dataframeML.list(lonHeader);
        List<Double> lis2t = dataframeML.list(latHeader);
        List<String> citu = dataframeML.list(cityHeader);
        for (int i = 0; i < dataframeML.getSize(); i++) {
            LOG.trace("X={}", xScale.get());
            LOG.trace("Y={}", yScale.get());
            double latitudeInDegrees = list.get(i).doubleValue();
            double longitudeInDegrees = lis2t.get(i).doubleValue();
            double[] screenLocation = mercatorMap.getScreenLocation(latitudeInDegrees, longitudeInDegrees);
            double r = radius.get();
            double x = xScale.get() + screenLocation[0] * r;
            double y = yScale.get() + screenLocation[1] * r;
            getGc().fillOval(x, y, r, r);
            getGc().fillText(citu.get(i), x, y);
        }
    }

    private Map<Country, Color> getCountriesColor(Country countries) {
        Map<Country, Color> enumMap = new EnumMap<>(Country.class);
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
            if (object instanceof Number) {
                enumMap.put(countries,
                    ColorPattern.getColorForValue(ColorPattern.SATURATION, ((Number) object).doubleValue(), min, max));
            } else if (object instanceof String) {
                enumMap.put(countries, getCategoryMap().get(object));
            }
        });
        return enumMap;
    }


}