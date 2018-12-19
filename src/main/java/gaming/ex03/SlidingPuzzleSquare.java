package gaming.ex03;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Insets;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

public class SlidingPuzzleSquare extends Region {

    
	private final IntegerProperty number;
	private StackPane stackPane;
	private Text text = new Text();

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
        return number.get() == SlidingPuzzleModel.MAP_SIZE * SlidingPuzzleModel.MAP_SIZE;
    }
}
