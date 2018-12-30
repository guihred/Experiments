package graphs.app;

import graphs.entities.Cell;
import graphs.entities.CellType;
import graphs.entities.Graph;
import graphs.entities.GraphModelAlgorithms;
import java.util.List;
import java.util.Random;

public class DelaunayTopology extends BaseTopology {


	public DelaunayTopology(int size, Graph graph) {
		super(graph, "Delaunay", size);
	}

	public double distance(double x1, double x2, double y1, double y2) {
		double a = x1 - x2;
		double b = y1 - y2;
		return Math.sqrt(a * a + b * b);
	}

	@Override
	public void execute() {

		List<Cell> allCells = graph.getModel().getAllCells();
		setSize(allCells.size());
		graph.clean();
		graph.getModel().removeAllCells();
		Random rnd = new Random();
        final int bound = 150;
		double x = 0;
		double y = 0;
		for (int i = 0; i < getSize(); i++) {
			Cell cell = graph.getModel().addCell(BaseTopology.identifier(i), CellType.CIRCLE);
			double a = rnd.nextDouble() * 2 * Math.PI;
			x += bound * Math.cos(a);
			y += bound * Math.sin(a);
			cell.relocate(x, y);
		}

        GraphModelAlgorithms.triangulate(graph, graph.getModel().getAddedCells());

		graph.endUpdate();
	}






}