package graphs.entities;

import static javafx.beans.binding.Bindings.createDoubleBinding;
import static javafx.beans.binding.Bindings.when;

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
            when(selected).then(Color.RED).otherwise(when(color.isNull()).then(Color.DODGERBLUE).otherwise(color)));
        circle.widthProperty().bind(createDoubleBinding(
            () -> Math.max(text.getBoundsInParent().getWidth() + WIDTH / 2, WIDTH), text.boundsInParentProperty()));
        circle.heightProperty().bind(createDoubleBinding(
            () -> Math.max(WIDTH, text.getBoundsInParent().getHeight() + WIDTH / 8), text.boundsInParentProperty()));

        setView(new StackPane(circle, text));
    }

    @Override
    public CellType getType() {
        return CellType.RECTANGLE;
    }

}