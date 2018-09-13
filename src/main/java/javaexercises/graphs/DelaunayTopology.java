package javaexercises.graphs;

import com.aspose.imaging.internal.Exceptions.Exception;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class DelaunayTopology extends BaseTopology {


	public DelaunayTopology(int size, Graph graph) {
		super(graph, "Delaunay", size);
	}

	public double distance(double x1, double x2, double y1, double y2) {
		double a = x1 - x2;
		double b = y1 - y2;
		return Math.sqrt(a * a + b * b);
	}

	@Override
	public void execute() {

		List<Cell> allCells = graph.getModel().getAllCells();
		size = allCells.size();
		graph.clean();
		graph.getModel().removeAllCells();
		Random rnd = new Random();
		int bound = 150;
		double x = 0;
		double y = 0;
		for (int i = 0; i < size; i++) {
			Cell cell = graph.getModel().addCell(BaseTopology.identifier(i), CellType.CIRCLE);
			double a = rnd.nextDouble() * 2 * Math.PI;
			x += bound * Math.cos(a);
			y += bound * Math.sin(a);
			cell.relocate(x, y);
		}

		triangulate(graph.getModel().getAddedCells());

		graph.endUpdate();
	}

	private List<Ponto> getPointSet(List<Cell> all) {

		return all.stream().map(c -> new Ponto(c.getLayoutX(), c.getLayoutY(), c))
				.collect(Collectors.toList());
	}

    private static void legalizeEdge(List<Triangle> triangleSoup1, Triangle triangle, Linha edge, Ponto newVertex) {
		Triangle neighbourTriangle = triangleSoup1.stream().filter(t -> t.isNeighbour(edge) && t != triangle).findFirst().orElse(null);
		if (neighbourTriangle != null && neighbourTriangle.isPointInCircumcircle(newVertex)) {
			triangleSoup1.remove(triangle);
			triangleSoup1.remove(neighbourTriangle);

			Ponto noneEdgeVertex = neighbourTriangle.getNoneEdgeVertex(edge);

			Triangle firstTriangle = new Triangle(noneEdgeVertex, edge.a, newVertex);
			Triangle secondTriangle = new Triangle(noneEdgeVertex, edge.b, newVertex);

			triangleSoup1.add(firstTriangle);
			triangleSoup1.add(secondTriangle);

			legalizeEdge(triangleSoup1, firstTriangle, new Linha(noneEdgeVertex, edge.a), newVertex);
			legalizeEdge(triangleSoup1, secondTriangle, new Linha(noneEdgeVertex, edge.b), newVertex);
		}
	}

	public List<Triangle> triangulate() {
		List<Triangle> triangulate = triangulate(graph.getModel().getAllCells());
		graph.sortChildren();
		return triangulate;
	}

	private List<Triangle> triangulate(List<Cell> all) {
		graph.getModel().removeAllEdges();
		List<Triangle>
		triangleSoup = new ArrayList<>();
		double maxOfAnyCoordinate = 0.0D;
		List<Ponto> pointSet = getPointSet(all);
		for (Ponto vector : pointSet) {
			maxOfAnyCoordinate = Math.max(Math.max(vector.x, vector.y), maxOfAnyCoordinate);
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
				Linha edge = triangleSoup.stream().map(t7 -> t7.findNearestEdge(point2)).sorted().findFirst().orElseThrow(()->new Exception("There should be someone")).edge;

                Triangle first = triangleSoup.stream().filter(t4 -> t4.isNeighbour(edge)).findFirst()
                        .orElseThrow(() -> new Exception("There should be some triangle"));
                Triangle second = triangleSoup.stream().filter(t5 -> t5.isNeighbour(edge) && t5 != first).findFirst()
                        .orElseThrow(() -> new Exception("There should be some triangle"));

				Ponto firstNoneEdgeVertex = first.getNoneEdgeVertex(edge);
				Ponto secondNoneEdgeVertex = second.getNoneEdgeVertex(edge);

				triangleSoup.remove(first);
				triangleSoup.remove(second);

				Triangle triangle1 = new Triangle(edge.a, firstNoneEdgeVertex, pointSet.get(i));
				Triangle triangle2 = new Triangle(edge.b, firstNoneEdgeVertex, pointSet.get(i));
				Triangle triangle3 = new Triangle(edge.a, secondNoneEdgeVertex, pointSet.get(i));
				Triangle triangle4 = new Triangle(edge.b, secondNoneEdgeVertex, pointSet.get(i));

				triangleSoup.add(triangle1);
				triangleSoup.add(triangle2);
				triangleSoup.add(triangle3);
				triangleSoup.add(triangle4);

				legalizeEdge(triangleSoup, triangle1, new Linha(edge.a, firstNoneEdgeVertex), pointSet.get(i));
				legalizeEdge(triangleSoup, triangle2, new Linha(edge.b, firstNoneEdgeVertex), pointSet.get(i));
				legalizeEdge(triangleSoup, triangle3, new Linha(edge.a, secondNoneEdgeVertex), pointSet.get(i));
				legalizeEdge(triangleSoup, triangle4, new Linha(edge.b, secondNoneEdgeVertex), pointSet.get(i));
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

		for (Triangle t : triangleSoup) {
			Cell cella = t.getA().getC();
			Cell cellb = t.getB().getC();
			Cell cellc = t.getC().getC();

			graph.getModel().addBiEdge(cella.getCellId(), cellb.getCellId(), (int) t.getA().sub(t.getB()).mag());
			graph.getModel().addBiEdge(cella.getCellId(), cellc.getCellId(), (int) t.getA().sub(t.getC()).mag());
			graph.getModel().addBiEdge(cellc.getCellId(), cellb.getCellId(), (int) t.getB().sub(t.getC()).mag());
		}
		graph.endUpdate();
		return triangleSoup;
	}
}