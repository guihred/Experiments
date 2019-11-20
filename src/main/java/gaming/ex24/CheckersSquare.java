package gaming.ex24;

import java.util.Arrays;
import javafx.beans.NamedArg;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;

public class CheckersSquare extends StackPane {

    private ObjectProperty<CheckersPlayer> state = new SimpleObjectProperty<>(CheckersPlayer.NONE);
    private boolean black;

    public CheckersSquare(@NamedArg("black") boolean black) {
        this.black = black;

        setBackground(
            new Background(new BackgroundFill(black ? Color.BLACK : Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
        setPrefSize(50, 50);
        for (CheckersPlayer o : Arrays.asList(CheckersPlayer.WHITE, CheckersPlayer.BLACK)) {
            Shape shape = o.getShape();
            shape.visibleProperty().bind(state.isEqualTo(o));
            getChildren().add(shape);
        }

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

    public void setState(CheckersPlayer state) {
        this.state.set(state);
    }
}