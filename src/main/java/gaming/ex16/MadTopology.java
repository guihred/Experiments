package gaming.ex16;

import com.aspose.imaging.internal.Exceptions.Exception;
import gaming.ex16.MadTriangle.MadEdgeDistance;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.stream.Collectors;
import javaexercises.DisjSets;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class MadTopology {
    private List<MadCell> allCells = new ArrayList<>();
    private List<MadEdge> allEdges = new ArrayList<>();

    public void drawShapes(GraphicsContext gc) {
        gc.setStroke(Color.BLUE);
        gc.setLineWidth(1);

        for (MadEdge c : allEdges) {

            gc.setStroke(c.isMain() ? Color.RED : Color.BLUE);
            gc.strokeLine(c.getSource().getX(), c.getSource().getY(), c.getTarget().getX(), c.getTarget().getY());
        }
    }
    public List<MadTriangle> execute(float maxWidth, float maxHeight) {

        int sqrt = 10;
        float radius = maxWidth / 10;
        float sqrt2 = (float) Math.sqrt(3);
        int m = (int) (maxHeight / radius / sqrt2 * 2) + 1;

        int size = 10 * m;
        for (int i = 0; i < size; i++) {
            MadCell cell = new MadCell(i);
            int n = i / sqrt;
            float x = i % sqrt * radius + (n % 2 == 0 ? 0f : -radius / 2) + 30;
            int j = i / sqrt;
            float k = j * radius;
            float y = k * sqrt2 / 2;
            cell.relocate(x, y);
            allCells.add(cell);
        }

        List<MadTriangle> triangulate = triangulate(allCells);
        CreateMadMaze.createLabyrinth(triangulate, allEdges);
        return triangulate;
    }

    public int indexOf(int s) {
        for (int i = 0; i < allCells.size(); i++) {
            if (allCells.get(i).getId() == s) {
                return i;
            }
        }
        return -1;
    }

    public List<MadEdge> kruskal() {
        int numVertices = allCells.size();
        DisjSets ds = new DisjSets(numVertices);
        PriorityQueue<MadEdge> pq = new PriorityQueue<>(allEdges);
        List<MadEdge> mst = new ArrayList<>();
        while (mst.size() != numVertices - 1) {
            MadEdge e1 = pq.poll();
            int uset = ds.find(indexOf(e1.getSource().getId()));
            int vset = ds.find(indexOf(e1.getTarget().getId()));
            if (uset != vset) {
                mst.add(e1);
                ds.union(uset, vset);
            }
        }
        List<MadEdge> collect = allEdges.stream().filter(
                e -> mst.stream().anyMatch(ed -> ed.getSource() == e.getTarget() && ed.getTarget() == e.getSource()))
                .collect(Collectors.toList());

        mst.addAll(collect);
        mst.forEach(e -> e.setMain(true));

        return mst;
    }

    private static List<MadPonto> getPointSet(List<MadCell> all) {
        return all.stream().map(c -> new MadPonto(c.getX(), c.getY(), c)).collect(Collectors.toList());
    }

    private static void legalizeEdge(List<MadTriangle> triangleSoup1, MadTriangle triangle, MadLinha edge,
            MadPonto newVertex) {
        MadTriangle neighbourMadTriangle = triangleSoup1.stream().filter(t -> t.isNeighbour(edge) && t != triangle)
                .findFirst().orElse(null);
        if (neighbourMadTriangle != null && neighbourMadTriangle.isPointInCircumcircle(newVertex)) {
            triangleSoup1.remove(triangle);
            triangleSoup1.remove(neighbourMadTriangle);

            MadPonto noneEdgeVertex = neighbourMadTriangle.getNoneEdgeVertex(edge);

            MadTriangle firstMadTriangle = new MadTriangle(noneEdgeVertex, edge.getA(), newVertex);
            MadTriangle secondMadTriangle = new MadTriangle(noneEdgeVertex, edge.getB(), newVertex);

            triangleSoup1.add(firstMadTriangle);
            triangleSoup1.add(secondMadTriangle);

            legalizeEdge(triangleSoup1, firstMadTriangle, new MadLinha(noneEdgeVertex, edge.getA()), newVertex);
            legalizeEdge(triangleSoup1, secondMadTriangle, new MadLinha(noneEdgeVertex, edge.getB()), newVertex);
        }
    }

    private List<MadTriangle> triangulate(List<MadCell> all) {
        List<MadTriangle> triangleSoup = new ArrayList<>();
        float maxOfAnyCoordinate = 0.0f;
        List<MadPonto> pointSet = getPointSet(all);
        for (MadPonto vector : pointSet) {
            maxOfAnyCoordinate = Math.max(Math.max(vector.getX(), vector.getY()), maxOfAnyCoordinate);
        }

        maxOfAnyCoordinate *= 16.0D;
        MadPonto p1 = new MadPonto(0.0f, 3.0f * maxOfAnyCoordinate, null);
        MadPonto p2 = new MadPonto(3.0f * maxOfAnyCoordinate, 0.0f, null);
        MadPonto p3 = new MadPonto(-3.0f * maxOfAnyCoordinate, -3.0f * maxOfAnyCoordinate, null);
        MadTriangle superMadTriangle = new MadTriangle(p1, p2, p3);
        triangleSoup.add(superMadTriangle);
        for (int i = 0; i < pointSet.size(); i++) {
            MadPonto point = pointSet.get(i);
            MadTriangle triangle = triangleSoup.stream().filter(t6 -> t6.contains(point)).findFirst().orElse(null);

            if (triangle == null) {
                MadPonto point2 = pointSet.get(i);
                Optional<MadEdgeDistance> findFirst = triangleSoup.stream().map(t7 -> t7.findNearestEdge(point2))
                        .sorted().findFirst();
                if (!findFirst.isPresent()) {
                    continue;
                }
                MadLinha edge = findFirst.get().edge;

                MadTriangle first = triangleSoup.stream().filter(t4 -> t4.isNeighbour(edge)).findFirst()
                        .orElseThrow(() -> new Exception("There should be someone"));
                MadTriangle second = triangleSoup.stream().filter(t5 -> t5.isNeighbour(edge) && t5 != first).findFirst()
                        .orElseThrow(() -> new Exception("There should be someone"));

                MadPonto firstNoneEdgeVertex = first.getNoneEdgeVertex(edge);
                MadPonto secondNoneEdgeVertex = second.getNoneEdgeVertex(edge);

                triangleSoup.remove(first);
                triangleSoup.remove(second);

                MadTriangle triangle1 = new MadTriangle(edge.getA(), firstNoneEdgeVertex, pointSet.get(i));
                MadTriangle triangle2 = new MadTriangle(edge.getB(), firstNoneEdgeVertex, pointSet.get(i));
                MadTriangle triangle3 = new MadTriangle(edge.getA(), secondNoneEdgeVertex, pointSet.get(i));
                MadTriangle triangle4 = new MadTriangle(edge.getB(), secondNoneEdgeVertex, pointSet.get(i));

                triangleSoup.add(triangle1);
                triangleSoup.add(triangle2);
                triangleSoup.add(triangle3);
                triangleSoup.add(triangle4);

                legalizeEdge(triangleSoup, triangle1, new MadLinha(edge.getA(), firstNoneEdgeVertex), pointSet.get(i));
                legalizeEdge(triangleSoup, triangle2, new MadLinha(edge.getB(), firstNoneEdgeVertex), pointSet.get(i));
                legalizeEdge(triangleSoup, triangle3, new MadLinha(edge.getA(), secondNoneEdgeVertex), pointSet.get(i));
                legalizeEdge(triangleSoup, triangle4, new MadLinha(edge.getB(), secondNoneEdgeVertex), pointSet.get(i));
            } else {
                MadPonto a = triangle.getA();
                MadPonto b = triangle.getB();
                MadPonto c = triangle.getC();

                triangleSoup.remove(triangle);

                MadTriangle first = new MadTriangle(a, b, pointSet.get(i));
                MadTriangle second = new MadTriangle(b, c, pointSet.get(i));
                MadTriangle third = new MadTriangle(c, a, pointSet.get(i));

                triangleSoup.add(first);
                triangleSoup.add(second);
                triangleSoup.add(third);

                legalizeEdge(triangleSoup, first, new MadLinha(a, b), pointSet.get(i));
                legalizeEdge(triangleSoup, second, new MadLinha(b, c), pointSet.get(i));
                legalizeEdge(triangleSoup, third, new MadLinha(c, a), pointSet.get(i));
            }
        }

        triangleSoup.removeIf(t1 -> t1.hasVertex(superMadTriangle.getA()));
        triangleSoup.removeIf(t2 -> t2.hasVertex(superMadTriangle.getB()));
        triangleSoup.removeIf(t3 -> t3.hasVertex(superMadTriangle.getC()));

        for (MadTriangle t : triangleSoup) {
            MadCell cella = t.getA().getCell();
            MadCell cellb = t.getB().getCell();
            MadCell cellc = t.getC().getCell();
            allEdges.add(new MadEdge(cella, cellb));
            allEdges.add(new MadEdge(cellb, cellc));
            allEdges.add(new MadEdge(cellc, cella));
        }

        return triangleSoup;
    }
}