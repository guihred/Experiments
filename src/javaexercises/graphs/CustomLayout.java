package javaexercises.graphs;

import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

public class CustomLayout implements Layout {

	Graph graph;

	Random rnd = new Random();

	public CustomLayout(Graph graph) {
		this.graph = graph;
	}

	@Override
	public void execute() {

		GraphModel model = graph.getModel();
		List<Cell> cells = model.getAllCells();
		cells.get(0).relocate(50, 50);
		Set<Cell> collect = cells.stream().collect(Collectors.toSet());
		int bound = 150;

		for (Cell cell : cells) {
			List<Cell> edges = model.adjacents(cell);
			for (Cell cell2 : edges) {
				if (collect.contains(cell2)) {
					int nextInt = rnd.nextInt(180) - 180;
					double x = bound * Math.cos(Math.toRadians(nextInt));
					double y = bound * Math.sin(Math.toRadians(nextInt));
					cell2.relocate(x + cell.getLayoutX(), y + cell.getLayoutY());
					collect.remove(cell2);
				}
			}

		}
	}

}