package graphs.app;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

import graphs.entities.*;
import java.util.*;
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

        displayInLayers(cells, model.getAllEdges());

    }

    public static void displayInLayers(Iterable<Cell> cells, List<Edge> allEdges) {
        Map<Cell, Integer> layers = new HashMap<>();
        cells.forEach(e -> layers.put(e, 0));
        Collections.shuffle(allEdges);
        for (Edge edge : allEdges) {
            Integer srcLayer = layers.get(edge.getSource());
            Integer tgtLayer = layers.get(edge.getTarget());
            if (srcLayer >= tgtLayer) {
                layers.put(edge.getTarget(), srcLayer + 1);
            }
        }
        Map<Integer, List<Cell>> layerMap = layers.entrySet().stream()
                .collect(groupingBy(Entry<Cell, Integer>::getValue, mapping(Entry<Cell, Integer>::getKey, toList())));
        int numOfLayers = layerMap.keySet().stream().mapToInt(e -> e).max().orElse(1);
        double bound = 800;
        layerMap.forEach((lay, cels) -> {
            double layerHeight = bound / numOfLayers / 2;
            double y = layerHeight * lay;
            double xStep = bound / (cels.size() + 1);
            double x = xStep;

            int sqrt = Integer.max((int) Math.sqrt(cels.size()) - 1, 1);
            for (int i = 0; i < cels.size(); i++) {
                Cell cell = cels.get(i);
                cell.setColor(null);
                cell.relocate(x, y + i % sqrt * layerHeight);
                x += xStep;
            }
        });
    }

    public static void layoutInLayers(List<Cell> cells, List<Edge> addedEdges) {
        GraphModelAlgorithms.coloring(cells, addedEdges);
        Map<Color, List<Cell>> cellByColor = cells.stream().collect(Collectors.groupingBy(Cell::getColor));

        List<List<Cell>> orderedCellGroups = cellByColor.entrySet().stream()
                .sorted(Comparator.comparing(e -> numberOfEdges(e, addedEdges)))
                .map(Entry<Color, List<Cell>>::getValue).collect(Collectors.toList());
        double bound = 800;
        double layerHeight = bound / (orderedCellGroups.size() + 1);
        double y = layerHeight;
        for (List<Cell> list : orderedCellGroups) {
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