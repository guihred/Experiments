package graphs.topology;

import graphs.entities.Cell;
import graphs.entities.Graph;
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
            double x = rnd.nextDouble() * bound;
            double y = rnd.nextDouble() * bound;
			cell.relocate(x, y);
		}
	}

}