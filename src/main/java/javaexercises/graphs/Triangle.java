package javaexercises.graphs;

import java.util.Arrays;
import java.util.stream.Stream;

class Triangle {
	private Ponto a;
	private Ponto b;
	private Ponto c;

	public Triangle(Ponto a, Ponto b, Ponto c) {
		this.a = a;
		this.b = b;
		this.c = c;
	}

	public Stream<Ponto> allPoints() {
		return Stream.of(a, b, c);
	}

	@Override
	public String toString() {
		return Arrays.toString(new Ponto[] { a, b, c });
	}

	private static Ponto computeClosestPoint(Linha edge, Ponto point) {
		Ponto ab = edge.b.sub(edge.a);
		double t = point.sub(edge.a).dot(ab) / ab.dot(ab);

		if (t < 0.0D) {
			t = 0.0D;
		} else if (t > 1.0D) {
			t = 1.0D;
		}

		return edge.a.add(ab.mult(t));
	}

	public boolean contains(Ponto point) {
		double pab = point.sub(a).cross(b.sub(a));
		double pbc = point.sub(b).cross(c.sub(b));
		if (!hasSameSign(pab, pbc)) {
			return false;
		}
		double pca = point.sub(c).cross(a.sub(c));
		return hasSameSign(pab, pca);
	}

	public EdgeDistancePack findNearestEdge(Ponto point) {
		EdgeDistancePack[] edges = new EdgeDistancePack[3];

		edges[0] = new EdgeDistancePack(new Linha(a, b), computeClosestPoint(new Linha(a, b), point).sub(point).mag());
		edges[1] = new EdgeDistancePack(new Linha(b, c), computeClosestPoint(new Linha(b, c), point).sub(point).mag());
		edges[2] = new EdgeDistancePack(new Linha(c, a), computeClosestPoint(new Linha(c, a), point).sub(point).mag());


		Arrays.sort(edges);
		return edges[0];
	}

	public Ponto getNoneEdgeVertex(Linha edge) {
		if (a != edge.a && a != edge.b) {
			return a;
		} else if (b != edge.a && b != edge.b) {
			return b;
		} else if (c != edge.a && c != edge.b) {
			return c;
		}

		return null;
	}

	private static boolean hasSameSign(double a1, double b1) {
		return Math.signum(a1) == Math.signum(b1);
	}

	public boolean hasVertex(Ponto vertex) {
		return a == vertex || b == vertex || c == vertex;
	}

	public boolean isNeighbour(Linha edge) {
		return (a == edge.a || b == edge.a || c == edge.a) && (a == edge.b || b == edge.b || c == edge.b);
	}

	public boolean isOrientedCCW() {
		double a11 = a.x - c.x;
		double a21 = b.x - c.x;

		double a12 = a.y - c.y;
		double a22 = b.y - c.y;

		double det = a11 * a22 - a12 * a21;

		return det > 0.0D;
	}

	public boolean isPointInCircumcircle(Ponto point) {
		double a11 = a.x - point.x;
		double a21 = b.x - point.x;
		double a31 = c.x - point.x;

		double a12 = a.y - point.y;
		double a22 = b.y - point.y;
		double a32 = c.y - point.y;

		double a13 = (a.x - point.x) * (a.x - point.x) + (a.y - point.y) * (a.y - point.y);
		double a23 = (b.x - point.x) * (b.x - point.x) + (b.y - point.y) * (b.y - point.y);
		double a33 = (c.x - point.x) * (c.x - point.x) + (c.y - point.y) * (c.y - point.y);

		double det = a11 * a22 * a33 + a12 * a23 * a31 + a13 * a21 * a32 - a13 * a22 * a31 - a12 * a21 * a33 - a11 * a23 * a32;

		if (isOrientedCCW()) {
			return det > 0.0D;
		}

		return det < 0.0D;
	}

	public Ponto getA() {
		return a;
	}

	public void setA(Ponto a) {
		this.a = a;
	}

	public Ponto getB() {
		return b;
	}

	public void setB(Ponto b) {
		this.b = b;
	}

	public Ponto getC() {
		return c;
	}

	public void setC(Ponto c) {
		this.c = c;
	}

}