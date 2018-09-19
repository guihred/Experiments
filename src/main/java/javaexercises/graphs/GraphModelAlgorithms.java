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

public class GraphModelAlgorithms {

    public static Integer addedCost(String v, String w, List<Edge> addedEdges, Map<String, Cell> cellMap) {
        return addedEdges.stream().filter(e -> e.source.equals(cellMap.get(v)) && e.target.equals(cellMap.get(w)))
                .map(Edge::getValor).findFirst().orElse(null);
    }

    public static List<Cell> adjacents(Cell c, List<Edge> allEdges) {
        return allEdges.stream().filter(e -> e.source.equals(c)).map(Edge::getTarget).collect(Collectors.toList());
    }


    public static List<Edge> chainEdges(String s, String t, Map<String, Cell> cellMap, List<Cell> allCells,
            List<Edge> allEdges, Map<Cell, Map<Cell, Cell>> paths) {
        Cell v1 = cellMap.get(s);
        Cell v2 = cellMap.get(t);
        for (Cell v : allCells) {
            dijkstra(v, allCells, allEdges, paths);
        }

        Map<Cell, Integer> dijkstra = dijkstra(v1, allCells, allEdges, paths);
        List<Edge> chain = new ArrayList<>();
        Cell path = v2;
        Integer integer = dijkstra.get(v2);
        if (integer != null && integer < Integer.MAX_VALUE) {
            while (path != v1) {
                Cell pathTo = pathTo(path, v1, paths);
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

    public static void coloring(List<Cell> allCells2, List<Edge> allEdges) {
        List<Color> availableColors = PieGraph.generateRandomColors(allCells2.size());
        int i = 0;
        List<Cell> vertices = allCells2.stream()
                .sorted(Comparator.comparing((Cell e) -> edgesNumber(e, allEdges)).reversed())
                .peek(p -> p.setColor(null)).collect(Collectors.toList());
        while (vertices.stream().anyMatch(v -> v.getColor() == null)) {
            List<Cell> v = vertices.stream().filter(c -> c.getColor() == null).collect(Collectors.toList());
            Color color = availableColors.get(i);
            for (int j = 0; j < v.size(); j++) {
                if (anyAdjacents(v.get(j), allEdges).stream().noneMatch(c -> c.getColor() == color)) {
                    v.get(j).setColor(color);
                }
            }
            i = (i + 1) % availableColors.size();
        }
    }


    public static void findArticulations(List<Cell> allCells, List<Edge> allEdges) {
        Map<Cell, Integer> num = new HashMap<>();
        Map<Cell, Integer> low = new HashMap<>();
        Map<Cell, Cell> parent = new HashMap<>();
        Cell s2 = allCells.get(0);
        assignNum(num, parent, 0, s2, allEdges);
        assignLow(num, low, parent, s2, allEdges);
    }


    public static List<Edge> kruskal(List<Cell> allCells, List<Edge> allEdges) {
        int numVertices = allCells.size();
        DisjSets ds = new DisjSets(numVertices);
        PriorityQueue<Edge> pq = new PriorityQueue<>(allEdges);
        List<Edge> mst = new ArrayList<>();
        while (mst.size() != numVertices - 1) {
            Edge e1 = pq.poll();
            int uset = ds.find(indexOf(e1.getSource().getCellId(), allCells));
            int vset = ds.find(indexOf(e1.getTarget().getCellId(), allCells));
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


    public static void sortTopology(List<Cell> allCells, List<Edge> allEdges) {
        int counter = 0;
        Map<Cell, Integer> indegree = new HashMap<>();
        Map<Cell, Integer> topNum = new HashMap<>();

        Queue<Cell> q = new LinkedList<>();
        for (Cell v : allCells) {
            for (Cell w : adjacents(v, allEdges)) {
                indegree.put(w, indegree.getOrDefault(w, 0) + 1);
            }
            if (indegree.getOrDefault(v, 0) == 0) {
                q.add(v);
            }
        }
        while (!q.isEmpty()) {
            Cell v = q.poll();
            topNum.put(v, ++counter);
            for (Cell w : adjacents(v, allEdges)) {
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

    public static Map<Cell, Integer> unweightedUndirected(String s, Map<String, Cell> cellMap, List<Cell> allCells,
            List<Edge> allEdges, Map<Cell, Map<Cell, Cell>> paths) {
        Cell source = cellMap.get(s);

        Map<Cell, Integer> distance = new HashMap<>();
        Map<Cell, Boolean> known = createDistanceMap(source, distance, allCells);
        for (int i = 0; i < allCells.size(); i++) {
            for (Cell v : allCells) {
                if (!known.get(v) && distance.get(v) == i) {
                    known.put(v, true);
                    for (Cell w : anyAdjacents(v, allEdges)) {
                        if (distance.get(w) == Integer.MAX_VALUE) {
                            distance.put(w, i + 1);
                            setPath(w, source, v, paths);
                        }
                    }

                }
            }
        }
        return distance;

    }

    private static List<Cell> anyAdjacents(Cell c, List<Edge> allEdges) {
        return allEdges.stream().filter(e -> e.source.equals(c) || e.target.equals(c))
                .flatMap(e -> Stream.of(e.getTarget(), e.getSource())).filter(e -> e != c).distinct()
                .collect(Collectors.toList());
    }

    private static void assignLow(Map<Cell, Integer> num, Map<Cell, Integer> low, Map<Cell, Cell> parent, Cell v,
            List<Edge> allEdges) {
        low.put(v, num.get(v));
        for (Cell w : adjacents(v, allEdges)) {
            if (num.get(w) > num.get(v)) {
                assignLow(num, low, parent, w, allEdges);
                if (low.get(w) >= num.get(v)) {
                    v.setSelected(true);
                }
                low.put(v, Integer.min(low.get(v), low.get(w)));

            } else if (parent.get(v) != w) {
                low.put(v, Integer.min(low.get(v), num.get(w)));
            }

        }
    }

    private static void assignNum(Map<Cell, Integer> num, Map<Cell, Cell> parent, int c, Cell s, List<Edge> allEdges) {
        int counter = c;
        num.put(s, counter++);
        for (Cell w : adjacents(s, allEdges)) {
            if (!num.containsKey(w)) {
                parent.put(w, s);
                assignNum(num, parent, counter, w, allEdges);
            }
        }
    }



    private static Integer cost(Cell v, Cell w, List<Edge> allEdges) {
        return allEdges.stream().filter(e -> e.source.equals(v) && e.target.equals(w)).map(Edge::getValor).findFirst()
                .orElse(null);
    }

    private static Map<Cell, Boolean> createDistanceMap(Cell source, Map<Cell, Integer> distance, List<Cell> allCells) {
        Map<Cell, Boolean> known = new HashMap<>();
        for (Cell v : allCells) {
            distance.put(v, Integer.MAX_VALUE);
            known.put(v, false);
        }
        distance.put(source, 0);
        return known;
    }

    private static Map<Cell, Integer> dijkstra(Cell s, List<Cell> allCells, List<Edge> allEdges,
            Map<Cell, Map<Cell, Cell>> paths) {
        Map<Cell, Integer> distance = new HashMap<>();
        Map<Cell, Boolean> known = createDistanceMap(s, distance, allCells);
        while (known.entrySet().stream().anyMatch(e -> !e.getValue())) {
            Cell v = getMinDistanceCell(distance, known);
            known.put(v, true);
            for (Cell w : adjacents(v, allEdges)) {
                if (!known.get(w)) {
                    Integer cvw = cost(v, w, allEdges);
                    if (distance.get(v) + cvw < distance.get(w)) {
                        distance.put(w, distance.get(v) + cvw);
                        setPath(w, s, v, paths);
                    }
                }
            }
        }
        return distance;
    }

    public static long edgesNumber(Cell c, List<Edge> allEdges) {
        return allEdges.stream().filter(e -> e.source.equals(c) || e.target.equals(c)).count();
    }

    private static Cell getMinDistanceCell(Map<Cell, Integer> distance, Map<Cell, Boolean> known) {
        return distance.entrySet().stream().filter(e -> !known.get(e.getKey()))
                .min(Comparator.comparing(Entry<Cell, Integer>::getValue))
                .orElseThrow(() -> new Exception("There should be someone")).getKey();
    }

    private static int indexOf(String s, List<Cell> allCells) {
        for (int i = 0; i < allCells.size(); i++) {
            if (allCells.get(i).getCellId().equals(s)) {
                return i;
            }
        }
        return -1;
    }


    private static Cell pathTo(Cell from, Cell to, Map<Cell, Map<Cell, Cell>> paths) {
        Map<Cell, Cell> map = paths.get(from);
        if (map == null) {
            return null;
        }
        return map.get(to);
    }

    private static void setPath(Cell from, Cell to, Cell by, Map<Cell, Map<Cell, Cell>> paths) {
        if (paths == null) {
            paths = new HashMap<>();
        }
        if (!paths.containsKey(from)) {
            paths.put(from, new HashMap<>());
        }
        paths.get(from).put(to, by);
    }


}