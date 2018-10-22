package graphs.app;

import graphs.entities.Cell;
import graphs.entities.Edge;
import graphs.entities.Graph;
import graphs.entities.GraphModelAlgorithms;
import java.util.List;
import java.util.Random;

public class RandomLayout implements Layout {

	private Graph graph;


	public RandomLayout(Graph graph) {
		this.graph = graph;
	}

	@Override
	public void execute() {

		List<Cell> cells = graph.getModel().getAllCells();
		 List<Edge> allEdges = graph.getModel().getAllEdges();
        graph.clean();
		layoutRandom(cells, allEdges);
	}

	public static void layoutRandom(List<Cell> cells, List<Edge> allEdges) {
		Random rnd = new Random();
		int bound = 400;
		for (Cell cell : cells) {
			int i = 0;
			int minIntersection = Integer.MAX_VALUE;
			double miny = 0;
			double minx = 0;
			List<Edge> edges = GraphModelAlgorithms.edges(cell, allEdges);
			do {
				double x = rnd.nextDouble() * bound;
				double y = rnd.nextDouble() * bound;
				cell.relocate(x, y);
				int j = 0;
				for (Edge edge : edges) {
					j += GraphModelAlgorithms.intersection(allEdges, edge);
				}
				if (minIntersection > j && !GraphModelAlgorithms.anyIntersection(cells, cell)) {
					minx = x;
					miny = y;
					minIntersection = j;
				}
			} while (minIntersection == Integer.MAX_VALUE
                    || i++ < 10 && GraphModelAlgorithms.anyIntersection(edges, allEdges));
<<<<<<< HEAD
=======

>>>>>>> branch 'master' of https://github.com/guihred/FXperiments.git
			cell.relocate(minx, miny);
		}
	}

}