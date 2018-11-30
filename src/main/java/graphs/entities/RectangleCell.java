package graphs.entities;

import javafx.beans.binding.Bindings;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class RectangleCell extends Cell {

    private static final double WIDTH = 40;

    public RectangleCell(String id) {
		super(id);
        Rectangle circle = new Rectangle(WIDTH, WIDTH);
		circle.setStroke(Color.BLACK);
		circle.fillProperty().bind(
				Bindings.when(selected).then(Color.RED).otherwise(Bindings.when(color.isNull()).then(Color.DODGERBLUE).otherwise(color)));
		circle.widthProperty().bind(Bindings.createDoubleBinding(
                () -> Math.max(text.getBoundsInParent().getWidth() + WIDTH / 2, WIDTH),
                text.boundsInParentProperty()));
		circle.heightProperty().bind(Bindings.createDoubleBinding(
                () -> Math.max(WIDTH, text.getBoundsInParent().getHeight() + WIDTH / 8),
				text.boundsInParentProperty()));

		setView(new StackPane(circle, text));
	}

	@Override
	public CellType getType() {
        return CellType.RECTANGLE;
	}

}