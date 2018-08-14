package gaming.ex16;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class CreateMadMaze {

    public static void createLabyrinth(List<MadTriangle> maze, List<MadEdge> allEdges) {
        new CreateMadMaze().handle(maze, allEdges);
    }

    int r = 0;

    void handle(List<MadTriangle> maze, List<MadEdge> allEdges) {
        final Random random = new Random();
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

            addIfPresent(check, openA, 'A');
            addIfPresent(check, openB, 'B');
            addIfPresent(check, openC, 'C');

            if (!check.isEmpty()) {
                history.add(maze.get(r));
                Character direction = check.get(random.nextInt(check.size()));
                removeEdgeOfTriangle(maze, allEdges, openC, openB, openA, direction);
            } else {
                getBackIn(maze, history);
            }
        }

    }

    private void removeEdgeOfTriangle(List<MadTriangle> maze, List<MadEdge> allEdges, Optional<MadTriangle> openC,
            Optional<MadTriangle> openB, Optional<MadTriangle> openA, Character direction) {
        if ('A' == direction && openA.isPresent()) {
            MadTriangle madTriangle = openA.get();
            MadCell cellB = maze.get(r).getB().getCell();
            MadCell cellC = maze.get(r).getC().getCell();
            allEdges.removeIf(e -> e.getSource().equals(cellC) && e.getTarget().equals(cellB)
                    || e.getSource().equals(cellB) && e.getTarget().equals(cellC));
            r = maze.indexOf(madTriangle);
        }
        if ('B' == direction && openB.isPresent()) {
            MadTriangle madTriangle = openB.get();
            MadCell cellA = maze.get(r).getA().getCell();
            MadCell cellC = maze.get(r).getC().getCell();
            allEdges.removeIf(e -> e.getSource().equals(cellC) && e.getTarget().equals(cellA)
                    || e.getSource().equals(cellA) && e.getTarget().equals(cellC));
            r = maze.indexOf(madTriangle);
        }
        if ('C' == direction && openC.isPresent()) {
            MadTriangle madTriangle = openC.get();
            MadCell cellA = maze.get(r).getA().getCell();
            MadCell cellB = maze.get(r).getB().getCell();
            allEdges.removeIf(e -> e.getSource().equals(cellB) && e.getTarget().equals(cellA)
                    || e.getSource().equals(cellA) && e.getTarget().equals(cellB));
            r = maze.indexOf(madTriangle);
        }
    }

    private void addIfPresent(final List<Character> check, Optional<MadTriangle> openA, char a) {
        if (openA.isPresent()) {
            check.add(a);
        }
    }

    private boolean getBackIn(List<MadTriangle> createdMaze, List<MadTriangle> history) {
        final MadTriangle remove = history.remove(history.size() - 1);
        r = createdMaze.indexOf(remove);
        return false;
    }
}