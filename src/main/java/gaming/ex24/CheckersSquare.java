package gaming.ex24;

import java.util.Arrays;
import javafx.beans.NamedArg;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.scene.effect.Effect;
import javafx.scene.effect.InnerShadow;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;

public class CheckersSquare extends StackPane {

    private ObjectProperty<CheckersPlayer> state = new SimpleObjectProperty<>(CheckersPlayer.NONE);
    private boolean black;
	private BooleanProperty highlight = new SimpleBooleanProperty(false);
	private BooleanProperty selected = new SimpleBooleanProperty(false);

    public CheckersSquare(@NamedArg("black") boolean black) {
        this.black = black;

        setBackground(
            new Background(new BackgroundFill(black ? Color.BLACK : Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
        setPrefSize(50, 50);
        for (CheckersPlayer o : Arrays.asList(CheckersPlayer.WHITE, CheckersPlayer.BLACK)) {
            Shape shape = o.getShape();
            shape.visibleProperty().bind(state.isEqualTo(o));
            getChildren().add(shape);
			Effect glow = new InnerShadow(20, Color.YELLOW);
			shape.effectProperty().bind(Bindings.when(selected).then(glow).otherwise((Effect) null));
        }
		Effect glow = new InnerShadow(20, Color.BLUE);
		effectProperty().bind(Bindings.when(highlight).then(glow).otherwise((Effect) null));
    }

    public boolean getHighlight() {
		return highlight.get();
	}

	public boolean getSelected() {
		return selected.get();
	}

    public CheckersPlayer getState() {
        return state.get();
    }

    public boolean isBlack() {
        return black;
    }

	public void setBlack(boolean black) {
        this.black = black;
    }

	public void setHighlight(boolean highlight) {
		this.highlight.set(highlight);
	}

	public void setSelected(boolean selected) {
		this.selected.set(selected);
	}

	public void setState(CheckersPlayer state) {
        this.state.set(state);
    }
}