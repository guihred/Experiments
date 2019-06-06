package gaming.ex16;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class CreateMadMaze {
    private int r;

    private boolean goBackIn(List<MadTriangle> createdMaze, List<MadTriangle> history) {
        final MadTriangle remove = history.remove(history.size() - 1);
        r = createdMaze.indexOf(remove);
        return false;
    }

    private void handle(List<MadTriangle> maze, List<MadEdge> allEdges) {
        final List<MadTriangle> history = new ArrayList<>();
        final List<Character> check = new ArrayList<>();
        history.add(maze.get(0));
        while (!history.isEmpty()) {
            maze.get(r).setVisited(true);
            check.clear();
            Optional<MadTriangle> openC = maze.stream().filter(t -> t.hasVertex(maze.get(r).getA())
                && t.hasVertex(maze.get(r).getB()) && !t.hasVertex(maze.get(r).getC())).filter(e -> !e.isVisited())
                .findAny();
            Optional<MadTriangle> openB = maze.stream().filter(t -> t.hasVertex(maze.get(r).getA())
                && !t.hasVertex(maze.get(r).getB()) && t.hasVertex(maze.get(r).getC())).filter(e -> !e.isVisited())
                .findAny();
            Optional<MadTriangle> openA = maze.stream()
                .filter(t -> !t.hasVertex(maze.get(r).getA()) && t.hasVertex(maze.get(r).getB())
                    && t.hasVertex(maze.get(r).getC()))
                .filter(e -> !e.isVisited())

                .findAny();

            addIfPresent(check, openA.orElse(null), 'A');
            addIfPresent(check, openB.orElse(null), 'B');
            addIfPresent(check, openC.orElse(null), 'C');

            if (!check.isEmpty()) {
                history.add(maze.get(r));
                Collections.shuffle(check);
                Character direction = check.remove(0);
                removeEdgeOfTriangle(maze, allEdges, openC.orElse(null), openB.orElse(null), openA.orElse(null),
                    direction);
            } else {
                goBackIn(maze, history);
            }
        }

    }

    private void removeEdgeOfTriangle(List<MadTriangle> maze, List<MadEdge> allEdges, MadTriangle openC,
        MadTriangle openB, MadTriangle openA, Character direction) {
        if ('A' == direction && openA != null) {
            MadTriangle madTriangle = openA;
            MadCell cellB = maze.get(r).getB().getCell();
            MadCell cellC = maze.get(r).getC().getCell();
            allEdges.removeIf(e -> edgeHasCell(cellB, cellC, e));
            r = maze.indexOf(madTriangle);
        }
        if ('B' == direction && openB != null) {
            MadTriangle madTriangle = openB;
            MadCell cellA = maze.get(r).getA().getCell();
            MadCell cellC = maze.get(r).getC().getCell();
            allEdges.removeIf(e -> edgeHasCell(cellA, cellC, e));
            r = maze.indexOf(madTriangle);
        }
        if ('C' == direction && openC != null) {
            MadTriangle madTriangle = openC;
            MadCell cellA = maze.get(r).getA().getCell();
            MadCell cellB = maze.get(r).getB().getCell();
            allEdges.removeIf(e -> edgeHasCell(cellA, cellB, e));
            r = maze.indexOf(madTriangle);
        }
    }

    public static void createLabyrinth(List<MadTriangle> maze, List<MadEdge> allEdges) {
        new CreateMadMaze().handle(maze, allEdges);
    }

    private static void addIfPresent(final List<Character> check, MadTriangle openA, char a) {
        if (openA != null) {
            check.add(a);
        }
    }

    private static boolean edgeHasCell(MadCell cellB, MadCell cellC, MadEdge e) {
        return e.edgeHasCell(cellB, cellC);
    }

}