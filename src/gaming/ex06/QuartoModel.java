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

    Circle[][] map = new Circle[4][4];
    QuartoPiece[][] mapQuarto = new QuartoPiece[4][4];
    List<QuartoPiece> pieces = new ArrayList<>();

    final void reset() {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                mapQuarto[i][j] = null;
            }
        }
        for (QuartoPiece piece : pieces) {
            int j = piece.number % 4;
            int k = piece.number / 4;
            piece.setTranslateX(j == 0 ? -110 : j == 1 ? -90 : j == 2 ? 90 : 110);
            piece.setTranslateZ(k == 0 ? -110 : k == 1 ? -90 : k == 2 ? 90 : 110);
        }

    }

    boolean checkEnd() {


		if (Stream.of(mapQuarto).filter(d -> Stream.of(d).noneMatch(Objects::isNull))
				.map(d -> Stream.of(d).map(q -> q.number).collect(Collectors.toList()))
				.anyMatch(this::somethingInCommon)) {
			return true;
		}


        for (int i = 0; i < 4; i++) {
            List<Integer> a = new ArrayList<>();
            for (int j = 0; j < 4; j++) {
                if (mapQuarto[j][i] == null) {
                    break;
                }
                a.add(mapQuarto[j][i].number);
                if (j == 3) {
					if (somethingInCommon(a)) {
						return true;
					}
                }
            }
        }
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (mapQuarto[i][j] == null || mapQuarto[i][j + 1] == null || mapQuarto[i + 1][j] == null || mapQuarto[i + 1][j + 1] == null) {
					continue;
                }
                List<Integer> a = new ArrayList<>();
                a.add(mapQuarto[i][j].number);
                a.add(mapQuarto[i][j + 1].number);
                a.add(mapQuarto[i + 1][j].number);
                a.add(mapQuarto[i + 1][j + 1].number);
				if (somethingInCommon(a)) {
					return true;
				}

            }
        }
        return false;
    }

	private boolean somethingInCommon(List<Integer> a) {
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
