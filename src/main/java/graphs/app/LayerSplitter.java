package graphs.app;

import graphs.entities.Cell;
import graphs.entities.Edge;
import graphs.entities.GraphModelAlgorithms;
import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import utils.HasLogging;

public class LayerSplitter {
	private static final Logger LOG = HasLogging.log();
	private final List<Cell> cells;
    private final List<Edge> edges;
    private Set<Cell> stack = new LinkedHashSet<>();
    private Set<Cell> marked = new LinkedHashSet<>();

    LayerSplitter(List<Cell> cells, List<Edge> edges) {
        this.cells = new ArrayList<>(cells);
        this.edges = new LinkedList<>(edges);
    }

    public List<List<Cell>> orderVertices(List<List<Cell>> layers) {
        for (int i = 0; i < layers.size(); i++) {
            List<Cell> list = layers.get(i);
            if (list.size() > 1) {
                int ceil = (int) Math.ceil(list.size() / 2.);
                List<Cell> subList = new ArrayList<>(list.subList(0, ceil));
                List<Cell> subList2 = new ArrayList<>(list.subList(ceil, list.size()));
                subList.sort(Comparator.comparing((Cell t) -> GraphModelAlgorithms.edgesNumber(t, edges)));
                subList2.sort(Comparator.comparing((Cell t) -> GraphModelAlgorithms.edgesNumber(t, edges)).reversed());
                list.clear();
                list.addAll(subList);
                list.addAll(subList2);
            }
        }
        return layers;
    }

    private List<List<Cell>> assignLayers() {
        List<List<Cell>> sorted = new ArrayList<>();
        List<Edge> edges1 = new ArrayList<>(edges);
        List<Cell> vertices = new ArrayList<>(cells);
        List<Cell> start = getVerticesWithoutIncomingEdges(edges1, vertices);
        while (!start.isEmpty()) {
            sorted.add(start);
            List<Cell> st = start;
            edges1.removeIf(e -> st.contains(e.getSource()));
            vertices.removeIf(st::contains);
            start = getVerticesWithoutIncomingEdges(edges1, vertices);
        }
        return sorted;
    }

    @SuppressWarnings("unused")
    private long crossings(List<List<Cell>> layers) {
        return edges.stream().filter(e -> getLayerNumber(e.getSource(), layers) > getLayerNumber(e.getTarget(), layers))
            .count();
    }

    private void dfsRemove(Cell vertex) {
        if (marked.contains(vertex)) {
            return;
        }
        marked.add(vertex);
        stack.add(vertex);
        for (Edge edge : GraphModelAlgorithms.edges(vertex, edges)) {
            if (stack.contains(edge.getTarget())) {
                edges.remove(edge);
                edge.setSelected(true);
                LOG.trace("{} goes back", edge);
            } else if (!marked.contains(edge.getTarget())) {
                dfsRemove(edge.getTarget());
            }
        }
        stack.remove(vertex);
    }

    private List<Edge> removeCycles() {
        for (Cell vertex : cells) {
            dfsRemove(vertex);
        }
        return edges;
    }

    public static Object[] createVirtualVerticesAndEdges(List<List<Cell>> layers, List<Edge> ed) {
        int virtualIndex = 0;
        List<Edge> edges = new ArrayList<>(ed);
        for (int i = 0; i < layers.size() - 1; i++) {
            List<Cell> currentLayer = layers.get(i);
            List<Cell> nextLayer = layers.get(i + 1);
            for (Cell vertex : currentLayer) {
                List<Edge> outgoingMulti = edges.stream().filter(e -> e.getSource() == vertex)
                    .filter(e -> Math.abs(getLayerNumber(e.getTarget(), layers) - getLayerNumber(vertex, layers)) > 1)
                    .collect(Collectors.toList());
                List<Edge> incomingMulti = edges.stream().filter(e -> e.getTarget() == vertex)
                    .filter(e -> Math.abs(getLayerNumber(e.getSource(), layers) - getLayerNumber(vertex, layers)) > 1)
                    .collect(Collectors.toList());
                for (Edge edge : outgoingMulti) {
                    Cell virtualVertex = new Cell("" + virtualIndex++);
                    nextLayer.add(virtualVertex);
                    edges.remove(edge);
                    edges.add(new Edge(edge.getSource(), virtualVertex, edge.getValor()));
                    edges.add(new Edge(virtualVertex, edge.getTarget(), edge.getValor()));
                }
                for (Edge edge : incomingMulti) {
                    Cell virtualVertex = new Cell("" + virtualIndex++);
                    nextLayer.add(virtualVertex);
                    edges.remove(edge);
                    edges.add(new Edge(virtualVertex, edge.getTarget(), edge.getValor()));
                    edges.add(new Edge(edge.getSource(), virtualVertex, edge.getValor()));
                }
            }
        }
        return new Object[] { layers, edges };
    }

    public static int getLayerNumber(Cell vertex, List<List<Cell>> layers2) {
        for (int i = 0; i < layers2.size(); i++) {
            List<Cell> list = layers2.get(i);
            if (list.contains(vertex)) {
                return i;
            }
        }
        return 0;
    }

    public static List<List<Cell>> getLayers(List<Cell> cells, List<Edge> edges) {
        LayerSplitter cycleRemover = new LayerSplitter(cells, edges);
        cycleRemover.removeCycles();
        return cycleRemover.orderVertices(cycleRemover.assignLayers());
    }

    private static List<Cell> getVerticesWithoutIncomingEdges(List<Edge> edges, List<Cell> vertices) {
        List<Cell> targets = edges.stream().map(Edge::getTarget).distinct().collect(Collectors.toList());
        return vertices.stream().filter(v -> !targets.contains(v)).collect(Collectors.toList());
    }
}
