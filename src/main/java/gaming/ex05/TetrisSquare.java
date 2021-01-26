package gaming.ex05;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.scene.layout.Region;
import javafx.scene.shape.Rectangle;

public class TetrisSquare extends Region {

	private final ObjectProperty<TetrisPieceState> state = new SimpleObjectProperty<>(TetrisPieceState.EMPTY);

    public TetrisSquare() {

        setPadding(new Insets(10));
        final int prefWidth = 30;
        setPrefSize(prefWidth, prefWidth);
        setShape(new Rectangle(prefWidth, prefWidth));

        styleProperty().bind(
                Bindings.when(state.isEqualTo(TetrisPieceState.EMPTY))
                .then("-fx-background-color:black;")
                .otherwise("-fx-background-color:green; "));
    }

	public TetrisPieceState getState() {
		return state.get();
	}

	public void setState(TetrisPieceState value) {
		state.set(value);
	}

}
