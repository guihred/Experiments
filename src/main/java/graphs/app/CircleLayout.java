package graphs.app;

import graphs.entities.*;
import java.util.*;
import java.util.stream.Collectors;

public class CircleLayout implements Layout {

	private Graph graph;

	public CircleLayout(Graph graph) {
		this.graph = graph;
	}

	@Override
	public void execute() {

        GraphModel model = graph.getModel();
        graph.clean();
        List<Cell> cells = model.getAllCells();
        generateCircle(cells, model.getAllEdges());
	}

	public static void generateCircle(Collection<Cell> cells, List<Edge> allEdges) {
        generateCircle(cells, allEdges, 0, 0, 0, 1);
    }

    public static void generateCircle(Collection<Cell> cells, List<Edge> allEdges, double centerX, double centerY,
            double startAngle, int mul) {
		Set<Cell> visited = new HashSet<>();
        int bound = radius(cells.size(), cells.size() == 1 ? 0 : mul,
                cells.stream().mapToDouble(Cell::getWidth).max().orElse(20));
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
                if (mul == 1) {
                    List<Cell> edges = GraphModelAlgorithms.adjacents(cell, allEdges).stream().filter(cells::contains)
                            .distinct().collect(Collectors.toList());
                    for (Cell cell2 : edges) {
                        if (!visited.contains(cell2)) {
                            double x1 = Math.cos(Math.toRadians(angle)) * bound;
                            double y1 = Math.sin(Math.toRadians(angle)) * bound;
                            cell2.relocate(x1 + centerX, y1 + centerY);
                            visited.add(cell2);
                            angle += step;
                        }
                    }
                }
            }
		}
	}

    public static int radius(int size2) {
        return radius(size2, 1, 20);
    }

    public static int radius(int size2, int mul, double cellBound) {
        int i = size2 / 30 + 1;
        return (int) (cellBound * i * mul);
    }

}