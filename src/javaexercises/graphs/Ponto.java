package javaexercises.graphs;

import java.util.Objects;

class Ponto {
	public double x;
	public double y;
	private Cell c;


	public Ponto(double x, double y, Cell c) {
		this.x = x;
		this.y = y;
		this.c = c;
	}

	public Ponto add(Ponto vector) {
		return new Ponto(x + vector.x, y + vector.y, c);
	}

	public double cross(Ponto vector) {
		return y * vector.x - x * vector.y;
	}
	public double dot(Ponto vector) {
		return x * vector.x + y * vector.y;
	}

	@Override
	public boolean equals(Object obj) {

		return super.equals(obj);
	}

	public Cell getC() {
		return c;
	}

	@Override
	public int hashCode() {
		return Objects.hash(x, y);
	}

	public double mag() {
		return Math.sqrt(x * x + y * y);
	}
	public Ponto mult(double scalar) {
		return new Ponto(x * scalar, y * scalar, c);
	}

	public Ponto sub(Ponto vector) {
		return new Ponto(x - vector.x, y - vector.y, c);
	}

	@Override
	public String toString() {
		return "(" + x + "," + y + ")";
	}

}