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
        final List<String> check = new ArrayList<>();
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

            if (openA.isPresent()) {
                check.add("A");
            }
            if (openB.isPresent()) {
                check.add("B");
            }
            if (openC.isPresent()) {
                check.add("C");
            }

            if (!check.isEmpty()) {
                history.add(maze.get(r));
                final String direction = check.get(random.nextInt(check.size()));
                if ("A".equals(direction) && openA.isPresent()) {
                    MadTriangle madTriangle = openA.get();
                    MadCell cellB = maze.get(r).getB().getCell();
                    MadCell cellC = maze.get(r).getC().getCell();
                    allEdges.removeIf(e -> e.getSource().equals(cellC) && e.getTarget().equals(cellB)
                            || e.getSource().equals(cellB) && e.getTarget().equals(cellC));
                    r = maze.indexOf(madTriangle);
                }
                if ("B".equals(direction) && openB.isPresent()) {
                    MadTriangle madTriangle = openB.get();
                    MadCell cellA = maze.get(r).getA().getCell();
                    MadCell cellC = maze.get(r).getC().getCell();
                    allEdges.removeIf(e -> e.getSource().equals(cellC) && e.getTarget().equals(cellA)
                            || e.getSource().equals(cellA) && e.getTarget().equals(cellC));
                    r = maze.indexOf(madTriangle);
                }
                if ("C".equals(direction) && openC.isPresent()) {
                    MadTriangle madTriangle = openC.get();
                    MadCell cellA = maze.get(r).getA().getCell();
                    MadCell cellB = maze.get(r).getB().getCell();
                    allEdges.removeIf(e -> e.getSource().equals(cellB) && e.getTarget().equals(cellA)
                            || e.getSource().equals(cellA) && e.getTarget().equals(cellB));
                    r = maze.indexOf(madTriangle);
                }
            } else {
                getBackIn(maze, history);
            }
        }

    }

    private boolean getBackIn(List<MadTriangle> createdMaze, List<MadTriangle> history) {
        final MadTriangle remove = history.remove(history.size() - 1);
        r = createdMaze.indexOf(remove);
        return false;
    }
}