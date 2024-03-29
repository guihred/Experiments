package graphs;

import java.util.Objects;

public class EdgeElement implements Comparable<EdgeElement> {
	private Vertex u;
	private Vertex v;
	private Integer weight;

	public EdgeElement() {
	}

	public EdgeElement(Vertex u, Vertex v, Integer weight) {
		this.u = u;
		this.v = v;
		this.weight = weight;
	}

	@Override
	public int compareTo(EdgeElement o) {
		return Integer.compare(weight, o.weight);
	}

	@Override
	public boolean equals(Object obj) {
        if (obj == null || !getClass().isInstance(obj)) {
            return false;
        }
		EdgeElement other = (EdgeElement) obj;
		if (Objects.equals(u, other.u) && Objects.equals(v, other.v)) {
			return true;
		}
        return Objects.equals(v, other.u) && Objects.equals(u, other.v);
	}

	public Vertex getU() {
		return u;
	}

	public Vertex getV() {
		return v;
	}

	public Integer getWeight() {
		return weight;
	}

	@Override
	public int hashCode() {
		return Objects.hash(u, v);
	}

	public void setU(Vertex u) {
		this.u = u;
	}

	public void setV(Vertex v) {
		this.v = v;
	}

	public void setWeight(Integer weight) {
		this.weight = weight;
	}

	@Override
	public String toString() {
        return u + "-" + v + "(" + weight + ")";
	}

}