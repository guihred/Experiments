package gaming.ex03;

import javafx.beans.NamedArg;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Insets;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

public class SlidingPuzzleSquare extends Region {

    public static final int MAP_SIZE = 4;
    private final IntegerProperty number;
    private StackPane stackPane;
    private Text text = new Text();

    public SlidingPuzzleSquare(@NamedArg("number") int number) {
        this.number = new SimpleIntegerProperty(number);
        setPadding(new Insets(10));
        if (isEmpty()) {
            setBackground(new Background(new BackgroundFill(Color.BLUE, CornerRadii.EMPTY, new Insets(1))));
        } else {
            setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, new Insets(1))));
            text.textProperty().bind(this.number.asString());
        }
        final int prefHeight = 50;
        setPrefSize(prefHeight, prefHeight);
        text.onMouseClickedProperty().bind(onMouseClickedProperty());

    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !getClass().isInstance(obj)) {
            return false;
        }
        return ((SlidingPuzzleSquare) obj).number.get() == number.get();

    }

    public Integer getNumber() {
        return number.get();
    }

    public StackPane getStack() {
        if (stackPane == null) {
            stackPane = new StackPane(this, text);
        }
        return stackPane;
    }

    @Override
    public int hashCode() {
        return number.get();
    }

    public final boolean isEmpty() {
        return number.get() == SlidingPuzzleSquare.MAP_SIZE * SlidingPuzzleSquare.MAP_SIZE;
    }

    public void setNumber(int value) {
        number.set(value);
    }
}
