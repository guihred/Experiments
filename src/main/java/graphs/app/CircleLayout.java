package graphs.app;

import graphs.entities.*;
import java.util.*;
import java.util.stream.Collectors;
import javafx.beans.NamedArg;

public class CircleLayout extends Layout {

    public CircleLayout(@NamedArg("graph") Graph graph) {
        super(graph);
    }

    @Override
    public void execute() {

        GraphModel model = graph.getModel();
        graph.clean();
        List<Cell> cells = model.getAllCells();
		double width = graph.getScrollPane().getViewportBounds().getWidth();
		double height = graph.getScrollPane().getViewportBounds().getHeight();
        generateCircle(cells, model.getAllEdges(), width / 2, height / 2, 2);
    }

    public static void generateCircle(Collection<Cell> cells, List<Edge> allEdges, double centerX, double centerY,
        double startAngle, double bound) {
        Set<Cell> visited = new HashSet<>();
        int size = cells.size();
        double step = 360.0 / size;
        double angle = startAngle;
        List<Cell> orderedCell = cells.stream()
            .sorted(Comparator.comparing(e -> GraphModelAlgorithms.edgesNumber(e, allEdges, cells)))
            .collect(Collectors.toList());
        for (Cell cell : orderedCell) {

            if (!visited.contains(cell)) {
                double x = Math.cos(Math.toRadians(angle)) * bound;
                double y = Math.sin(Math.toRadians(angle)) * bound;
                cell.relocate(x + centerX, y + centerY);
                visited.add(cell);
                angle += step;
            }
        }
    }

    public static void generateCircle(Collection<Cell> cells, List<Edge> allEdges, double centerX, double centerY,
        int mul) {
        int bound = radius(cells.size(), mul == 1 && cells.size() == 1 ? 0 : mul,
            cells.stream().mapToDouble(Cell::getWidth).max().orElse(20));
        generateCircle(cells, allEdges, centerX, centerY, 0, bound);
    }

    public static int radius(int size2) {
        return radius(size2, 1, 20);
    }

    public static int radius(int size2, int mul, double cellBound) {
        int i = size2 / 30 + 1;
        return (int) (cellBound * i * mul);
    }

}