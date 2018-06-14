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
	DoubleProperty layout = new SimpleDoubleProperty(30);
	double maxLayout = 480;
	DoubleProperty lineSize = new SimpleDoubleProperty(5);
	IntegerProperty bins = new SimpleIntegerProperty(20);
	IntegerProperty ybins = new SimpleIntegerProperty(20);
	double xProportion;
	double yProportion;
	GraphicsContext gc;
	ObservableMap<String, DoubleSummaryStatistics> stats = FXCollections.observableHashMap();
    ObservableMap<String, Color> colors = FXCollections.observableHashMap();
	IntegerProperty radius = new SimpleIntegerProperty(5);


	public WorldMapGraph() {
		super(2000, 1200);
		gc = getGraphicsContext2D();
		InvalidationListener listener = observable -> drawGraph();
		stats.addListener(listener);
		lineSize.addListener(listener);
		colors.addListener(listener);
		lineSize.addListener(listener);
		layout.addListener(listener);
		bins.addListener(listener);
		ybins.addListener(listener);
		drawGraph();
	}

	public void drawGraph() {
		gc.clearRect(0, 0, getWidth(), getHeight());
		Countries[] values = Countries.values();
		gc.setFill(Color.BLACK);
		gc.setStroke(Color.WHITE);
		for (int i = 0; i < values.length; i++) {
			Countries countries = values[i];
			gc.beginPath();
			gc.appendSVGPath(countries.getPath());
			gc.fill();
			gc.stroke();
			gc.closePath();

		}
	}

}