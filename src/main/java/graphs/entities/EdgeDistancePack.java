package graphs.entities;

import java.util.Objects;

public class EdgeDistancePack implements Comparable<EdgeDistancePack> {

	private Linha edge;
    private double distance;

	public EdgeDistancePack(Linha edge, double distance) {
		this.edge = edge;
		this.distance = distance;
	}

	@Override
	public int compareTo(EdgeDistancePack o) {
		return Double.compare(distance, o.distance);
	}

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!getClass().isInstance(obj)) {
            return false;
        }
        EdgeDistancePack other = (EdgeDistancePack) obj;
        return Double.doubleToLongBits(distance) == Double.doubleToLongBits(other.distance)
            && Objects.equals(edge, other.edge);
    }

    public Linha getEdge() {
        return edge;
    }

    @Override
    public int hashCode() {
        return Objects.hash(distance, edge);
    }

    public void setEdge(Linha edge) {
        this.edge = edge;
    }

    public static double distance(double x1, double x2, double y1, double y2) {
        double a = x1 - x2;
        double b = y1 - y2;
        return Math.sqrt(a * a + b * b);
    }
}