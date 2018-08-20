/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gaming.ex19;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.effect.InnerShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

/**
 *
 * @author Note
 */
public class SudokuModel {


    public static final int MAP_NUMBER = 3;

	public static final int MAP_N_SQUARED = MAP_NUMBER * MAP_NUMBER;
	private Random random = new Random();
    private GridPane numberBoard = new GridPane();
    private List<NumberButton> numberOptions = new ArrayList<>();
	private List<SudokuSquare> sudokuSquares = new ArrayList<>();

    private SudokuSquare pressedSquare;

	public SudokuModel() {
        initialize();

    }

    private void initialize() {
        numberBoard.setVisible(false);
		for (int i = 0; i < MAP_N_SQUARED; i++) {
			for (int j = 0; j < MAP_N_SQUARED; j++) {
				sudokuSquares.add(new SudokuSquare(i, j));
			}
        }
        for (int i = 0; i < MAP_NUMBER; i++) {
            for (int j = 0; j < MAP_NUMBER; j++) {
                NumberButton child = new NumberButton(i * MAP_NUMBER + j + 1);
                numberOptions.add(child);
                numberBoard.add(child, j, i);
            }
        }
        NumberButton child = new NumberButton(0);
        numberOptions.add(child);
        numberBoard.add(child, 3, 0);
        reset();
    }


    private void createRandomNumbers() {
        List<Integer> numbers = IntStream.rangeClosed(1, MAP_N_SQUARED).boxed().collect(Collectors.toList());
		int nTries = 0;
		for (int i = 0; i < MAP_N_SQUARED; i++) {
			for (int j = 0; j < MAP_N_SQUARED; j++) {
                int row = i;
                int col = j;
                Collections.shuffle(numbers);
                Optional<Integer> fitNumbers = numbers.stream().filter(n -> isNumberFit(n, row, col)).findFirst();
                getMapAt(i, j).setPermanent(true);
                if (fitNumbers.isPresent()) {
                    getMapAt(i, j).setNumber(fitNumbers.get());
                    continue;
                }
                nTries++;
                j = -1;
                sudokuSquares.stream().filter(e -> e.isInRow(row)).forEach(SudokuSquare::setEmpty);
                if (nTries > 100) {
                    i = -1;
                    nTries = 0;
                    sudokuSquares.forEach(SudokuSquare::setEmpty);
                    break;
                }
			}
		}
    }


	private boolean isNumberFit(int n, int row, int col) {
        return sudokuSquares.stream().filter(e -> !e.isInPosition(row, col)).filter(s -> s.isInRow(row))
                .noneMatch(s -> s.getNumber() == n)
                && sudokuSquares.stream().filter(e -> !e.isInPosition(row, col)).filter(s -> s.isInArea(row, col))
                        .noneMatch(s -> s.getNumber() == n)
                && sudokuSquares.stream().filter(e -> !e.isInPosition(row, col)).filter(s -> s.isInCol(col))
                        .noneMatch(s -> s.getNumber() == n);
	}


	public SudokuSquare getMapAt(int i, int j) {
		return sudokuSquares.get(i * MAP_N_SQUARED + j);
	}


    public Region getNumberBoard() {
        return numberBoard;
    }


    private void removeRandomNumbers() {
        List<SudokuSquare> sudokuSquares2 = sudokuSquares.stream().filter(SudokuSquare::isNotEmpty)
				.collect(Collectors.toList());
		SudokuSquare sudokuSquare = sudokuSquares2.get(random.nextInt(sudokuSquares2.size()));
		int previousN = sudokuSquare.setEmpty();
        List<Integer> possibleNumbers = IntStream.rangeClosed(1, MAP_N_SQUARED)
				.filter(n -> isNumberFit(sudokuSquare, n)).boxed().collect(Collectors.toList());
        if (possibleNumbers.size() == 1) {
            updatePossibilities();
            sudokuSquare.setPermanent(false);
            return;
        }
        sudokuSquare.setNumber(previousN);
    }

