package gaming.ex19;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javafx.beans.NamedArg;
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
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

/**
 *
 * @author Note
 */
public final class SudokuSquare extends Pane {

    public static final int MAP_NUMBER = 3;
    private final IntegerProperty number = new SimpleIntegerProperty(0);
    private final int row;
    private final int col;
    private ObservableList<Integer> possibilities = FXCollections.observableArrayList();
    private boolean permanent = true;
    private BooleanProperty wrong = new SimpleBooleanProperty(false);

    public SudokuSquare() {
        this(-1, -1);
    }

    public SudokuSquare(@NamedArg("row") int i, @NamedArg("col") int j) {
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
        getStyleClass().add("sudokuSquare");

    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            return true;
        }
        if (!(obj instanceof SudokuSquare)) {
            return false;
        }
        SudokuSquare sq = (SudokuSquare) obj;
        return sq.row == row && sq.col == col;
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
        return row / MAP_NUMBER == row1 / MAP_NUMBER && col / MAP_NUMBER == col1 / MAP_NUMBER;
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
        return "(" + row + "," + col + ") " + number.get();
    }

    private String displayPossibilities() {
        return extracted(possibilities).stream().map(Objects::toString).collect(Collectors.joining(" ", " ", " "))
            .replaceAll("(\\d \\d \\d)", "$1\n");
    }

    private void updateStyle() {
        StringBuilder style = new StringBuilder();

        style.append("-fx-background-color:");
        style.append(permanent ? " lightgray;" : " white;");
        style.append("-fx-border-color: black;");

        style.append("-fx-border-width: ");
        style.append(col % MAP_NUMBER == 0 ? " 2" : " 1");
        style.append(row % MAP_NUMBER == MAP_NUMBER - 1 ? " 2" : " 1");
        style.append(col % MAP_NUMBER == MAP_NUMBER - 1 ? " 2" : " 1");
        style.append(row % MAP_NUMBER == 0 ? " 2" : " 1");

        style.append(";");
        setStyle(style.toString());
    }

    private static List<Integer> extracted(List<?> possibilities) {
        if (possibilities.stream().anyMatch(Collection.class::isInstance)) {
            return possibilities.stream().map(Object.class::cast).map(Collection.class::cast)
                .flatMap(Collection<Integer>::stream).collect(Collectors.toList());
        }
        return possibilities.stream().map(Integer.class::cast).collect(Collectors.toList());
    }
}