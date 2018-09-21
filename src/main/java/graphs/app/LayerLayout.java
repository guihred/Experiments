package graphs.app;

import graphs.entities.*;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javafx.scene.paint.Color;

public class LayerLayout implements Layout {

	private Graph graph;

	public LayerLayout(Graph graph) {
		this.graph = graph;
	}

	@Override
	public void execute() {

        GraphModel model = graph.getModel();
        graph.clean();
		List<Cell> cells = model.getAllCells();
        layoutInLayers(cells, model.getAllEdges());
    }

    public static void layoutInLayers(List<Cell> cells, List<Edge> addedEdges) {
        GraphModelAlgorithms.coloring(cells, addedEdges);
        Map<Color, List<Cell>> collect = cells.stream().collect(Collectors.groupingBy(Cell::getColor));

        double bound = 800;
        List<List<Cell>> collect2 = collect.entrySet().stream()
                .sorted(Comparator.comparing(e -> numberOfEdges(e, addedEdges)))
                .map(Entry<Color, List<Cell>>::getValue).collect(Collectors.toList());
        double layerHeight = bound / (collect2.size() + 1);
        double y = layerHeight;
        for (List<Cell> list : collect2) {
            double xStep = bound / (list.size() + 1);
            double x = xStep;
            for (int i = 0; i < list.size(); i++) {
                Cell cell = list.get(i);
                cell.setColor(null);
                cell.relocate(x, y + i % 2 * layerHeight / 4.0);
                x += xStep;
            }
            y += layerHeight;
        }
    }

    public static long numberOfEdges(Entry<Color, List<Cell>> e, List<Edge> allEdges) {
        return e.getValue().stream().mapToLong(c -> GraphModelAlgorithms.edgesNumber(c, allEdges)).sum();
    }

}