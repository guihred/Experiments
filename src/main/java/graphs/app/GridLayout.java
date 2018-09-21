package graphs.app;

import graphs.entities.Cell;
import graphs.entities.Graph;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.Random;

public class GridLayout implements Layout {

	private Graph graph;


	public GridLayout(Graph graph) {
		this.graph = graph;
	}

	@Override
	public void execute() {

		Cell[] cells = graph.getModel().getAllCells().stream().toArray(Cell[]::new);
        graph.clean();
        layoutInGrid(cells, graph);
	}

    private static void layoutInGrid(Cell[] cells, Graph graph) {
        Random rnd = new Random();
        Cell cell2 = cells[0];
		Map<Cell, Integer> w = graph.getModel().unweightedUndirected(cell2.getCellId());
		Comparator<Cell> comparing = Comparator.comparing(w::get);
		Arrays.sort(cells, comparing);

		int size = cells.length;
		int sqrt = (int) Math.ceil(Math.sqrt(size));
		int radius = radius(size);
		double sqrt2 = Math.sqrt(3);
		for (int i = 0; i < size; i++) {
			Cell cell = cells[i];
			if (w.get(cell) == Integer.MAX_VALUE) {
				w.putAll(graph.getModel().unweightedUndirected(cell.getCellId()));
				Arrays.sort(cells, i, size, comparing);
			}
            int j = i / sqrt;
            int f = -radius / 2;
            double x = i % sqrt * radius + (j % 2 == 0 ? 0 : f) + rnd.nextInt(11) - 5d;
            int k = j * radius;
			double y = k * sqrt2 / 2;

			cell.relocate(x, y);
		}
    }

	public static int radius(int size2) {
		return 100 * (size2 / 50 + 1);
	}

}