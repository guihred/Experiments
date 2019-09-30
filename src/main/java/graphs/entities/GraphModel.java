package graphs.entities;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;

public class GraphModel {

    private List<Cell> addedCells;

    private List<Edge> addedEdges;
    private final ObservableList<Cell> allCells = FXCollections.observableArrayList();
    private List<Edge> allEdges;

    private ObservableMap<String, Cell> cellMap; // <id,cell>
    private Cell graphParent;
    private Map<Cell, Map<Cell, Cell>> paths; // <id,cell>

    private List<Cell> removedCells;
    private List<Edge> removedEdges;

    private final ObservableList<String> cellIds = FXCollections.observableArrayList();

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
            .anyMatch(e -> isEdgeExistent(sourceId, targetId, e))) {
            return;
        }

        Cell sourceCell = cellMap.get(sourceId);
        Cell targetCell = cellMap.get(targetId);
        Edge edge = new Edge(sourceCell, targetCell, valor, false);
        Edge edge2 = new Edge(targetCell, sourceCell, valor, false);
        addedEdges.add(edge);
        addedEdges.add(edge2);
    }

    public Cell addCell(String id, CellType type) {
        switch (type) {
            case CIRCLE:
                return addCell(new CircleCell(id));
            case TRIANGLE:
                return addCell(new TriangleCell(id));
            case RECTANGLE:
                return addCell(new RectangleCell(id));
            default:
                throw new UnsupportedOperationException("Unsupported type: " + type);
        }
    }
    public Integer addedCost(String v, String w) {
        return GraphModelAlgorithms.addedCost(v, w, addedEdges, cellMap);
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
        if(sourceCell!=null&&targetCell!=null) {
            Edge edge = new Edge(sourceCell, targetCell, valor);
            addedEdges.add(edge);
        }
    }

    public List<Cell> adjacents(Cell c) {
        return GraphModelAlgorithms.adjacents(c, allEdges);
    }

    public void attachOrphansToGraphParent(Iterable<Cell> cellList) {

        for (Cell cell : cellList) {
            if (cell.getCellParents().isEmpty()) {
                graphParent.addCellChild(cell);
            }
        }

    }

    public List<Edge> chainEdges(String s, String t) {
        if (paths == null) {
            paths=new HashMap<>();
        }
        return GraphModelAlgorithms.chainEdges(s, t, cellMap, allCells, allEdges, paths);
    }

    public void clearSelected() {
        allCells.forEach(c -> c.setSelected(false));
        allEdges.forEach(c -> c.setSelected(false));
    }

    public void coloring() {
        GraphModelAlgorithms.coloring(allCells, allEdges);
    }

    public void disconnectFromGraphParent(Iterable<Cell> cellList) {
        for (Cell cell : cellList) {
            graphParent.removeCellChild(cell);
        }
    }

    public List<Edge> edges(Cell c) {
		return GraphModelAlgorithms.edges(c, allEdges);
    }


    public void findArticulations() {
        GraphModelAlgorithms.findArticulations(allCells, allEdges);
    }

    public List<Cell> getAddedCells() {
        return addedCells;
    }

    public List<Edge> getAddedEdges() {
        return addedEdges;
    }

    public ObservableList<Cell> getAllCells() {
        return allCells;
    }

    public List<Edge> getAllEdges() {
        return allEdges;
    }

    public Cell getCell(String key) {
		return cellMap.get(key);
	}

    public ObservableList<String> getCellIds() {
        return cellIds;
    }



    public ObservableMap<String, Cell> getCellMap() {
        return cellMap;
    }

    public List<Cell> getRemovedCells() {
        return removedCells;
    }

    public List<Edge> getRemovedEdges() {
        return removedEdges;
    }

    public List<Edge> kruskal() {
        return GraphModelAlgorithms.kruskal(allCells, allEdges);
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

    public void pageRank() {
        GraphModelAlgorithms.pageRank(allCells, allEdges);
    }

    public void removeAllCells() {
        removedCells.addAll(allCells);

    }

    public void removeAllEdges() {
        removedEdges.addAll(allEdges);
    }

    public void sortTopology() {
        GraphModelAlgorithms.sortTopology(allCells, allEdges);
    }

    public List<Triangle> triangulate(List<Cell> all) {
        removeAllEdges();
        List<Triangle> triangles = GraphModelAlgorithms.triangulate(all);
        for (Triangle t : triangles) {
            Cell cella = t.getA().getC();
            Cell cellb = t.getB().getC();
            Cell cellc = t.getC().getC();
            addBiEdge(cella.getCellId(), cellb.getCellId(), (int) t.getA().sub(t.getB()).mag());
            addBiEdge(cella.getCellId(), cellc.getCellId(), (int) t.getA().sub(t.getC()).mag());
            addBiEdge(cellc.getCellId(), cellb.getCellId(), (int) t.getB().sub(t.getC()).mag());
        }
        return triangles;
    }

    public Map<Cell, Integer> unweightedUndirected(String s) {
        return GraphModelAlgorithms.unweightedUndirected(s, cellMap, allCells, allEdges, paths);

    }

    private Cell addCell(Cell cell) {
        addedCells.add(cell);
        cellMap.put(cell.getCellId(), cell);
        return cell;

    }

    private void bindCellsId() {
        allCells.addListener((Change<? extends Cell> c) -> {
            while (c.next()) {
				cellIds.setAll(c.getList().stream().map(Cell::getCellId).collect(Collectors.toList()));
            }
        });
    }

    private final void clear() {
        allCells.clear();
        addedCells = FXCollections.observableArrayList();
        removedCells = FXCollections.observableArrayList();

        allEdges = FXCollections.observableArrayList();
        addedEdges = FXCollections.observableArrayList();
        removedEdges = FXCollections.observableArrayList();

        cellMap = FXCollections.observableHashMap();
        bindCellsId();
    }
    private static boolean isEdgeExistent(String sourceId, String targetId, Edge e) {
        return e.source.getCellId().equals(sourceId) && e.target.getCellId().equals(targetId)
                || e.target.getCellId().equals(sourceId) && e.source.getCellId().equals(targetId);
    }

}