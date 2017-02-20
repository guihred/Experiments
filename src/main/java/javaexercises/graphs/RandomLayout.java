package javaexercises.graphs;

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
		for (Cell cell : cells) {
			double x = rnd.nextDouble() * 500;
			double y = rnd.nextDouble() * 500;
			cell.relocate(x, y);
		}
	}

}