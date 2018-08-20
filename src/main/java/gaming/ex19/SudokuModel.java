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
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

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
        numberBoard.setGridLinesVisible(true);
		List<Integer> numbers = IntStream.rangeClosed(1, MAP_N_SQUARED).boxed().collect(Collectors.toList());
		int nTries = 0;
		for (int i = 0; i < MAP_N_SQUARED; i++) {
			for (int j = 0; j < MAP_N_SQUARED; j++) {
				Collections.shuffle(numbers);
				for (Integer n : numbers) {
					if (isNumberFit(n, i, j)) {
						getMapAt(i, j).setNumber(n);
						break;
					}
				}
				if (getMapAt(i, j).isEmpty()) {
					nTries++;
					j = -1;
					int row1=i;
					sudokuSquares.stream().filter(e -> e.isInRow(row1)).forEach(SudokuSquare::setEmpty);
					if (nTries > 100) {
						i = -1;
						nTries = 0;
						sudokuSquares.forEach(SudokuSquare::setEmpty);
						break;
					}
				}
			}
		}
		for (int i = 0; i < MAP_N_SQUARED * MAP_N_SQUARED; i++) {
            handleKeyPressed(null);
		}

	}


	private boolean isNumberFit(int n, int row, int col) {
		return sudokuSquares.stream().filter(s -> s.isInRow(row)).noneMatch(s -> s.getNumber() == n)
				&& sudokuSquares.stream().filter(s -> s.isInArea(row, col)).noneMatch(s -> s.getNumber() == n)
				&& sudokuSquares.stream().filter(s -> s.isInCol(col)).noneMatch(s -> s.getNumber() == n);
	}


	public SudokuSquare getMapAt(int i, int j) {
		return sudokuSquares.get(i * MAP_N_SQUARED + j);
	}


    public Region getNumberBoard() {
        return numberBoard;
    }


	@SuppressWarnings("unused")
	public void handleKeyPressed(KeyEvent event) {
        List<SudokuSquare> sudokuSquares2 = sudokuSquares.stream().filter(SudokuSquare::isNotEmpty)
				.collect(Collectors.toList());
		SudokuSquare sudokuSquare = sudokuSquares2.get(random.nextInt(sudokuSquares2.size()));
		int previousN = sudokuSquare.setEmpty();
		List<Integer> collect = IntStream.rangeClosed(1, MAP_N_SQUARED)
				.filter(n -> isNumberFit(sudokuSquare, n)).boxed().collect(Collectors.toList());
		if (collect.size() > 1) {
			sudokuSquare.setNumber(previousN);
			return;
        }
        setPossibilities();

    }

    private void setPossibilities() {
        sudokuSquares.stream().filter(SudokuSquare::isEmpty).forEach(sq -> sq.setPossibilities(IntStream
                .rangeClosed(1, MAP_N_SQUARED).filter(n -> isNumberFit(sq, n)).boxed().collect(Collectors.toList())));
    }

    public void handleMousePressed(MouseEvent e) {
        Optional<SudokuSquare> pressed = sudokuSquares.stream()
                .filter(s -> s.getBoundsInParent().contains(e.getX(), e.getY())).findFirst();
        if (!pressed.isPresent()) {
            pressedSquare = null;
            return;
        }
        pressedSquare = pressed.get();
        Bounds boundsInParent = pressedSquare.getBoundsInParent();
        double maxY = pressedSquare.getCol() > MAP_N_SQUARED / 2 ? boundsInParent.getMinY() - 90
                : boundsInParent.getMaxY();
        double maxX = pressedSquare.getRow() > MAP_N_SQUARED / 2 ? boundsInParent.getMinX() - 90
                : boundsInParent.getMaxX();
        numberBoard.setPadding(new Insets(maxY, 0, 0, maxX));
        numberBoard.setVisible(true);
    }

    public void handleMouseMoved(MouseEvent s) {

    }

    public void handleMouseReleased(MouseEvent s) {
        Optional<NumberButton> findFirst = numberOptions.stream()
                .filter(e -> e.getBoundsInParent().contains(s.getX(), s.getY())).findFirst();
        if(pressedSquare!=null&&findFirst.isPresent()) {
            NumberButton node = findFirst.get();

            int number = node.getNumber();
            pressedSquare.setNumber(number);
            setPossibilities();
        }
        
        
        numberBoard.setVisible(false);

    }


	private boolean isNumberFit(SudokuSquare sudokuSquare, int n) {
		return isNumberFit(n, sudokuSquare.getRow(), sudokuSquare.getCol());
	}

}

final class NumberButton extends Region {

    private final int number;

    public NumberButton(int i) {
        this.number = i;

        setOnMouseDragOver(e -> setHover(true));
        setOnMouseDragExited(e -> setHover(false));

        styleProperty().bind(Bindings.when(this.hoverProperty()).then("-fx-background-color: white;")
                .otherwise("-fx-background-color: lightgray;"));

        Text text = new Text(Integer.toString(i));
        text.wrappingWidthProperty().bind(widthProperty());
        text.setTextOrigin(VPos.CENTER);
        text.layoutYProperty().bind(heightProperty().divide(2));
        text.setTextAlignment(TextAlignment.CENTER);
        getChildren().add(text);
        setPrefSize(30, 30);

    }

    public int getNumber() {
        return number;
    }
}