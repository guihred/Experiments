package graphs.entities;

import javafx.beans.binding.Bindings;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;

public class TriangleCell extends Cell {

	public TriangleCell(String id) {
		super(id);

		double width = 50;
		double height = 50;

		Polygon view1 = new Polygon(width / 2, 0, width, height, 0, height);
        view1.setStroke(Color.BLACK);
		view1.fillProperty().bind(
				Bindings.when(selected).then(Color.RED).otherwise(Bindings.when(color.isNull()).then(Color.DODGERBLUE).otherwise(color)));

		setView(new StackPane(view1, text));

	}

	@Override
	public CellType getType() {
		return CellType.TRIANGLE;
	}

}