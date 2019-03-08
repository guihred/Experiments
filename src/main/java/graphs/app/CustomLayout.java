package graphs.app;

import graphs.entities.Cell;
import graphs.entities.Edge;
import graphs.entities.Graph;
import graphs.entities.GraphModel;
import graphs.entities.GraphModelAlgorithms;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

public class CustomLayout implements Layout {

	private static final	Random RND = new Random();


	private Graph graph;

	public CustomLayout(Graph graph) {
		this.graph = graph;
	}

	@Override
	public void execute() {

		GraphModel model = graph.getModel();
		graph.clean();
		List<Cell> cells = model.getAllCells();
		layoutInCustom(cells, model.getAllEdges());
	}
	public static void layoutInCustom(List<Cell> cells, List<Edge> allEdges) {
		cells.get(0).relocate(50, 50);
		Set<Cell> cellSet = cells.stream().collect(Collectors.toSet());
		final int bound = 150;

		for (Cell cell : cells) {
			List<Cell> edges = GraphModelAlgorithms.adjacents(cell, allEdges);
			for (Cell cell2 : edges) {
				if (cellSet.contains(cell2)) {
					int i = 0;
					final int maxAtempts = 20;
					do {
						int nextInt = RND.nextInt(bound * 2) - 180;
						double x = bound * (0.5 + Math.random()) * Math.cos(Math.toRadians(nextInt));
						double y = bound * (0.5 + Math.random()) * Math.sin(Math.toRadians(nextInt));
						cell2.relocate(x + cell.getLayoutX(), y + cell.getLayoutY());
						cellSet.remove(cell2);
					} while (i++ < maxAtempts && GraphModelAlgorithms.anyIntersection(cells, cell2));
				}
			}

		}
	}


}