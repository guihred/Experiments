package fxpro.ch06;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

class ResponsiveUIModel {
	private final ObjectProperty<Paint> fillPaint = new SimpleObjectProperty<>(Color.LIGHTGRAY);
	private final ObjectProperty<Paint> strokePaint = new SimpleObjectProperty<>(Color.DARKGRAY);

	public ObjectProperty<Paint> getFillPaint() {
		return fillPaint;
	}
	public ObjectProperty<Paint> getStrokePaint() {
		return strokePaint;
	}
}