package graphs.entities;

import java.util.Arrays;
import java.util.stream.Stream;

public class Triangle {
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

	public Ponto getA() {
		return a;
	}

	public Ponto getB() {
		return b;
	}

	public Ponto getC() {
		return c;
	}

	public Ponto getNoneEdgeVertex(Linha edge) {
		if (a != edge.getA() && a != edge.getB()) {
			return a;
		} else if (b != edge.getA() && b != edge.getB()) {
			return b;
		} else if (c != edge.getA() && c != edge.getB()) {
			return c;
		}

		return null;
	}

	public boolean hasVertex(Ponto vertex) {
		return a == vertex || b == vertex || c == vertex;
	}

	public boolean isNeighbour(Linha edge) {
        return Stream.of(a, b, c).anyMatch(s -> s == edge.getA()) && Stream.of(a, b, c).anyMatch(s -> s == edge.getB());
	}

	public boolean isOrientedCCW() {
		double a11 = a.getX() - c.getX();
		double a21 = b.getX() - c.getX();

		double a12 = a.getY() - c.getY();
		double a22 = b.getY() - c.getY();

		double det = a11 * a22 - a12 * a21;

		return det > 0.0D;
	}

	public boolean isPointInCircumcircle(Ponto point) {
		double a11 = a.getX() - point.getX();
		double a21 = b.getX() - point.getX();
		double a31 = c.getX() - point.getX();

		double a12 = a.getY() - point.getY();
		double a22 = b.getY() - point.getY();
		double a32 = c.getY() - point.getY();

		double a13 = (a.getX() - point.getX()) * (a.getX() - point.getX()) + (a.getY() - point.getY()) * (a.getY() - point.getY());
		double a23 = (b.getX() - point.getX()) * (b.getX() - point.getX()) + (b.getY() - point.getY()) * (b.getY() - point.getY());
		double a33 = (c.getX() - point.getX()) * (c.getX() - point.getX()) + (c.getY() - point.getY()) * (c.getY() - point.getY());

		double det = a11 * a22 * a33 + a12 * a23 * a31 + a13 * a21 * a32 - a13 * a22 * a31 - a12 * a21 * a33 - a11 * a23 * a32;

		if (isOrientedCCW()) {
			return det > 0.0D;
		}

		return det < 0.0D;
	}

	public void setA(Ponto a) {
		this.a = a;
	}

	public void setB(Ponto b) {
		this.b = b;
	}

	public void setC(Ponto c) {
		this.c = c;
	}

	@Override
	public String toString() {
		return Arrays.toString(new Ponto[] { a, b, c });
	}

	private static Ponto computeClosestPoint(Linha edge, Ponto point) {
		Ponto ab = edge.getB().sub(edge.getA());
		double t = point.sub(edge.getA()).dot(ab) / ab.dot(ab);

		if (t < 0.0D) {
			t = 0.0D;
		} else if (t > 1.0D) {
			t = 1.0D;
		}

		return edge.getA().add(ab.mult(t));
	}

	private static boolean hasSameSign(double a1, double b1) {
		return Math.signum(a1) == Math.signum(b1);
	}

}