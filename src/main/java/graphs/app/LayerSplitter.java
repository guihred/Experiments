package graphs.app;

import graphs.entities.Cell;
import graphs.entities.Edge;
import graphs.entities.GraphModelAlgorithms;
import java.util.*;
import java.util.stream.Collectors;

public class LayerSplitter {
    private final List<Cell> cells;
    private final List<Edge> edges;
    private Set<Cell> stack = new LinkedHashSet<>();
    private Set<Cell> marked = new LinkedHashSet<>();

    LayerSplitter(List<Cell> cells, List<Edge> edges) {
        this.cells = new ArrayList<>(cells);
        this.edges = new ArrayList<>(edges);
    }

    // [String[][],Edge[]]
    public List<List<Cell>> orderVertices(List<List<Cell>> layers) {
//        
        List<List<Cell>> best = layers.stream().collect(Collectors.toList());
        for (int i = 0; i < 24; i++) {
            Collections.shuffle(layers);
            if (crossings(layers) > crossings(best)) {
                best = layers.stream().collect(Collectors.toList());
            }
        }
        return best;
    }

    private List<List<Cell>> assignLayers() {
        List<List<Cell>> sorted = new ArrayList<>();
        List<Edge> edges1 = edges;
        List<Cell> vertices = cells;
        List<Cell> start = getVerticesWithoutIncomingEdges(edges1, vertices);
        while (start.size() > 0) {
            sorted.add(start);
            List<Cell> st = start;
            edges1.removeIf(e -> st.contains(e.getSource()));
            vertices.removeIf(v -> st.contains(v));
            start = getVerticesWithoutIncomingEdges(edges1, vertices);
        }
        return sorted;
    }

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
        List<Cell> targets = edges.stream().map(e -> e.getTarget()).distinct().collect(Collectors.toList());
        return vertices.stream().filter(v -> !targets.contains(v)).collect(Collectors.toList());
    }
}
