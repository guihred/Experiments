package graphs.app;

import graphs.entities.Cell;
import graphs.entities.Edge;
import graphs.entities.GraphModelAlgorithms;
import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import utils.ex.HasLogging;

public final class LayerSplitter {
    private static final Logger LOG = HasLogging.log();
    private final List<Cell> cells;
    private final List<Edge> edges;
    private Set<Cell> stack = new LinkedHashSet<>();
    private Set<Cell> marked = new LinkedHashSet<>();

    private LayerSplitter(List<Cell> cells, List<Edge> edges) {
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
                LOG.info("{} goes back", edge);
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

    public static List<List<Cell>> getLayers(List<Cell> cells, Collection<Edge> edges) {
        LayerSplitter cycleRemover = new LayerSplitter(cells,
                edges.stream().sorted(Comparator.comparing(Edge::getValor)).collect(Collectors.toList()));
        cycleRemover.removeCycles();
        return cycleRemover.orderVertices(cycleRemover.assignLayers());
    }

    private static List<Cell> getVerticesWithoutIncomingEdges(List<Edge> edges, List<Cell> vertices) {
        List<Cell> targets = edges.stream().map(Edge::getTarget).distinct().collect(Collectors.toList());
        return vertices.stream().filter(v -> !targets.contains(v)).collect(Collectors.toList());
    }
}
