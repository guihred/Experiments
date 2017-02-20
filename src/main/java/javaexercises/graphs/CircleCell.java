package javaexercises.graphs;

import javafx.beans.binding.Bindings;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class CircleCell extends Cell {

	public CircleCell(String id) {
		super(id);
		Circle circle = new Circle(20);
		circle.setStroke(Color.BLACK);
		circle.fillProperty().bind(
				Bindings.when(selected).then(Color.RED).otherwise(Bindings.when(color.isNull()).then(Color.DODGERBLUE).otherwise(color)));
		setView(new StackPane(circle, text));
	}

	@Override
	public CellType getType() {
		return CellType.CIRCLE;
	}

}