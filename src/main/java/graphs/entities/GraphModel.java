package graphs.entities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

	public Cell getCell(String key) {
		return cellMap.get(key);
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
        Edge edge = new Edge(sourceCell, targetCell, valor);
        addedEdges.add(edge);
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
        return allEdges.stream().filter(e -> e.source.equals(c)).collect(Collectors.toList());
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

    public void removeAllCells() {
        removedCells.addAll(allCells);

    }

    public void removeAllEdges() {
        removedEdges.addAll(allEdges);
    }

    public void sortTopology() {
        GraphModelAlgorithms.sortTopology(allCells, allEdges);
    }

    public Map<Cell, Integer> unweightedUndirected(String s) {
        return GraphModelAlgorithms.unweightedUndirected(s, cellMap, allCells, allEdges, paths);

    }

    private Cell addCell(Cell cell) {
        addedCells.add(cell);
        cellMap.put(cell.getCellId(), cell);
        return cell;

    }

    private final void clear() {

        allCells = new ArrayList<>();
        addedCells = new ArrayList<>();
        removedCells = new ArrayList<>();

        allEdges = new ArrayList<>();
        addedEdges = new ArrayList<>();
        removedEdges = new ArrayList<>();

        cellMap = new HashMap<>(); // <id,cell>

    }



}