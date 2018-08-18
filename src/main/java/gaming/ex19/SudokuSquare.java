package gaming.ex19;

import java.util.Objects;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

/**
 *
 * @author Note
 */
public final class SudokuSquare extends Region {

	private final IntegerProperty number = new SimpleIntegerProperty(0);
	final int i;
	final int j;


	public SudokuSquare(int i, int j) {
		this.i = i;
		this.j = j;
		setPadding(new Insets(10));
        setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, new Insets(1))));
		StringBuilder style = new StringBuilder();
		style.append("-fx-background-color: white;");
		style.append("-fx-border-color: black;");

		style.append("-fx-border-width: ");
		style.append(j % SudokuModel.MAP_NUMBER == 0 ? " 2" : " 1");
		style.append(i % SudokuModel.MAP_NUMBER == SudokuModel.MAP_NUMBER - 1 ? " 2" : " 1");
		style.append(j % SudokuModel.MAP_NUMBER == SudokuModel.MAP_NUMBER - 1 ? " 2" : " 1");
		style.append(i % SudokuModel.MAP_NUMBER == 0 ? " 2" : " 1");

		style.append(";");
		setStyle(style.toString());
		// setEffect(new InnerShadow());
        Text text = new Text();
        text.textProperty().bind(Bindings.when(number.isNotEqualTo(0)).then(number.asString()).otherwise(""));
        text.wrappingWidthProperty().bind(widthProperty());
        text.setTextOrigin(VPos.CENTER);
        text.layoutYProperty().bind(heightProperty().divide(2));
        text.setTextAlignment(TextAlignment.CENTER);
        getChildren().add(text);
        setPrefSize(50, 50);
    }

    public void setNumber(int value) {
        number.set(value);
    }

	public boolean isInCol(int col) {
		return j == col;
	}

	public boolean isInArea(int row, int col) {
		return i / SudokuModel.MAP_NUMBER == row / SudokuModel.MAP_NUMBER
				&& j / SudokuModel.MAP_NUMBER == col / SudokuModel.MAP_NUMBER;
	}

	public boolean isInRow(int row) {
		return i == row;
	}

    public boolean isEmpty() {
		return number.get() == 0;
    }

    public int getNumber() {
        return number.get();
    }

	@Override
	public boolean equals(Object obj) {
		if (super.equals(obj)) {
			return true;
		}
		return obj instanceof SudokuSquare && ((SudokuSquare) obj).i == i && ((SudokuSquare) obj).j == j;
	}

	@Override
	public int hashCode() {
		return Objects.hash(i, j);
	}
}