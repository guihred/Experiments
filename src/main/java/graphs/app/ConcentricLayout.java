package graphs.app;

import graphs.entities.*;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javafx.scene.paint.Color;

public class ConcentricLayout implements Layout {

	private final Graph graph;

	public ConcentricLayout(Graph graph) {
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

    public static void layoutConcentric(List<Cell> cells, List<Edge> allEdges, double center) {
        GraphModelAlgorithms.coloring(cells, allEdges);
        Map<Color, List<Cell>> cellByColor = cells.stream().collect(Collectors.groupingBy(Cell::getColor));
        List<List<Cell>> cellsGroups = cellByColor.entrySet().stream()
                .sorted(Comparator.comparing(e -> LayerLayout.numberOfEdges(e, allEdges)))
                .map(Entry<Color, List<Cell>>::getValue).collect(Collectors.toList());
        for (int i = 0; i < cellsGroups.size(); i++) {
            List<Cell> list = cellsGroups.get(i);
            list.forEach(e -> e.setColor(null));
            CircleLayout.generateCircle(list, allEdges, center, center, 180.0 / list.size() * i, i + 1);
        }
    }


}
