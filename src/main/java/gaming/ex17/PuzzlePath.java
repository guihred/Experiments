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
    WAVE((x, y) -> wave(x, y)),
    ROUNDED((x, y) -> {
        boolean b = x + y > 0;
        boolean m = x == 0;
        int i = x + y > 0 ? 1 : -1;
        List<PathElement> elements = new ArrayList<>();
        elements.addAll(b == m ? wave(i * y / 3, i * x / 3) : waveInverted(i * y / 3, i * x / 3));
        elements.add(new LineTo(x, y));
        elements.addAll(b == m ? waveInverted(i * -y / 3, i * -x / 3) : wave(i * -y / 3, i * -x / 3));
        return elements;
    }),
    ROUNDED_2((x, y) -> {
        boolean b = x + y > 0;
        boolean m = x == 0;
        int i = x + y > 0 ? 1 : -1;
        List<PathElement> elements = new ArrayList<>();
        elements.addAll(b == m ? waveInverted(i * -y / 3, i * -x / 3) : wave(i * -y / 3, i * -x / 3));
        elements.add(new LineTo(x, y));
        elements.addAll(b == m ? wave(i * y / 3, i * x / 3) : waveInverted(i * y / 3, i * x / 3));
        return elements;
    }),
	;

	private BiFunction<Double, Double, List<PathElement>> path;



	PuzzlePath(BiFunction<Double, Double, List<PathElement>> path) {
		this.path = path;
	}

	public List<PathElement> getPath(double x, double y) {
		List<PathElement> arrayList = new ArrayList<>();
        arrayList.addAll(STRAIGHT.path.apply(x / 3, y / 3));
        arrayList.addAll(path.apply(x / 3, y / 3));
        arrayList.addAll(STRAIGHT.path.apply(x / 3, y / 3));
		arrayList.forEach(e -> e.setAbsolute(false));
		return arrayList;
	}


	private static double nonZero(double a, double b) {
		return a != 0 ? a : b;
	}

    private static List<PathElement> wave(Double x, Double y) {
        boolean b = x + y > 0;
        boolean c = x > y;
        return Arrays.asList(new ArcTo((x + y) / 4, (x + y) / 4, 0, x / 2, y / 2, false, b && c ^ !b && !c),
            new ArcTo((x + y) / 4, (x + y) / 4, 0, x / 2, y / 2, false, !b || !(c ^ !b) || c));
    }

    private static List<PathElement> waveInverted(Double x, Double y) {
        List<PathElement> wave2 = new ArrayList<>(wave(x, y));
        for (int i = 0; i < wave2.size() / 2; i++) {
            PathElement pathElement = wave2.get(i);
            wave2.set(i, wave2.get(wave2.size() - 1 - i));
            wave2.set(wave2.size() - 1 - i, pathElement);
        }
        return wave2;
    }
}
