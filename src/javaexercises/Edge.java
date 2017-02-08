package javaexercises;

import java.util.Objects;

class Edge implements Comparable<Edge> {
	private Vertex u;
	private Vertex v;
	private Integer weight;

	public Edge() {
	}

	public Edge(Vertex u, Vertex v, Integer weight) {
		this.u = u;
		this.v = v;
		this.weight = weight;
	}

	@Override
	public int compareTo(Edge o) {
		return Integer.compare(weight, o.weight);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof Edge)) {
			return false;
		}
		Edge other = (Edge) obj;
		if (Objects.equals(u, other.u) && Objects.equals(v, other.v)) {
			return true;
		}
		if (Objects.equals(v, other.u) && Objects.equals(u, other.v)) {
			return true;
		}
		return true;
	}

	@Override
	public int hashCode() {
		return Objects.hash(u, v);
	}

	@Override
	public String toString() {
		return u.getName() + "-" + v.getName() + "(" + weight + ")";
	}

	public Vertex getU() {
		return u;
	}

	public void setU(Vertex u) {
		this.u = u;
	}

	public Vertex getV() {
		return v;
	}

	public void setV(Vertex v) {
		this.v = v;
	}

	public Integer getWeight() {
		return weight;
	}

	public void setWeight(Integer weight) {
		this.weight = weight;
	}

}