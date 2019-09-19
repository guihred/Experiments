package graphs.app;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

import graphs.entities.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javafx.beans.NamedArg;
import javafx.scene.paint.Color;

public class LayerLayout implements Layout {

    private final Graph graph;

    public LayerLayout(@NamedArg("graph") Graph graph) {
		this.graph = graph;
	}

	@Override
	public void execute() {

        GraphModel model = graph.getModel();
        graph.clean();
		List<Cell> cells = model.getAllCells();

        layoutInLayers(cells, model.getAllEdges());

    }

    public Graph getGraph() {
        return graph;
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
        final double bound = 800;
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

        List<List<Cell>> orderedCellGroups = LayerSplitter.getLayers(cells, addedEdges);
        final double bound = orderedCellGroups.stream()
            .mapToDouble(l -> l.stream().mapToDouble(Cell::getHeight).max().orElse(0)).sum();
        final int f = 400;
        double sum2 = orderedCellGroups.stream().mapToDouble(l -> l.stream().mapToDouble(Cell::getWidth).sum()).max()
            .orElse(0) + f;
        double layerHeight = bound / (orderedCellGroups.size() + 1) + 50;
        double y = layerHeight;
        for (List<Cell> list : orderedCellGroups) {
            double sum = list.stream().mapToDouble(Cell::getWidth).sum();
            double xStep = 5;
            double x = sum2 / 2 - sum / 2;
            for (int i = 0; i < list.size(); i++) {
                Cell cell = list.get(i);
                cell.relocate(x, y);

                x += cell.getWidth() + xStep;
            }
            y += layerHeight;
        }
    }

    public static long numberOfEdges(Entry<Color, List<Cell>> e, List<Edge> allEdges) {
        return e.getValue().stream().mapToLong(c -> GraphModelAlgorithms.edgesNumber(c, allEdges)).sum();
    }

}