package ml.graph;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
import utils.RotateUtils;

public class WorldMapGraph2 extends WorldMapGraph {
	private DoubleProperty radius = new SimpleDoubleProperty(1);
	private DoubleProperty xScale = new SimpleDoubleProperty(0);
	private DoubleProperty yScale = new SimpleDoubleProperty(0);
	private String cityHeader = "City";
	private String latHeader;
	private String lonHeader;

	public WorldMapGraph2() {

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

	@Override
	public List<Country> anyAdjacents(Country c) {
		Country[] values = Country.values();
		return Stream.of(values).filter(e -> e.neighbors().contains(c)).flatMap(e -> Stream.of(e, c))
				.filter(e -> e != c).distinct().collect(Collectors.toList());
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
		}

		if (summary == null && dataframeML != null && dataframeML.getFormat(valueHeader.get()) != String.class) {
			summary = dataframeML.summary(valueHeader.get());
		} else if (dataframeML != null && dataframeML.getFormat(valueHeader.get()) == String.class) {
			createCategoryMap();
		}
		if (dataframeML != null) {
			drawLabels();
		}
		for (int i = 0; i < countries.length; i++) {
			drawCountry(countries[i]);
		}
		if (dataframeML != null && lonHeader != null && latHeader != null) {
			drawPoints();
		}
		if (showNeighbors) {
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
		List<Double> list = dataframeML.list(lonHeader, Double.class);
		List<Double> lis2t = dataframeML.list(latHeader, Double.class);
		List<String> citu = dataframeML.list(cityHeader, String.class);
		for (int i = 0; i < dataframeML.getSize(); i++) {
			getLogger().trace("X={}", xScale.get());
			getLogger().trace("Y={}", yScale.get());
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
				enumMap.put(countries, ColorPattern.getColorForValue(ColorPattern.SATURATION,
						((Number) object).doubleValue(), min, max));
			} else if (object instanceof String) {
				enumMap.put(countries, getCategoryMap().get(object));
			}
		});
		return enumMap;
	}
}