package graphs.entities;

import static javafx.beans.binding.Bindings.when;

import javafx.beans.NamedArg;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class CircleCell extends Cell {

    public CircleCell(@NamedArg("cellId") String cellId) {
        super(cellId);
        Circle circle = new Circle(20);
        circle.setStroke(Color.BLACK);
        circle.fillProperty().bind(
            when(selected).then(Color.RED).otherwise(when(color.isNull()).then(Color.DODGERBLUE).otherwise(color)));
        setView(new StackPane(circle, text));
    }

    @Override
    public CellType getType() {
        return CellType.CIRCLE;
    }

}