    private void updatePossibilities() {
        sudokuSquares.stream().forEach(sq -> sq.setPossibilities(IntStream
                .rangeClosed(1, MAP_N_SQUARED).filter(n -> isNumberFit(sq, n)).boxed().collect(Collectors.toList())));
        sudokuSquares.stream()
                .forEach(sq -> sq.setWrong(!sq.isEmpty() && !sq.getPossibilities().contains(sq.getNumber())));
    }

    public void handleMousePressed(MouseEvent ev) {
        Optional<SudokuSquare> pressed = sudokuSquares.stream()
                .filter(e -> !e.isPermanent())
                .filter(s -> s.getBoundsInParent().contains(ev.getX(), ev.getY())).findFirst();
        if (!pressed.isPresent()) {
            pressedSquare = null;
            return;
        }
        pressedSquare = pressed.get();
        Bounds boundsInParent = pressedSquare.getBoundsInParent();
        int halfTheSize = MAP_N_SQUARED / 2;
        double maxY = pressedSquare.getCol() > halfTheSize ? boundsInParent.getMinY() - 90
                : boundsInParent.getMaxY();
        double maxX = pressedSquare.getRow() > halfTheSize ? boundsInParent.getMinX() - 90
                : boundsInParent.getMaxX();
        numberBoard.setPadding(new Insets(maxY, 0, 0, maxX));
        numberBoard.setVisible(true);
        handleMouseMoved(ev);
    }

    public void handleMouseMoved(MouseEvent s) {
        numberOptions.forEach(e -> e.setOver(e.getBoundsInParent().contains(s.getX(), s.getY())));
    }

    public void handleMouseReleased(MouseEvent s) {
        Optional<NumberButton> findFirst = numberOptions.stream()
                .filter(e -> e.getBoundsInParent().contains(s.getX(), s.getY())).findFirst();
        if (pressedSquare != null && findFirst.isPresent()) {
            NumberButton node = findFirst.get();
            pressedSquare.setNumber(node.getNumber());
            updatePossibilities();
            pressedSquare = null;
        }
        numberBoard.setVisible(false);
        if (sudokuSquares.stream().allMatch(e -> !e.isEmpty() && !e.isWrong())) {
            final Button button = new Button("Reset");
            final Stage stage1 = new Stage();
            button.setOnAction(a -> {
                reset();
                stage1.close();
            });

            final Text text = new Text("You Won");
            final Group group = new Group(text, button);
            group.setLayoutY(50);
            group.setLayoutX(50);
            stage1.setScene(new Scene(group));
            stage1.show();
        }

    }

    private void reset() {
        createRandomNumbers();
        for (int i = 0; i < MAP_N_SQUARED * MAP_N_SQUARED; i++) {
            removeRandomNumbers();
        }
    }


	private boolean isNumberFit(SudokuSquare sudokuSquare, int n) {
		return isNumberFit(n, sudokuSquare.getRow(), sudokuSquare.getCol());
	}

}

final class NumberButton extends Region {

    private final int number;
    private BooleanProperty over = new SimpleBooleanProperty(false);
    public NumberButton(int i) {
        this.number = i;
        styleProperty().bind(Bindings.when(over).then("-fx-background-color: white;")
                .otherwise("-fx-background-color: lightgray;"));
        setEffect(new InnerShadow());
        Text text = new Text(i == 0 ? "X" : Integer.toString(i));
        text.wrappingWidthProperty().bind(widthProperty());
        text.setTextOrigin(VPos.CENTER);
        text.layoutYProperty().bind(heightProperty().divide(2));
        text.setTextAlignment(TextAlignment.CENTER);
        getChildren().add(text);
        setPrefSize(30, 30);

    }

    public void setOver(boolean over) {
        this.over.set(over);
    }
    public int getNumber() {
        return number;
    }
}