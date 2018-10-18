package graphs.app;

import graphs.entities.Cell;
import graphs.entities.Graph;
import graphs.entities.GraphModelAlgorithms;
import java.util.List;
import java.util.Random;

public class RandomLayout implements Layout {

	private Graph graph;

	private Random rnd = new Random();

	public RandomLayout(Graph graph) {
		this.graph = graph;
	}

	@Override
	public void execute() {

		List<Cell> cells = graph.getModel().getAllCells();
        graph.clean();
        int bound = 400;
		for (Cell cell : cells) {
			int i = 0;
			do {
				double x = rnd.nextDouble() * bound;
				double y = rnd.nextDouble() * bound;
				cell.relocate(x, y);
			} while (i++ < 20 && GraphModelAlgorithms.anyIntersection(cells, cell));
		}
	}

}