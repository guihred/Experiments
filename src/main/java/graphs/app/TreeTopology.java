package graphs.app;

import graphs.entities.*;
import java.util.List;
import java.util.Random;

public class TreeTopology extends BaseTopology {

	public TreeTopology(int size, Graph graph) {
		super(graph, "Tree", size);
	}



	@Override
	public void execute() {
		graph.clean();
		graph.getModel().removeAllCells();
		graph.getModel().removeAllEdges();
		Random rnd = new Random();
		int bound = 100;
		int nextInt = rnd.nextInt(180) - 180;
		double x = bound * Math.cos(Math.toRadians(nextInt));
		double y = bound * Math.sin(Math.toRadians(nextInt));
		for (int i = 0; i < getSize(); i++) {
			Cell cell = graph.getModel().addCell(BaseTopology.identifier(i), CellType.CIRCLE);
			nextInt = rnd.nextInt(360);
			x += bound * Math.cos(Math.toRadians(nextInt));
			y += bound * Math.sin(Math.toRadians(nextInt));
			cell.relocate(x, y);
		}
		List<Cell> cells = graph.getModel().getAddedCells();
		for (Cell cell : cells) {
			double x1 = cell.getLayoutX();
			double y1 = cell.getLayoutY();
			for (Cell cell2 : cells) {
				double m = (x1 + cell2.getLayoutX()) / 2;
				double n = (y1 + cell2.getLayoutY()) / 2;
                double r = EdgeDistancePack.distance(x1, cell2.getLayoutX(), y1, cell2.getLayoutY());
                if (cells.stream().filter(c -> c != cell && c != cell2)
                        .noneMatch(c -> EdgeDistancePack.distance(m, c.getLayoutX(), n, c.getLayoutY()) <= r / 2)) {
					graph.getModel().addBiEdge(cell.getCellId(), cell2.getCellId(), 1);
				}

			}
		}
		graph.endUpdate();
		List<Edge> kruskal = graph.getModel().kruskal();
		graph.getModel().removeAllEdges();
		for (Edge edge : kruskal) {
			graph.getModel().addBiEdge(edge.getSource().getCellId(), edge.getTarget().getCellId(), 1);
		}
		graph.endUpdate();
		graph.sortChildren();
	}


}