package gaming.ex11;

import static gaming.ex11.DotsModel.MAZE_SIZE;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.beans.binding.Bindings;
import javafx.collections.ObservableMap;
import javafx.collections.ObservableSet;
import javafx.scene.Group;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;

public final class DotsHelper {
    private DotsHelper() {
    }

    public static boolean bNotContainsD(DotsSquare a, DotsSquare b, DotsSquare c, DotsSquare d) {
        return a.contains(b) && !b.contains(d) && d.contains(c) && c.contains(a);
    }

    public static boolean cNotContainsA(DotsSquare a, DotsSquare b, DotsSquare c, DotsSquare d) {
        return a.contains(b) && b.contains(d) && d.contains(c) && !c.contains(a);
    }

    public static DotsModel createModel(final BorderPane borderPane) {
        return new DotsModel(borderPane);
    }

    public static boolean dNotContainsC(DotsSquare a, DotsSquare b, DotsSquare c, DotsSquare d) {
        return a.contains(b) && b.contains(d) && !d.contains(c) && c.contains(a);
    }

    public static List<Map.Entry<DotsSquare, DotsSquare>> getBestPossibilities(DotsSquare[][] maze2) {
        List<Map.Entry<DotsSquare, DotsSquare>> melhor = new ArrayList<>();
        for (int i = 0; i < MAZE_SIZE; i++) {
            for (int j = 0; j < MAZE_SIZE; j++) {
                final List<DotsSquare> checkMelhor = maze2[i][j].checkMelhor();
                final DotsSquare maze1 = maze2[i][j];
                melhor.addAll(checkMelhor.stream().map(e -> new AbstractMap.SimpleEntry<>(maze1, e))
                    .collect(Collectors.toList()));
            }
        }
        return melhor;
    }

    public static List<Map.Entry<DotsSquare, DotsSquare>> getBestPossibilities2(DotsSquare[][] maze2) {
        final List<Map.Entry<DotsSquare, DotsSquare>> possibilities = getPossibilities(maze2);

        return possibilities.stream().filter((Map.Entry<DotsSquare, DotsSquare> entry) -> {
            final boolean checkMelhor = entry.getKey().checkMelhor(entry.getValue());
            if (!checkMelhor) {
                return false;
            }

            final boolean checkMelhor1 = entry.getValue().checkMelhor(entry.getKey());
            if (!checkMelhor1) {
                return false;
            }
            entry.getKey().addAdj(entry.getValue());
            final boolean criou = Stream.of(maze2).flatMap(Stream::of).flatMap(DotsSquare::almostSquare).count() > 0;
            entry.getKey().removeAdj(entry.getValue());
            return !criou;
        }).collect(Collectors.toList());
    }

    public static List<Map.Entry<DotsSquare, DotsSquare>> getBestPossibilities3(DotsSquare[][] maze2) {
        final List<Map.Entry<DotsSquare, DotsSquare>> possibilities = getPossibilities(maze2);
        final Map<Integer, List<Map.Entry<DotsSquare, DotsSquare>>> orderedPossibilities = possibilities.stream()
            .collect(Collectors.groupingBy(e -> getCountMap(e.getKey(), e.getValue(), maze2)));
        final int bestPossibility = orderedPossibilities.keySet().stream().mapToInt(i -> i).min().orElse(0);
        return orderedPossibilities.getOrDefault(bestPossibility, Collections.emptyList());
    }

    public static List<Map.Entry<DotsSquare, DotsSquare>> getPossibilities(DotsSquare[][] maze2) {
        List<Map.Entry<DotsSquare, DotsSquare>> possibilities = new ArrayList<>();
        for (int i = 0; i < MAZE_SIZE; i++) {
            for (int j = 0; j < MAZE_SIZE; j++) {
                if (i < MAZE_SIZE - 1 && !maze2[i][j].contains(maze2[i + 1][j])) {
                    possibilities.add(new AbstractMap.SimpleEntry<>(maze2[i][j], maze2[i + 1][j]));
                }
                if (j < MAZE_SIZE - 1 && !maze2[i][j].contains(maze2[i][j + 1])) {
                    possibilities.add(new AbstractMap.SimpleEntry<>(maze2[i][j], maze2[i][j + 1]));
                }
            }
        }
        Collections.shuffle(possibilities);
        return possibilities;
    }

    public static int getSumBySquare(DotsSquare a, DotsSquare b, int s, DotsSquare c, DotsSquare d,
        DotsSquare[][] maze) {
        int sum = s;
        if (cNotContainsA(a, b, c, d)) {
            sum++;
            sum += getCountMap(a, c, maze);
        }
        if (bNotContainsD(a, b, c, d)) {
            sum++;
            sum += getCountMap(b, d, maze);
        }
        if (dNotContainsC(a, b, c, d)) {
            sum++;
            sum += getCountMap(d, c, maze);
        }
        return sum;
    }

    public static void initializeMaze(Group gridPane1, DotsSquare[][] maze) {
        for (int i = 0; i < MAZE_SIZE; i++) {
            for (int j = 0; j < MAZE_SIZE; j++) {
                maze[i][j] = new DotsSquare(i, j);
                gridPane1.getChildren().add(maze[i][j]);
            }
        }
    }

    public static List<Entry<DotsSquare, DotsSquare>> notEmpty(List<Map.Entry<DotsSquare, DotsSquare>> possibilities,
        List<Entry<DotsSquare, DotsSquare>> bestPossibilities2) {
        return possibilities.isEmpty() ? bestPossibilities2 : possibilities;
    }

    public static Text pointsDisplay(String key,ObservableMap<String, ObservableSet<Set<DotsSquare>>> points) {
        Text tuPoints = new Text("0");
        tuPoints.textProperty().bind(
            Bindings.createStringBinding(() -> Integer.toString(points.get(key).size()), points.get("TU"), points));
        return tuPoints;
    }

    static int getCountMap(DotsSquare a, DotsSquare b, DotsSquare[][] maze) {
        a.addAdj(b);
        int sum = 0;
        int i = Integer.min(a.getI(), b.getI());
        int j = Integer.min(a.getJ(), b.getJ());
        if (a.getI() == b.getI()) {
            if (i > 0) {
                DotsSquare c = maze[i - 1][j];
                DotsSquare d = maze[i - 1][j + 1];
                sum = getSumBySquare(a, b, sum, c, d, maze);
            }
            if (i < MAZE_SIZE - 1) {
                DotsSquare c = maze[i + 1][j];
                DotsSquare d = maze[i + 1][j + 1];
                sum = getSumBySquare(a, b, sum, c, d, maze);
            }
        } else if (a.getJ() == b.getJ()) {
            if (j > 0) {
                DotsSquare c = maze[i][j - 1];
                DotsSquare d = maze[i + 1][j - 1];
                sum = getSumBySquare(a, b, sum, c, d, maze);
            }
            if (j < MAZE_SIZE - 1) {
                DotsSquare c = maze[i][j + 1];
                DotsSquare d = maze[i + 1][j + 1];
                sum = getSumBySquare(a, b, sum, c, d, maze);
            }

        }

        a.removeAdj(b);

        return sum;
    }

}
