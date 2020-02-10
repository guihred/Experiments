package graphs.app;

import graphs.entities.Cell;
import graphs.entities.Graph;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.Random;
import javafx.beans.NamedArg;

public class GridLayout extends Layout {

    private static final Random RND = new Random();


    public GridLayout(@NamedArg("graph") Graph graph) {
        super(graph);
    }

    @Override
    public void execute() {

        Cell[] cells = graph.getModel().getAllCells().stream().toArray(Cell[]::new);
        graph.clean();
        layoutInGrid(cells, graph);
    }


    public static int radius(int size2) {
        return 100 * (size2 / 50 + 1);
    }

    private static void layoutInGrid(Cell[] cells, Graph graph) {
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
            double x = i % sqrt * radius + (j % 2 == 0 ? 0 : f) + RND.nextInt(11) - 5.;
            int k = j * radius;
            double y = k * sqrt2 / 2;

            cell.relocate(x, y);
        }
    }

}