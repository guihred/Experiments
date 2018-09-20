package javaexercises.graphs;

import javafx.beans.binding.Bindings;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;;

public class RectangleCell extends Cell {

	public RectangleCell(String id) {
		super(id);
        Rectangle circle = new Rectangle(40, 40);
		circle.setStroke(Color.BLACK);
		circle.fillProperty().bind(
				Bindings.when(selected).then(Color.RED).otherwise(Bindings.when(color.isNull()).then(Color.DODGERBLUE).otherwise(color)));
        circle.setWidth(Double.max(text.getBoundsInParent().getWidth() + 20, 40));

		setView(new StackPane(circle, text));
	}

	@Override
	public CellType getType() {
        return CellType.RECTANGLE;
	}

}