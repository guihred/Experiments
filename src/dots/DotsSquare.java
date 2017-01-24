package dots;

import static dots.StreamHelp.anyMatch;
import static dots.StreamHelp.filter;
import static dots.StreamHelp.flatMap;

import java.util.*;
import java.util.function.Function;
import javafx.beans.binding.Bindings;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class DotsSquare extends Region {

	public static final int SQUARE_SIZE = 40;

	final int i, j;
	final Set<DotsSquare> adjacencies = new HashSet<>();

	public DotsSquare(int i, int j) {
		// setStyle("-fx-background-color:green;");
		setPrefSize(SQUARE_SIZE, SQUARE_SIZE);
		this.i = i;
		this.j = j;
		setLayoutX(i * SQUARE_SIZE);
		setLayoutY(j * SQUARE_SIZE);

		final Circle circle = new Circle(SQUARE_SIZE / 2, SQUARE_SIZE / 2, 2, Color.BLACK);
		circle.fillProperty().bind(Bindings.when(hoverProperty()).then(Color.WHITE).otherwise(Color.BLACK));
		circle.centerYProperty().bind(heightProperty().divide(2));
		circle.centerXProperty().bind(widthProperty().divide(2));
		getChildren().addAll(circle);
	}

	@Override
	public String toString() {
		return "(" + i + "," + j + ")";
	}

	Double[] getCenter() {
		Double[] a = { getLayoutX() + getWidth() / 2, getLayoutY() + getHeight() / 2 };
		return a;
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash += i * 7;
		hash += j * 11;
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final DotsSquare other = (DotsSquare) obj;
		if (i != other.i) {
			return false;
		}
		if (j != other.j) {
			return false;
		}
		return true;
	}

	public void addAdj(DotsSquare selected) {
		adjacencies.add(selected);
		selected.adjacencies.add(this);
	}

	boolean contains(DotsSquare selected) {
		return adjacencies.contains(selected);
	}

	Set<Set<DotsSquare>> check() {
		final Set<DotsSquare> collect = filter(adjacencies,
				a -> anyMatch(a.adjacencies, b -> b != this && anyMatch(b.adjacencies, c -> a != c && c.adjacencies.contains(DotsSquare.this))));
		Set<Set<DotsSquare>> pontos = new HashSet<>();
		for (DotsSquare a : collect) {
			for (DotsSquare b : filter(a.adjacencies, b -> b != this && anyMatch(b.adjacencies, c -> a != c && c.adjacencies.contains(this)))) {
				for (DotsSquare c : filter(b.adjacencies, c -> a != c && c.adjacencies.contains(this))) {
					pontos.add(new LinkedHashSet<>(Arrays.asList(a, b, c, this)));
				}
			}
		}
		return pontos;
	}

	List<DotsSquare> almostSquare() {
		// ONE link away from being a square
		final List<DotsSquare> hopped = flatMap(
				adjacencies,
				a -> flatMap(filter(a.adjacencies, b -> b != this),
						b -> filter(b.adjacencies, (DotsSquare c) -> a != c && !c.contains(this) && Math.abs(c.i - i) + Math.abs(c.j - j) == 1)));
		return hopped;
	}

	List<DotsSquare> checkMelhor() {
		return almostSquare();
	}

	boolean checkMelhor(DotsSquare adj) {
		final Set<DotsSquare> arrayList = new HashSet<>(adjacencies);
		arrayList.add(adj);
		boolean good = flatMap(arrayList, (Function<DotsSquare, Collection<DotsSquare>>) a -> flatMap(filter(a.adjacencies, b -> b != this),
				b -> filter(b.adjacencies, (DotsSquare c) -> a != c && !c.contains(this) && Math.abs(c.i - i) + Math.abs(c.j - j) == 1))).size() == 0;
		return good;
	}

	void removeAdj(DotsSquare value) {
		adjacencies.remove(value);
		value.adjacencies.remove(this);
	}

}
