package gaming.ex17;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import javafx.scene.shape.ArcTo;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.PathElement;

public enum PuzzlePath {

	STRAIGHT((x, y) -> Arrays.asList(new LineTo(x, y))),
	ROUND((x, y) -> Arrays.asList(new ArcTo((x + y) / 2, (x + y) / 2, 0, x, y, false, x + y > 0))),
	ZIGZAGGED((x, y) -> {
		int i = x + y > 0 ? 1 : -1;
		return Arrays.asList(
				new LineTo(nonZero(i * y * PuzzlePiece.SQRT_2, x / 2.0), nonZero(i * x * PuzzlePiece.SQRT_2, y / 2.0)),
				new LineTo(nonZero(i * -y * PuzzlePiece.SQRT_2, x / 2.0), nonZero(i * -x * PuzzlePiece.SQRT_2, y / 2.0)));
	}),
	SQUARE((x, y) -> {
		int i = x + y > 0 ? 1 : -1;
		return Arrays.asList(new LineTo(i * y / 2, i * x / 2), new LineTo(x, y), new LineTo(i * -y / 2, i * -x / 2));
	}),
	WAVE((x, y) -> {
		boolean b = x + y > 0;
		boolean c = x == 0;
		boolean d = y == 0;
		return Arrays.asList(
				new ArcTo((x + y) / 4, (x + y) / 4, 0, x / 2, y / 2, false, b && c ^ !b && d),
				new ArcTo((x + y) / 4, (x + y) / 4, 0, x / 2, y / 2, false, !b || !(c ^ !b) || !d));
	})
	;

	private BiFunction<Double, Double, List<PathElement>> path;



	PuzzlePath(BiFunction<Double, Double, List<PathElement>> path) {
		this.path = path;
	}

	public List<PathElement> getPath(double x, double y) {
		List<PathElement> arrayList = new ArrayList<>();
		arrayList.addAll(STRAIGHT.path.apply(x / 4, y / 4));
		arrayList.addAll(path.apply(x / 2, y / 2));
		arrayList.addAll(STRAIGHT.path.apply(x / 4, y / 4));
		arrayList.forEach(e -> e.setAbsolute(false));
		return arrayList;
	}


	private static double nonZero(double a, double b) {
		return a != 0 ? a : b;
	}

}
