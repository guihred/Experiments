package javaexercises.graphs;

import com.aspose.imaging.internal.Exceptions.Exception;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javaexercises.DisjSets;
import javafx.scene.paint.Color;
import ml.PieGraph;
import simplebuilder.HasLogging;

public class GraphModel {

    private List<Cell> addedCells;

    private List<Edge> addedEdges;
    private List<Cell> allCells;
    private List<Edge> allEdges;

    private Map<String, Cell> cellMap; // <id,cell>
    private Cell graphParent;
    private Map<Cell, Map<Cell, Cell>> paths; // <id,cell>

    private List<Cell> removedCells;
    private List<Edge> removedEdges;

    public GraphModel() {

        graphParent = new Cell("_ROOT_");
        // clear model, create lists
        clear();
    }

    public void addBiEdge(String sourceId, String targetId, Integer valor) {
        if (sourceId.equals(targetId)) {
            return;
        }
        if (addedEdges.stream()
                .anyMatch(e -> e.source.getCellId().equals(sourceId) && e.target.getCellId().equals(targetId)
                        || e.target.getCellId().equals(sourceId) && e.source.getCellId().equals(targetId))) {
            return;
        }

        Cell sourceCell = cellMap.get(sourceId);
        Cell targetCell = cellMap.get(targetId);
        Edge edge = new Edge(sourceCell, targetCell, valor, false);
        Edge edge2 = new Edge(targetCell, sourceCell, valor, false);
        addedEdges.add(edge);
        addedEdges.add(edge2);
    }

    private Cell addCell(Cell cell) {
        addedCells.add(cell);
        cellMap.put(cell.getCellId(), cell);
        return cell;

    }

    public Cell addCell(String id, CellType type) {
        switch (type) {
            case CIRCLE:
                return addCell(new CircleCell(id));
            case TRIANGLE:
                return addCell(new TriangleCell(id));
            default:
                throw new UnsupportedOperationException("Unsupported type: " + type);
        }
    }

    public Integer addedCost(String v, String w) {
        return addedEdges.stream().filter(e -> e.source.equals(cellMap.get(v)) && e.target.equals(cellMap.get(w)))
                .map(Edge::getValor).findFirst().orElse(null);
    }

    public void addEdge(String sourceId, String targetId, Integer valor) {
        if (sourceId.equals(targetId)) {
            return;
        }
        if (addedEdges.stream()
                .anyMatch(e -> e.source.getCellId().equals(sourceId) && e.target.getCellId().equals(targetId))) {
            return;
        }
        Cell sourceCell = cellMap.get(sourceId);
        Cell targetCell = cellMap.get(targetId);
        Edge edge = new Edge(sourceCell, targetCell, valor);
        addedEdges.add(edge);
    }

    public List<Cell> adjacents(Cell c) {
        return allEdges.stream().filter(e -> e.source.equals(c)).map(Edge::getTarget).collect(Collectors.toList());
    }

    private List<Cell> anyAdjacents(Cell c) {
        return allEdges.stream().filter(e -> e.source.equals(c) || e.target.equals(c))
                .flatMap(e -> Stream.of(e.getTarget(), e.getSource())).filter(e -> e != c).distinct()
                .collect(Collectors.toList());
    }

    private void assignLow(Map<Cell, Integer> num, Map<Cell, Integer> low, Map<Cell, Cell> parent, Cell v) {
        low.put(v, num.get(v));
        for (Cell w : adjacents(v)) {
            if (num.get(w) > num.get(v)) {
                assignLow(num, low, parent, w);
                if (low.get(w) >= num.get(v)) {
                    v.setSelected(true);
                }
                low.put(v, Integer.min(low.get(v), low.get(w)));

            } else if (parent.get(v) != w) {
                low.put(v, Integer.min(low.get(v), num.get(w)));
            }

        }
    }

    private void assignNum(Map<Cell, Integer> num, Map<Cell, Cell> parent, int c, Cell s) {
        int counter = c;
        num.put(s, counter++);
        for (Cell w : adjacents(s)) {
            if (!num.containsKey(w)) {
                parent.put(w, s);
                assignNum(num, parent, counter, w);
            }
        }
    }

    public void attachOrphansToGraphParent(List<Cell> cellList) {

        for (Cell cell : cellList) {
            if (cell.getCellParents().isEmpty()) {
                graphParent.addCellChild(cell);
            }
        }

    }

    public List<Edge> chainEdges(String s, String t) {
        Cell v1 = cellMap.get(s);
        Cell v2 = cellMap.get(t);
        for (Cell v : allCells) {
            dijkstra(v);
        }

        Map<Cell, Integer> dijkstra = dijkstra(v1);
        List<Edge> chain = new ArrayList<>();
        Cell path = v2;
        Integer integer = dijkstra.get(v2);
        if (integer != null && integer < Integer.MAX_VALUE) {
            while (path != v1) {
                Cell pathTo = pathTo(path, v1);
                if (pathTo == null) {
                    return Collections.emptyList();
                }
                Cell p = path;
                chain.addAll(allEdges.stream().filter(e -> e.source.equals(p) && e.target.equals(pathTo)
                        || e.target.equals(p) && e.source.equals(pathTo)).collect(Collectors.toList()));
                path = pathTo;

            }
        }
        return chain;
    }

