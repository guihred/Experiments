package gaming.ex11;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.beans.binding.Bindings;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class DotsSquare extends Region {

    public static final int SQUARE_SIZE = 40;

    private final int i;
    private final int j;
    private final Set<DotsSquare> adjacencies = new HashSet<>();

    public DotsSquare(int i, int j) {
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

    public Double[] getCenter() {
        return new Double[] {getLayoutX() + getWidth() / 2, getLayoutY() + getHeight() / 2};
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash += getI() * 7;
        hash += getJ() * 11;
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
        return j == other.j;
    }

    public void addAdj(DotsSquare selected) {
        adjacencies.add(selected);
        selected.adjacencies.add(this);
    }

    public boolean contains(DotsSquare selected) {
        return adjacencies.contains(selected);
    }

    public Set<Set<DotsSquare>> check() {
        final List<DotsSquare> collect = adjacencies.stream()
                .filter(a -> a.adjacencies.stream()
                        .anyMatch(b -> b != this && b.adjacencies.stream()
                                .anyMatch(c -> a != c && c.adjacencies.contains(this)))).collect(Collectors.toList());
        Set<Set<DotsSquare>> pontos = new HashSet<>();
        for (DotsSquare a : collect) {
            for (DotsSquare b : a.adjacencies.stream()
                    .filter(b -> b != this && b.adjacencies.stream()
                            .anyMatch(c -> a != c && c.adjacencies.contains(this))).collect(Collectors.toList())) {
                for (DotsSquare c : b.adjacencies.stream()
                        .filter(c -> a != c && c.adjacencies.contains(this)).collect(Collectors.toList())) {
                    pontos.add(new LinkedHashSet<>(Arrays.asList(a, b, c, this)));
                }
            }
        }
        return pontos;
    }

    public Stream<DotsSquare> almostSquare() {
        // ONE link away from being a square
        return adjacencies.stream()
                .flatMap(a -> a.adjacencies.stream()
                        .filter(b -> b != this)
                        .flatMap(b -> b.adjacencies.stream()
                                .filter((DotsSquare c) -> a != c && !c.contains(this)
                                        && Math.abs(c.getI() - getI()) + Math.abs(c.getJ() - getJ()) == 1)));
    }

    public List<DotsSquare> checkMelhor() {
        return almostSquare().collect(Collectors.toList());
    }

    public boolean checkMelhor(DotsSquare adj) {
        final Set<DotsSquare> arrayList = new HashSet<>(adjacencies);
        arrayList.add(adj);

        return arrayList.stream()
                .flatMap(a -> a.adjacencies.stream()
                        .filter(b -> b != this)
                        .flatMap(b -> b.adjacencies.stream()
                                .filter((DotsSquare c) -> a != c && !c.contains(this)
                                        && Math.abs(c.getI() - getI()) + Math.abs(c.getJ() - getJ()) == 1))).count() == 0;
    }

    public void removeAdj(DotsSquare value) {
        adjacencies.remove(value);
        value.adjacencies.remove(this);
    }

	public int getI() {
		return i;
	}

	public int getJ() {
		return j;
	}

}
