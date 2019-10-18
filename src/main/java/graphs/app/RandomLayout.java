package graphs.app;

import graphs.entities.Cell;
import graphs.entities.Graph;
import graphs.entities.GraphModelAlgorithms;
import java.util.Collection;
import java.util.List;
import javafx.beans.NamedArg;

public class RandomLayout extends Layout {

    public RandomLayout(@NamedArg("graph") Graph graph) {
        super(graph);
    }

    @Override
    public void execute() {
        List<Cell> cells = graph.getModel().getAllCells();
        graph.clean();
		double width = graph.getScrollPane().getViewportBounds().getWidth();
		double height = graph.getScrollPane().getViewportBounds().getHeight();

		layoutRandomly(cells, width, height);
    }


	public static void layoutRandomly(Collection<Cell> cells, double width, double height) {
		for (Cell cell : cells) {
			double x = BaseTopology.rndPositive(width);
			double y = BaseTopology.rndPositive(height);
			cell.relocate(x, y);
			if (GraphModelAlgorithms.anyIntersection(cells, cell)) {
				x = Math.min(width - cell.getWidth(), Math.max(0, x + BaseTopology.rnd(cell.getWidth() * 2)));
				y = Math.min(height - cell.getHeight(), Math.max(0, y + BaseTopology.rnd(cell.getHeight() * 2)));
				cell.relocate(x, y);
			}
		}
	}

}