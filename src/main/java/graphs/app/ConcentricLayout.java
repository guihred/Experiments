package graphs.app;

import graphs.entities.Cell;
import graphs.entities.Edge;
import graphs.entities.Graph;
import graphs.entities.GraphModel;
import java.util.List;
import javafx.beans.NamedArg;

public class ConcentricLayout implements Layout {

	private final Graph graph;

    public ConcentricLayout(@NamedArg("graph") Graph graph) {
		this.graph = graph;
	}

	@Override
	public void execute() {

		GraphModel model = graph.getModel();
        graph.clean();
        List<Cell> cells = model.getAllCells();
        List<Edge> allEdges = model.getAllEdges();
        double w = graph.getScrollPane().getWidth() / 2;
        layoutConcentric(cells, allEdges, w);
    }

    public Graph getGraph() { 
        return graph;
    }

    public static void layoutConcentric(List<Cell> cells, List<Edge> allEdges, double center) {
        List<List<Cell>> cellsGroups = LayerSplitter.getLayers(cells, allEdges);
        for (int i = 0; i < cellsGroups.size(); i++) {
            List<Cell> list = cellsGroups.get(i);
            CircleLayout.generateCircle(list, allEdges, center, center, 135.0 / list.size() * i, i + 1);
        }
    }


}
