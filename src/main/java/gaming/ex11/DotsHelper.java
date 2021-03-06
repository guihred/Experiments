package gaming.ex11;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.collections.ObservableMap;
import javafx.collections.ObservableSet;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Text;
import javafx.util.Duration;
import simplebuilder.SimpleTimelineBuilder;

public final class DotsHelper {
    protected static final int MAZE_SIZE = 6;

    private DotsHelper() {
    }

    public static void addBlueSquare(Iterable<Set<DotsSquare>> squaresFilled, Group gridPane2, Color value) {
        for (Set<DotsSquare> collect2 : squaresFilled) {
            double[] toArray = collect2.stream().flatMapToDouble(a -> DoubleStream.of(a.getCenter())).toArray();
            Polygon polygon = new Polygon(toArray);
            polygon.setFill(value);
            gridPane2.getChildren().add(polygon);
        }
    }

    public static void addPolygon(Group gridPane, double[] toArray, EventHandler<ActionEvent> onFinished, Color value,
            ActionEvent f) {
        Polygon polygon = new Polygon(toArray);
        polygon.setFill(value);
        addPolygonOnFinished(gridPane, polygon, onFinished, f);
    }

    public static void bindText(String key, Text textEu2,
            ObservableMap<String, ObservableSet<Set<DotsSquare>>> points2) {
        textEu2.textProperty().bind(Bindings.createStringBinding(
                () -> String.format("%s:%d", key, points2.get(key).size()), points2.get(key), points2));
    }

    public static Timeline createAnimation(int nplayed, double[] center, double[] center2, Line line2) {
        return new SimpleTimelineBuilder().addKeyFrame(Duration.seconds(nplayed / 2.), line2.endXProperty(), center[0])
                .addKeyFrame(Duration.seconds(1. / 2 + nplayed / 2.), line2.endXProperty(), center2[0])
                .addKeyFrame(Duration.seconds(nplayed / 2.), line2.endYProperty(), center[1])
                .addKeyFrame(Duration.seconds(1. / 2 + nplayed / 2.), line2.endYProperty(), center2[1]).build();
    }

    public static List<Map.Entry<DotsSquare, DotsSquare>> getBestPossibilities(DotsSquare[][] maze2) {
        List<Map.Entry<DotsSquare, DotsSquare>> melhor = new ArrayList<>();
        for (int i = 0; i < DotsHelper.MAZE_SIZE; i++) {
            for (int j = 0; j < DotsHelper.MAZE_SIZE; j++) {
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

    public static int getCountMap(DotsSquare a, DotsSquare b, DotsSquare[][] maze) {
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
            if (i < DotsHelper.MAZE_SIZE - 1) {
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
            if (j < DotsHelper.MAZE_SIZE - 1) {
                DotsSquare c = maze[i][j + 1];
                DotsSquare d = maze[i + 1][j + 1];
                sum = getSumBySquare(a, b, sum, c, d, maze);
            }
        }
        a.removeAdj(b);
        return sum;
    }

    public static List<Map.Entry<DotsSquare, DotsSquare>> getPossibilities(DotsSquare[][] maze2) {
        List<Map.Entry<DotsSquare, DotsSquare>> possibilities = new ArrayList<>();
        for (int i = 0; i < DotsHelper.MAZE_SIZE; i++) {
            for (int j = 0; j < DotsHelper.MAZE_SIZE; j++) {
                if (i < DotsHelper.MAZE_SIZE - 1 && !maze2[i][j].contains(maze2[i + 1][j])) {
                    possibilities.add(new AbstractMap.SimpleEntry<>(maze2[i][j], maze2[i + 1][j]));
                }
                if (j < DotsHelper.MAZE_SIZE - 1 && !maze2[i][j].contains(maze2[i][j + 1])) {
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
        for (int i = 0; i < DotsHelper.MAZE_SIZE; i++) {
            for (int j = 0; j < DotsHelper.MAZE_SIZE; j++) {
                maze[i][j] = new DotsSquare(i, j);
                gridPane1.getChildren().add(maze[i][j]);
            }
        }
    }

    public static boolean isCountPolygonOver(Group gridPane2) {
        int size = MAZE_SIZE - 1;
        return gridPane2.getChildren().stream().filter(e -> e instanceof Polygon).count() == size * size;
    }

    public static boolean isPointNeighborToCurrent(DotsSquare over, DotsSquare selected2) {
        return Math.abs(over.getI() - selected2.getI()) + Math.abs(over.getJ() - selected2.getJ()) == 1
                && !over.contains(selected2);
    }

    public static List<Entry<DotsSquare, DotsSquare>> notEmpty(List<Map.Entry<DotsSquare, DotsSquare>> possibilities,
            List<Entry<DotsSquare, DotsSquare>> bestPossibilities2) {
        return possibilities.isEmpty() ? bestPossibilities2 : possibilities;
    }

    private static void addPolygonOnFinished(Group gridPane, Polygon polygon, EventHandler<ActionEvent> onFinished,
            ActionEvent f) {
        if (onFinished != null) {
            onFinished.handle(f);
        }
        gridPane.getChildren().add(polygon);

    }

    private static boolean bNotContainsD(DotsSquare a, DotsSquare b, DotsSquare c, DotsSquare d) {
        return a.contains(b) && !b.contains(d) && d.contains(c) && c.contains(a);
    }

    private static boolean cNotContainsA(DotsSquare a, DotsSquare b, DotsSquare c, DotsSquare d) {
        return a.contains(b) && b.contains(d) && d.contains(c) && !c.contains(a);
    }

    private static boolean dNotContainsC(DotsSquare a, DotsSquare b, DotsSquare c, DotsSquare d) {
        return a.contains(b) && b.contains(d) && !d.contains(c) && c.contains(a);
    }

}
