/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gaming.ex06;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.scene.shape.Circle;

/**
 *
 * @author Note
 */
public class QuartoModel {

	private final Circle[][] map = new Circle[4][4];
	private final QuartoPiece[][] mapQuarto = new QuartoPiece[4][4];
	private final List<QuartoPiece> pieces = new ArrayList<>();

	public boolean checkEnd() {
		if (checkColumns()) {
			return true;
		}
		if (checkRows()) {
			return true;
		}
		return checkSquares();
	}

	public Circle[][] getMap() {
		return map;
	}

	public QuartoPiece[][] getMapQuarto() {
		return mapQuarto;
	}

	public List<QuartoPiece> getPieces() {
		return pieces;
	}

	public final void reset() {
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				getMapQuarto()[i][j] = null;
			}
		}
		for (QuartoPiece piece : getPieces()) {
			int j = piece.getNumber() % 4;
			int k = piece.getNumber() / 4;
			piece.setTranslateX(QuartoModel.getTranslate(j));
			piece.setTranslateZ(QuartoModel.getTranslate(k));
		}

	}

	private boolean checkColumns() {
		return Stream.of(getMapQuarto()).filter(d -> Stream.of(d).noneMatch(Objects::isNull))
				.map(d -> Stream.of(d).map(QuartoPiece::getNumber).collect(Collectors.toList()))
				.anyMatch(QuartoModel::somethingInCommon);
	}

	private boolean checkRows() {
		for (int i = 0; i < 4; i++) {
			List<Integer> a = new ArrayList<>();
			for (int j = 0; j < 4; j++) {
				if (getMapQuarto()[j][i] == null) {
					break;
				}
				a.add(getMapQuarto()[j][i].getNumber());
				if (j == 3 && somethingInCommon(a)) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean checkSquares() {
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				if (neighborsNotNull(i, j)) {
					continue;
				}
				List<Integer> a = new ArrayList<>();
				a.add(getMapQuarto()[i][j].getNumber());
				a.add(getMapQuarto()[i][j + 1].getNumber());
				a.add(getMapQuarto()[i + 1][j].getNumber());
				a.add(getMapQuarto()[i + 1][j + 1].getNumber());
				if (somethingInCommon(a)) {
					return true;
				}

			}
		}
		return false;
	}

	private boolean neighborsNotNull(int i, int j) {
		return getMapQuarto()[i][j] == null || getMapQuarto()[i][j + 1] == null || getMapQuarto()[i + 1][j] == null
				|| getMapQuarto()[i + 1][j + 1] == null;
	}

	public static int getTranslate(int j) {
		final int border = 110;
		switch (j) {
			case 0:
				return -border;
			case 1:
				return -90;
			case 2:
				return 90;
			case 3:
			default:
				return border;

		}
	}

	private static boolean somethingInCommon(List<Integer> a) {
		int[] arr = new int[] { 1, 2, 4, 8 };
		for (int k = 0; k < arr.length; k++) {
			int l = arr[k];
			final long count = a.stream().map(n -> n & l).distinct().count();
			if (count == 1) {
				return true;
			}
		}
		return false;
	}

}
