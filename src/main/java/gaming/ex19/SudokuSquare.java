package gaming.ex19;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

/**
 *
 * @author Note
 */
public final class SudokuSquare extends Region {

	private final IntegerProperty number = new SimpleIntegerProperty(0);
	private final int row;
	private final int col;
	private ObservableList<Integer> possibilities = FXCollections.observableArrayList();


	public SudokuSquare(int i, int j) {
		row = i;
		col = j;
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
		Font default1 = Font.getDefault();

		text.setFont(Font.font(default1.getFamily(), FontWeight.BOLD, default1.getSize()));
        text.layoutYProperty().bind(heightProperty().divide(2));
        text.setTextAlignment(TextAlignment.CENTER);
        getChildren().add(text);
        setPrefSize(50, 50);

		Text possibilitiesText = new Text();
		possibilitiesText.setTextAlignment(TextAlignment.CENTER);
		possibilitiesText.setFont(Font.font(default1.getFamily(), FontWeight.THIN, default1.getSize() / 2));
		possibilitiesText.setTextOrigin(VPos.TOP);
		possibilitiesText.visibleProperty().bind(Bindings.createBooleanBinding(this::isEmpty, number));
		possibilitiesText.textProperty().bind(Bindings.createStringBinding(
				() -> possibilities.stream().map(Objects::toString).collect(Collectors.joining(" ", " ", " ")),
				possibilities));
		getChildren().add(possibilitiesText);

    }

	public void setPossibilities(List<Integer> possibilities) {
		this.possibilities.setAll(possibilities);
	}

    public void setNumber(int value) {
        number.set(value);
    }

	public int setEmpty() {
		int k = number.get();
		number.set(0);
		return k;
	}

	public boolean isInCol(int col1) {
		return col == col1;
	}

	public boolean isInArea(int row1, int col1) {
		return row / SudokuModel.MAP_NUMBER == row1 / SudokuModel.MAP_NUMBER
				&& col / SudokuModel.MAP_NUMBER == col1 / SudokuModel.MAP_NUMBER;
	}

	public boolean isInRow(int row1) {
		return row == row1;
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
		return obj instanceof SudokuSquare && ((SudokuSquare) obj).getRow() == getRow() && ((SudokuSquare) obj).getCol() == getCol();
	}

	@Override
	public int hashCode() {
		return Objects.hash(getRow(), getCol());
	}

	public int getRow() {
		return row;
	}

	public int getCol() {
		return col;
	}
}