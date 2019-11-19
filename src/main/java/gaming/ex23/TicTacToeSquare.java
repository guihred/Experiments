package gaming.ex23;

import java.util.Arrays;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.scene.effect.InnerShadow;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;

public class TicTacToeSquare extends StackPane {

    private ObjectProperty<TicTacToePlayer> state = new SimpleObjectProperty<>(TicTacToePlayer.NONE);

    public TicTacToeSquare() {
        setPadding(new Insets(10));
        setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, new Insets(1))));
        setEffect(new InnerShadow());
        setPrefSize(50, 50);
        for (TicTacToePlayer o : Arrays.asList(TicTacToePlayer.O, TicTacToePlayer.X)) {
            Shape shape = o.getShape();
            shape.visibleProperty().bind(state.isEqualTo(o));
            getChildren().add(shape);
        }

    }

    public TicTacToePlayer getState() {
        return state.get();
    }

    public void setState(TicTacToePlayer state) {
        this.state.set(state);
    }
}