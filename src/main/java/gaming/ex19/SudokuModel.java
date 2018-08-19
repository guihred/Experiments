/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gaming.ex19;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javafx.scene.input.KeyEvent;

/**
 *
 * @author Note
 */
public class SudokuModel {
	public static final int MAP_NUMBER = 3;

	public static final int MAP_N_SQUARED = MAP_NUMBER * MAP_NUMBER;
	private Random random = new Random();

	private List<SudokuSquare> sudokuSquares = new ArrayList<>();

	public SudokuModel() {
		for (int i = 0; i < MAP_N_SQUARED; i++) {
			for (int j = 0; j < MAP_N_SQUARED; j++) {
				sudokuSquares.add(new SudokuSquare(i, j));
			}
		}
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
					System.out.println("Trying again");
				}
			}
		}
		for (int i = 0; i < MAP_N_SQUARED * MAP_N_SQUARED; i++) {
			// handleKeyPressed(null);
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


	@SuppressWarnings("unused")
	public void handleKeyPressed(KeyEvent event) {
		List<SudokuSquare> sudokuSquares2 = sudokuSquares.stream().filter(e -> !e.isEmpty())
				.collect(Collectors.toList());
		SudokuSquare sudokuSquare = sudokuSquares2.get(random.nextInt(sudokuSquares2.size()));
		int previousN = sudokuSquare.setEmpty();
		List<Integer> collect = IntStream.rangeClosed(1, MAP_N_SQUARED)
				.filter(n -> isNumberFit(sudokuSquare, n)).boxed().collect(Collectors.toList());
		if (collect.size() > 1) {
			sudokuSquare.setNumber(previousN);
			return;
		}
		sudokuSquares.stream().filter(SudokuSquare::isEmpty)
		.forEach(sq -> sq.setPossibilities(
				IntStream.rangeClosed(1, MAP_N_SQUARED).filter(n -> isNumberFit(sq, n)).boxed().collect(Collectors.toList())
				));

	}


	private boolean isNumberFit(SudokuSquare sudokuSquare, int n) {
		return isNumberFit(n, sudokuSquare.getRow(), sudokuSquare.getCol());
	}

}