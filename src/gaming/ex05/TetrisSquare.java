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
        setPrefSize(30, 30);
        setShape(new Rectangle(30, 30));

        styleProperty().bind(
                Bindings.when(stateProperty().isEqualTo(TetrisPieceState.EMPTY))
                .then("-fx-background-color:black;")
                .otherwise("-fx-background-color:green; "));
    }

	public ObjectProperty<TetrisPieceState> stateProperty() {
		return state;
	}

	public void setState(TetrisPieceState value) {
		state.set(value);
	}

	public TetrisPieceState getState() {
		return state.get();
	}

}
