/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gaming.ex19;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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

	private List<SudokuSquare> sudokuSquares = new ArrayList<>();

	public SudokuModel() {
		for (int i = 0; i < MAP_N_SQUARED; i++) {
			for (int j = 0; j < MAP_N_SQUARED; j++) {
				sudokuSquares.add(new SudokuSquare(i, j));
			}
		}
		List<Integer> numbers = IntStream.rangeClosed(1, MAP_N_SQUARED).boxed().collect(Collectors.toList());
		for (int i = 0; i < MAP_N_SQUARED; i++) {
			for (int j = 0; j < MAP_N_SQUARED; j++) {
				Collections.shuffle(numbers);
				for (Integer n : numbers) {
					int row = i;
					int col = j;
					if (sudokuSquares.stream().filter(s -> s.isInRow(row)).noneMatch(s -> s.getNumber() == n)
							&& sudokuSquares.stream().filter(s -> s.isInArea(row, col))
									.noneMatch(s -> s.getNumber() == n)
							&& sudokuSquares.stream().filter(s -> s.isInCol(col)).noneMatch(s -> s.getNumber() == n)) {
						getMapAt(i, j).setNumber(n);
						break;
					}
				}
				if (getMapAt(i, j).isEmpty()) {
					j = 0;
				}
			}
		}
	}


	public SudokuSquare getMapAt(int i, int j) {
		return sudokuSquares.get(i * MAP_N_SQUARED + j);
	}


	public void handleKeyPressed(KeyEvent e) {
	}

}