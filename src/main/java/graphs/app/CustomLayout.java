package graphs.app;

import graphs.entities.*;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class CustomLayout implements Layout {

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
        final double bound = 150.;

		for (Cell cell : cells) {
			List<Cell> edges = GraphModelAlgorithms.adjacents(cell, allEdges);
			for (Cell cell2 : edges) {
				if (cellSet.contains(cell2)) {
					int i = 0;
					final int maxAtempts = 20;
					do {
                        double rndAngle = BaseTopology.rnd(bound * 2);
                        double x = BaseTopology.rnd(bound) * Math.cos(Math.toRadians(rndAngle));
                        double y = BaseTopology.rnd(bound) * Math.sin(Math.toRadians(rndAngle));
						cell2.relocate(x + cell.getLayoutX(), y + cell.getLayoutY());
						cellSet.remove(cell2);
					} while (i++ < maxAtempts && GraphModelAlgorithms.anyIntersection(cells, cell2));
				}
			}

		}
	}



}