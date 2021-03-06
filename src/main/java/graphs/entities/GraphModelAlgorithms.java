package graphs.entities;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import ml.graph.ColorPattern;
import org.slf4j.Logger;
import utils.DisjSets;
import utils.ImageFXUtils;
import utils.SimpleSummary;
import utils.ex.HasLogging;

public final class GraphModelAlgorithms {

    private static final Logger LOG = HasLogging.log();

    private GraphModelAlgorithms() {
    }

    public static Integer addedCost(String v, String w, Collection<Edge> addedEdges, Map<String, Cell> cellMap) {
        return addedEdges.stream().filter(e -> e.source.equals(cellMap.get(v)) && e.target.equals(cellMap.get(w)))
                .map(Edge::getValor).findFirst().orElse(null);
    }

    public static List<Cell> adjacents(Cell c, Collection<Edge> allEdges) {
        return allEdges.stream().filter(e -> e.source.equals(c)).map(Edge::getTarget).collect(Collectors.toList());
    }

    public static boolean anyIntersection(Collection<? extends Node> cells, Node cell2) {
        return cells.stream().anyMatch(e -> e != cell2 && e.getBoundsInParent().intersects(cell2.getBoundsInParent()));
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
                chain.addAll(allEdges.stream().filter(e -> isEdgePresent(pathTo, p, e)).collect(Collectors.toList()));
                path = pathTo;

            }
        }
        return chain;
    }

    public static void coloring(Collection<Cell> allCells2, List<Edge> allEdges) {
        List<Color> availableColors = ImageFXUtils.generateRandomColors(allCells2.size());
        int i = 0;
        List<Cell> vertices =
                allCells2.stream().sorted(Comparator.comparingLong((Cell e) -> biedgesNumber(e, allEdges)).reversed())
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

    public static List<Edge> edges(Cell c, Collection<Edge> allEdges) {
        return allEdges.stream().filter(e -> e.source.equals(c)).collect(Collectors.toList());
    }

    public static long edgesNumber(Cell c, List<Edge> allEdges) {
        return allEdges.stream().filter(e -> e.source.equals(c)).count();
    }

    public static long edgesNumber(Cell c, List<Edge> allEdges, Collection<Cell> allCells) {
        return allEdges.stream().filter(e -> isEdgeContained(c, allCells, e)).count();
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
        while (mst.size() != numVertices - 1 && !pq.isEmpty()) {
            Edge e1 = pq.poll();
            int uset = ds.find(indexOf(e1.getSource().getCellId(), allCells));
            int vset = ds.find(indexOf(e1.getTarget().getCellId(), allCells));
            if (uset != vset) {
                mst.add(e1);
                ds.union(uset, vset);
            }
        }
        List<Edge> inverted = allEdges.stream().filter(
                e -> mst.stream().anyMatch(ed -> ed.getSource() == e.getTarget() && ed.getTarget() == e.getSource()))
                .collect(Collectors.toList());

        mst.addAll(inverted);

        return mst;
    }

    public static BigDecimal[] pageRank(List<Cell> allCells, List<Edge> allEdges) {
        BigDecimal[] pageRank = new BigDecimal[allCells.size()];
        BigDecimal[] pageRank2 = new BigDecimal[allCells.size()];
        BigDecimal full = BigDecimal.ONE.divide(BigDecimal.valueOf(allCells.size()), MathContext.DECIMAL32);
        Arrays.fill(pageRank, full);
        for (int k = 0; k < 2; k++) {
            for (int i = 0; i < pageRank.length; i++) {
                Cell cell = allCells.get(i);
                List<Edge> incomingEdges = incomingEdges(cell, allEdges);
                pageRank2[i] = BigDecimal.ZERO;
                if (incomingEdges.isEmpty()) {
                    pageRank2[i] = pageRank[i];
                }
                for (int j = 0; j < incomingEdges.size(); j++) {
                    Edge edge = incomingEdges.get(j);
                    int indexOf = allCells.indexOf(edge.source);
                    BigDecimal e = pageRank[indexOf];
                    long edgesNumber = edgesNumber(edge.source, allEdges);
                    pageRank2[i] = pageRank2[i].add(e.divide(BigDecimal.valueOf(edgesNumber), MathContext.DECIMAL32));
                }

            }
            pageRank = pageRank2;
        }

        SimpleSummary<BigDecimal> summary = Stream.of(pageRank).collect(new SimpleSummary<>());
        Map<String, BigDecimal> cellMap = new HashMap<>();
        for (int i = 0; i < pageRank.length; i++) {
            Cell cell = allCells.get(i);
            cellMap.put(cell.getCellId(), pageRank[i]);
            BigDecimal value = pageRank[i];
            cell.setColor(ColorPattern.HUE.getColorForValue(value, summary.getMin(), summary.getMax()));
            cell.addText("" + value);
        }
        BigDecimal[] rank = pageRank;
        String orderedPageRank = IntStream.range(0, pageRank.length).boxed().sorted(Comparator.comparing(e -> rank[e]))
                .map(allCells::get).map(Cell::getCellId).collect(Collectors.joining("\n\t", "\n\t", ""));

        LOG.info("ORDERED PAGE RANK = {}", orderedPageRank);

        return pageRank;
    }

    public static Map<Cell, Integer> sortTopology(Collection<Cell> allCells, List<Edge> allEdges) {
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
            LOG.info("CYCLE FOUND");
        }
        return topNum;
    }

    public static List<Triangle> triangulate(List<Cell> all) {
        List<Triangle> triangleSoup = new ArrayList<>();
        double maxOfAnyCoordinate = 0.0D;
        List<Ponto> pointSet = getPointSet(all);
        for (Ponto vector : pointSet) {
            maxOfAnyCoordinate = Math.max(Math.max(vector.getX(), vector.getY()), maxOfAnyCoordinate);
        }

        maxOfAnyCoordinate *= 16.0D;
        Ponto p1 = new Ponto(0.0D, 3.0D * maxOfAnyCoordinate, null);
        Ponto p2 = new Ponto(3.0D * maxOfAnyCoordinate, 0.0D, null);
        Ponto p3 = new Ponto(-3.0D * maxOfAnyCoordinate, -3.0D * maxOfAnyCoordinate, null);
        Triangle superTriangle = new Triangle(p1, p2, p3);
        triangleSoup.add(superTriangle);
        for (int i = 0; i < pointSet.size(); i++) {
            Ponto point = pointSet.get(i);
            Triangle triangle = triangleSoup.stream().filter(t6 -> t6.contains(point)).findFirst().orElse(null);

            if (triangle == null) {
                Ponto point2 = pointSet.get(i);
                Linha edge = triangleSoup.stream().map(t7 -> t7.findNearestEdge(point2)).sorted().findFirst()
                        .orElseThrow(() -> new RuntimeException("There should be someone")).getEdge();

                Triangle first = triangleSoup.stream().filter(t4 -> t4.isNeighbour(edge)).findFirst()
                        .orElseThrow(() -> new RuntimeException("There should be some triangle"));
                Triangle second = triangleSoup.stream().filter(t5 -> t5.isNeighbour(edge) && t5 != first).findFirst()
                        .orElseThrow(() -> new RuntimeException("There should be some triangle"));

                Ponto firstNoneEdgeVertex = first.getNoneEdgeVertex(edge);
                Ponto secondNoneEdgeVertex = second.getNoneEdgeVertex(edge);

                triangleSoup.remove(first);
                triangleSoup.remove(second);

                Triangle triangle1 = new Triangle(edge.getA(), firstNoneEdgeVertex, pointSet.get(i));
                Triangle triangle2 = new Triangle(edge.getB(), firstNoneEdgeVertex, pointSet.get(i));
                Triangle triangle3 = new Triangle(edge.getA(), secondNoneEdgeVertex, pointSet.get(i));
                Triangle triangle4 = new Triangle(edge.getB(), secondNoneEdgeVertex, pointSet.get(i));

                triangleSoup.add(triangle1);
                triangleSoup.add(triangle2);
                triangleSoup.add(triangle3);
                triangleSoup.add(triangle4);

                legalizeEdge(triangleSoup, triangle1, new Linha(edge.getA(), firstNoneEdgeVertex), pointSet.get(i));
                legalizeEdge(triangleSoup, triangle2, new Linha(edge.getB(), firstNoneEdgeVertex), pointSet.get(i));
                legalizeEdge(triangleSoup, triangle3, new Linha(edge.getA(), secondNoneEdgeVertex), pointSet.get(i));
                legalizeEdge(triangleSoup, triangle4, new Linha(edge.getB(), secondNoneEdgeVertex), pointSet.get(i));
            } else {
                Ponto a = triangle.getA();
                Ponto b = triangle.getB();
                Ponto c = triangle.getC();

                triangleSoup.remove(triangle);

                Triangle first = new Triangle(a, b, pointSet.get(i));
                Triangle second = new Triangle(b, c, pointSet.get(i));
                Triangle third = new Triangle(c, a, pointSet.get(i));

                triangleSoup.add(first);
                triangleSoup.add(second);
                triangleSoup.add(third);

                legalizeEdge(triangleSoup, first, new Linha(a, b), pointSet.get(i));
                legalizeEdge(triangleSoup, second, new Linha(b, c), pointSet.get(i));
                legalizeEdge(triangleSoup, third, new Linha(c, a), pointSet.get(i));
            }
        }

        triangleSoup.removeIf(t1 -> t1.hasVertex(superTriangle.getA()));
        triangleSoup.removeIf(t2 -> t2.hasVertex(superTriangle.getB()));
        triangleSoup.removeIf(t3 -> t3.hasVertex(superTriangle.getC()));

        return triangleSoup;
    }

    public static Map<Cell, Integer> unweightedUndirected(String s, Map<String, Cell> cellMap, List<Cell> allCells,
            List<Edge> allEdges, Map<Cell, Map<Cell, Cell>> paths) {
        Cell source = cellMap.get(s);

        Map<Cell, Integer> distance = new HashMap<>();
        Map<Cell, Boolean> known = createDistanceMap(source, distance, allCells);
        for (int i = 0; i < allCells.size(); i++) {
            for (Cell cell0 : allCells) {
                if (unknownDistance(distance, known, i, cell0)) {
                    known.put(cell0, true);
                    for (Cell cell1 : anyAdjacents(cell0, allEdges)) {
                        if (distance.get(cell1) == Integer.MAX_VALUE) {
                            distance.put(cell1, i + 1);
                            setPath(cell1, source, cell0, paths);
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
                    LOG.info("{}", v.getCellId());
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

    private static long biedgesNumber(Cell c, Collection<Edge> allEdges) {
        return allEdges.stream().filter(e -> e.source.equals(c) || e.target.equals(c)).count();
    }

    private static Integer cost(Cell v, Cell w, List<Edge> allEdges) {
        return allEdges.stream().filter(e -> e.source.equals(v) && e.target.equals(w)).map(Edge::getValor).findFirst()
                .orElse(0);
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

    private static Cell getMinDistanceCell(Map<Cell, Integer> distance, Map<Cell, Boolean> known) {
        return distance.entrySet().stream().filter(e -> !known.get(e.getKey()))
                .min(Comparator.comparing(Entry<Cell, Integer>::getValue))
                .orElseThrow(() -> new RuntimeException("There should be someone")).getKey();
    }

    private static List<Ponto> getPointSet(List<Cell> all) {

        return all.stream().map(c -> new Ponto(c.getLayoutX(), c.getLayoutY(), c)).collect(Collectors.toList());
    }

    private static List<Edge> incomingEdges(Cell c, Collection<Edge> allEdges) {
        return allEdges.stream().filter(e -> e.target.equals(c)).collect(Collectors.toList());
    }

    private static int indexOf(String s, List<Cell> allCells) {
        for (int i = 0; i < allCells.size(); i++) {
            if (allCells.get(i).getCellId().equals(s)) {
                return i;
            }
        }
        return -1;
    }

    private static boolean isEdgeContained(Cell c, Collection<Cell> allCells, Edge e) {
        return e.source.equals(c) && allCells.contains(e.target) || e.target.equals(c) && allCells.contains(e.source);
    }

    private static boolean isEdgePresent(Cell pathTo, Cell p, Edge e) {
        return e.source.equals(p) && e.target.equals(pathTo) || e.target.equals(p) && e.source.equals(pathTo);
    }

    private static void legalizeEdge(List<Triangle> triangleSoup1, Triangle triangle, Linha edge, Ponto newVertex) {
        Triangle neighbourTriangle =
                triangleSoup1.stream().filter(t -> t.isNeighbour(edge) && t != triangle).findFirst().orElse(null);
        if (neighbourTriangle != null && neighbourTriangle.isPointInCircumcircle(newVertex)) {
            triangleSoup1.remove(triangle);
            triangleSoup1.remove(neighbourTriangle);

            Ponto noneEdgeVertex = neighbourTriangle.getNoneEdgeVertex(edge);

            Triangle firstTriangle = new Triangle(noneEdgeVertex, edge.getA(), newVertex);
            Triangle secondTriangle = new Triangle(noneEdgeVertex, edge.getB(), newVertex);

            triangleSoup1.add(firstTriangle);
            triangleSoup1.add(secondTriangle);

            legalizeEdge(triangleSoup1, firstTriangle, new Linha(noneEdgeVertex, edge.getA()), newVertex);
            legalizeEdge(triangleSoup1, secondTriangle, new Linha(noneEdgeVertex, edge.getB()), newVertex);
        }
    }

    private static Cell pathTo(Cell from, Cell to, Map<Cell, Map<Cell, Cell>> paths) {
        Map<Cell, Cell> map = paths.get(from);
        if (map == null) {
            return null;
        }
        return map.get(to);
    }

    private static void setPath(Cell from, Cell to, Cell by, Map<Cell, Map<Cell, Cell>> p) {
        Map<Cell, Map<Cell, Cell>> paths = p;
        if (paths == null) {
            paths = new HashMap<>();
        }
        paths.computeIfAbsent(from, f -> new HashMap<>()).put(to, by);
    }

    private static boolean unknownDistance(Map<Cell, Integer> distance, Map<Cell, Boolean> known, int i, Cell cell0) {
        return !known.get(cell0) && distance.get(cell0) == i;
    }

}