package gaming.ex05;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.scene.layout.Region;
import javafx.scene.shape.Rectangle;

public class TetrisSquare extends Region {

    ObjectProperty<State> state = new SimpleObjectProperty<>(State.EMPTY);

    public TetrisSquare() {

        setPadding(new Insets(10));
        setPrefSize(30, 30);
        setShape(new Rectangle(30, 30));

        styleProperty().bind(
                Bindings.when(state.isEqualTo(State.EMPTY))
                .then("-fx-background-color:black;")
                .otherwise("-fx-background-color:green; "));
    }

}

enum State {

    EMPTY,
    TRANSITION,
    SETTLED;
}