    public final void clear() {

        allCells = new ArrayList<>();
        addedCells = new ArrayList<>();
        removedCells = new ArrayList<>();

        allEdges = new ArrayList<>();
        addedEdges = new ArrayList<>();
        removedEdges = new ArrayList<>();

        cellMap = new HashMap<>(); // <id,cell>

    }

    public void clearSelected() {
        allCells.forEach(c -> c.setSelected(false));
        allEdges.forEach(c -> c.setSelected(false));
    }

    public void coloring() {
        List<Color> availableColors = PieGraph.generateRandomColors(allCells.size());
        int i = 0;
        List<Cell> vertices = allCells.stream().sorted(Comparator.comparing(this::edgesNumber).reversed())
                .peek(p -> p.setColor(null)).collect(Collectors.toList());
        while (vertices.stream().anyMatch(v -> v.getColor() == null)) {
            List<Cell> v = vertices.stream().filter(c -> c.getColor() == null).collect(Collectors.toList());
            Color color = availableColors.get(i);
            for (int j = 0; j < v.size(); j++) {
                if (anyAdjacents(v.get(j)).stream().noneMatch(c -> c.getColor() == color)) {
                    v.get(j).setColor(color);
                }
            }
            i = (i + 1) % availableColors.size();
        }
    }

    public Integer cost(Cell v, Cell w) {
        return allEdges.stream().filter(e -> e.source.equals(v) && e.target.equals(w)).map(Edge::getValor).findFirst()
                .orElse(null);
    }

    private Map<Cell, Integer> dijkstra(Cell s) {
        Map<Cell, Integer> distance = new HashMap<>();
        Map<Cell, Boolean> known = createDistanceMap(s, distance);
        while (known.entrySet().stream().anyMatch(e -> !e.getValue())) {
            Cell v = getMinDistanceCell(distance, known);
            known.put(v, true);
            for (Cell w : adjacents(v)) {
                if (!known.get(w)) {
                    Integer cvw = cost(v, w);
                    if (distance.get(v) + cvw < distance.get(w)) {
                        distance.put(w, distance.get(v) + cvw);
                        setPath(w, s, v);
                    }
                }
            }
        }
        return distance;
    }

    private Cell getMinDistanceCell(Map<Cell, Integer> distance, Map<Cell, Boolean> known) {
        return distance.entrySet().stream().filter(e -> !known.get(e.getKey()))
                .min(Comparator.comparing(Entry<Cell, Integer>::getValue))
                .orElseThrow(() -> new Exception("There should be someone")).getKey();
    }

    public Map<Cell, Integer> dijkstra(String s) {
        return dijkstra(cellMap.get(s));
    }

    public void disconnectFromGraphParent(List<Cell> cellList) {

        for (Cell cell : cellList) {
            graphParent.removeCellChild(cell);
        }
    }

    public List<Edge> edges(Cell c) {
        return allEdges.stream().filter(e -> e.source.equals(c)).collect(Collectors.toList());
    }

    private long edgesNumber(Cell c) {
        return allEdges.stream().filter(e -> e.source.equals(c)).count();
    }

    public void findArticulations() {
        Map<Cell, Integer> num = new HashMap<>();
        Map<Cell, Integer> low = new HashMap<>();
        Map<Cell, Cell> parent = new HashMap<>();
        Cell s2 = allCells.get(0);
        assignNum(num, parent, 0, s2);
        assignLow(num, low, parent, s2);
    }

    public List<Cell> getAddedCells() {
        return addedCells;
    }

    public List<Edge> getAddedEdges() {
        return addedEdges;
    }

    public List<Cell> getAllCells() {
        return allCells;
    }

    public List<Edge> getAllEdges() {
        return allEdges;
    }

    public List<Cell> getRemovedCells() {
        return removedCells;
    }

    public List<Edge> getRemovedEdges() {
        return removedEdges;
    }

    private int indexOf(String s) {
        for (int i = 0; i < allCells.size(); i++) {
            if (allCells.get(i).getCellId().equals(s)) {
                return i;
            }
        }
        return -1;
    }

    public List<Edge> kruskal() {
        int numVertices = allCells.size();
        DisjSets ds = new DisjSets(numVertices);
        PriorityQueue<Edge> pq = new PriorityQueue<>(allEdges);
        List<Edge> mst = new ArrayList<>();
        while (mst.size() != numVertices - 1) {
            Edge e1 = pq.poll();
            int uset = ds.find(indexOf(e1.getSource().getCellId()));
            int vset = ds.find(indexOf(e1.getTarget().getCellId()));
            if (uset != vset) {
                mst.add(e1);
                ds.union(uset, vset);
            }
        }
        List<Edge> collect = allEdges.stream().filter(
                e -> mst.stream().anyMatch(ed -> ed.getSource() == e.getTarget() && ed.getTarget() == e.getSource()))
                .collect(Collectors.toList());

        mst.addAll(collect);

        return mst;
    }

