package javaexercises.graphs;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class CircleLayout implements Layout {

	Graph graph;

	public CircleLayout(Graph graph) {
		this.graph = graph;
	}

	@Override
	public void execute() {

		List<Cell> cells = graph.getModel().getAllCells();
		Model model = graph.getModel();
		generateCircle(cells, model, 0, 0);
	}

	public static int radius(int size2) {
		return 200 * (size2 / 50 + 1);
	}

	public static void generateCircle(Collection<Cell> cells, Model model, double centerX, double centerY) {
		Set<Cell> visited = new HashSet<>();
		int bound = radius(cells.size());
		int size = cells.size();
		double step = 360.0 / size;
		double angle = 0;
		for (Cell cell : cells) {

			List<Cell> edges = model.adjacents(cell).stream().distinct().collect(Collectors.toList());
			if (!visited.contains(cell)) {
				double x = Math.cos(Math.toRadians(angle)) * bound;
				double y = Math.sin(Math.toRadians(angle)) * bound;
				cell.relocate(x + centerX, y + centerY);
				visited.add(cell);
				angle += step;
			}
			for (Cell cell2 : edges) {
				if (!visited.contains(cell2)) {
					double x = Math.cos(Math.toRadians(angle)) * bound;
					double y = Math.sin(Math.toRadians(angle)) * bound;
					cell2.relocate(x + centerX, y + centerY);
					visited.add(cell2);
					angle += step;
				}
			}
		}
	}

}