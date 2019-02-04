package gaming.ex19;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
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
    private boolean permanent = true;
    private BooleanProperty wrong = new SimpleBooleanProperty(false);

	public SudokuSquare(int i, int j) {
		row = i;
		col = j;
		setPadding(new Insets(10));
        setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, new Insets(1))));
        updateStyle();
        Text text = new Text();
        text.textProperty().bind(Bindings.when(number.isNotEqualTo(0)).then(number.asString()).otherwise(""));
        text.wrappingWidthProperty().bind(widthProperty());
        text.setTextOrigin(VPos.CENTER);
		Font default1 = Font.getDefault();

        text.setFont(Font.font(default1.getFamily(), FontWeight.BOLD, default1.getSize() * 2));
        text.layoutYProperty().bind(heightProperty().divide(2));
        text.setTextAlignment(TextAlignment.CENTER);
        getChildren().add(text);
        setPrefSize(50, 50);
        text.fillProperty().bind(Bindings.when(wrong).then(Color.RED).otherwise(Color.BLACK));
		Text possibilitiesText = new Text();
		possibilitiesText.setTextAlignment(TextAlignment.CENTER);
        possibilitiesText.setFont(Font.font(default1.getFamily(), FontWeight.THIN, default1.getSize() * 3 / 4));
		possibilitiesText.setTextOrigin(VPos.TOP);
		possibilitiesText.visibleProperty().bind(Bindings.createBooleanBinding(this::isEmpty, number));
        possibilitiesText.textProperty().bind(Bindings.createStringBinding(this::displayPossibilities, possibilities));
        possibilitiesText.wrappingWidthProperty().bind(widthProperty());
        getChildren().add(possibilitiesText);

    }

    @Override
	public boolean equals(Object obj) {
		if (super.equals(obj)) {
			return true;
		}
		return obj instanceof SudokuSquare && ((SudokuSquare) obj).getRow() == getRow() && ((SudokuSquare) obj).getCol() == getCol();
	}

    public int getCol() {
		return col;
	}

    public int getNumber() {
        return number.get();
    }

    public List<Integer> getPossibilities() {
        return possibilities;
    }

    public int getRow() {
		return row;
	}

    @Override
	public int hashCode() {
		return Objects.hash(getRow(), getCol());
	}

    public boolean isEmpty() {
		return number.get() == 0;
    }
    public boolean isInArea(int row1, int col1) {
		return row / SudokuModel.MAP_NUMBER == row1 / SudokuModel.MAP_NUMBER
				&& col / SudokuModel.MAP_NUMBER == col1 / SudokuModel.MAP_NUMBER;
	}


	public boolean isInCol(int col1) {
		return col == col1;
	}

	public boolean isInPosition(int row1, int col1) {
        return row == row1 && col1 == col;
    }

	public boolean isInRow(int row1) {
		return row == row1;
	}

	public boolean isNotEmpty() {
        return !isEmpty();
    }

    public boolean isPermanent() {
        return permanent;
    }

    public boolean isWrong() {
        return wrong.get();
    }

    public int setEmpty() {
		int k = number.get();
		number.set(0);
		return k;
	}

    public void setNumber(int value) {
        number.set(value);
    }

	public void setPermanent(boolean permanent) {
        this.permanent = permanent;
        updateStyle();
    }

	public void setPossibilities(List<Integer> possibilities) {
		this.possibilities.setAll(possibilities);
	}

	public void setWrong(boolean wrong) {
        this.wrong.set(wrong);
    }

	@Override
    public String toString() {
        return "("+row
                + ","+col
                + ") "+number.get();
    }

    private String displayPossibilities() {
        return possibilities.stream().map(Objects::toString).collect(Collectors.joining(" ", " ", " "))
                .replaceAll("(\\d \\d \\d)", "$1\n")
                ;
    }

    private void updateStyle() {
        StringBuilder style = new StringBuilder();

        if (permanent) {
            style.append("-fx-background-color: lightgray;");
        } else {
            style.append("-fx-background-color: white;");
        }

        style.append("-fx-border-color: black;");

        style.append("-fx-border-width: ");
        style.append(col % SudokuModel.MAP_NUMBER == 0 ? " 2" : " 1");
        style.append(row % SudokuModel.MAP_NUMBER == SudokuModel.MAP_NUMBER - 1 ? " 2" : " 1");
        style.append(col % SudokuModel.MAP_NUMBER == SudokuModel.MAP_NUMBER - 1 ? " 2" : " 1");
        style.append(row % SudokuModel.MAP_NUMBER == 0 ? " 2" : " 1");

        style.append(";");
        setStyle(style.toString());
    }
}