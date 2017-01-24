package gaming.ex03;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Insets;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

public class SlidingPuzzleSquare extends Region {

    
    final IntegerProperty number;
    Text text = new Text();

    public SlidingPuzzleSquare(int number) {
        this.number = new SimpleIntegerProperty(number);
        setPadding(new Insets(10));
        if (isEmpty()) {
            setBackground(new Background(new BackgroundFill(Color.BLUE, CornerRadii.EMPTY, new Insets(1))));
        } else {
            setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, new Insets(1))));
            text.textProperty().bind(this.number.asString());
        }
        setPrefSize(50, 50);

    }

    public final boolean isEmpty() {
        return number.get() == SlidingPuzzleModel.MAP_SIZE * SlidingPuzzleModel.MAP_SIZE;
    }

    StackPane stackPane;

    StackPane getStack() {
        if (stackPane == null) {
			stackPane = new StackPane(this, text);
		}
        return stackPane;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj.getClass() != SlidingPuzzleSquare.class) {
            return false;
        }
        if (((SlidingPuzzleSquare) obj).number.get() == number.get()) {
            return true;
        }
        return false;

    }

    @Override
    public int hashCode() {
        return number.get();
    }
}