    public void merge() {

        // cells
        allCells.addAll(addedCells);
        allCells.removeAll(removedCells);

        addedCells.clear();
        removedCells.clear();

        // edges
        allEdges.addAll(addedEdges);
        allEdges.removeAll(removedEdges);

        addedEdges.clear();
        removedEdges.clear();

    }

    private Cell pathTo(Cell from, Cell to) {
        Map<Cell, Cell> map = paths.get(from);
        if (map == null) {
            return null;
        }
        return map.get(to);
    }

    public void removeAllCells() {
        removedCells.addAll(allCells);

    }

    public void removeAllEdges() {
        removedEdges.addAll(allEdges);
    }

    public void setPath(Cell from, Cell to, Cell by) {
        if (paths == null) {
            paths = new HashMap<>();
        }
        if (!paths.containsKey(from)) {
            paths.put(from, new HashMap<>());
        }
        paths.get(from).put(to, by);
    }

    public void sortTopology() {
        int counter = 0;
        Map<Cell, Integer> indegree = new HashMap<>();
        Map<Cell, Integer> topNum = new HashMap<>();

        Queue<Cell> q = new LinkedList<>();
        for (Cell v : allCells) {
            for (Cell w : adjacents(v)) {
                indegree.put(w, indegree.getOrDefault(w, 0) + 1);
            }
            if (indegree.getOrDefault(v, 0) == 0) {
                q.add(v);
            }
        }
        while (!q.isEmpty()) {
            Cell v = q.poll();
            topNum.put(v, ++counter);
            for (Cell w : adjacents(v)) {
                indegree.put(w, indegree.getOrDefault(w, 0) - 1);
                if (indegree.getOrDefault(w, 0) == 0) {
                    q.add(w);
                }
            }

        }
        topNum.forEach((v, tn) -> v.addText(Integer.toString(tn)));

        if (counter != allCells.size()) {
            HasLogging.log().info("CYCLE FOUND");
        }
    }

    public Map<Cell, Integer> unweighted(String s) {
        Cell source = cellMap.get(s);

        Map<Cell, Integer> distance = new HashMap<>();
        Map<Cell, Boolean> known = createDistanceMap(source, distance);
        for (int i = 0; i < allCells.size(); i++) {
            for (Cell v : allCells) {
                if (!known.get(v) && distance.get(v) == i) {
                    known.put(v, true);
                    List<Cell> unreachables = adjacents(v).stream()
                            .filter(w -> Objects.equals(distance.get(w), Integer.MAX_VALUE))
                            .collect(Collectors.toList());
                    for (Cell w : unreachables) {
                        distance.put(w, i + 1);
                        setPath(w, source, v);
                    }

                }
            }
        }
        return distance;

    }

    private Map<Cell, Boolean> createDistanceMap(Cell source, Map<Cell, Integer> distance) {
        Map<Cell, Boolean> known = new HashMap<>();
        for (Cell v : allCells) {
            distance.put(v, Integer.MAX_VALUE);
            known.put(v, false);
        }
        distance.put(source, 0);
        return known;
    }

    public Map<Cell, Integer> unweightedUndirected(String s) {
        Cell source = cellMap.get(s);

        Map<Cell, Integer> distance = new HashMap<>();
        Map<Cell, Boolean> known = createDistanceMap(source, distance);
        for (int i = 0; i < allCells.size(); i++) {
            for (Cell v : allCells) {
                if (!known.get(v) && distance.get(v) == i) {
                    known.put(v, true);
                    for (Cell w : anyAdjacents(v)) {
                        if (distance.get(w) == Integer.MAX_VALUE) {
                            distance.put(w, i + 1);
                            setPath(w, source, v);
                        }
                    }

                }
            }
        }
        return distance;

    }

    public Map<Cell, Integer> weightedNegative(String s) {
        Cell source = cellMap.get(s);
        Map<Cell, Integer> distance = new HashMap<>();
        Queue<Cell> q = new LinkedList<>();

        for (Cell v : allCells) {
            distance.put(v, Integer.MAX_VALUE);
        }
        distance.put(source, 0);
        q.add(source);
        while (!q.isEmpty()) {
            Cell v = q.poll();

            for (Cell w : adjacents(v)) {
                Integer cvw = cost(v, w);
                if (distance.get(v) + cvw < distance.get(w)) {
                    distance.put(w, distance.get(v) + cvw);
                    setPath(w, source, v);

                    if (!q.contains(w)) {
                        q.add(w);
                    }
                }

            }
        }
        return distance;
    }

}