package graphs.entities;

public class EdgeDistancePack implements Comparable<EdgeDistancePack> {

	private Linha edge;
	protected double distance;

	public EdgeDistancePack(Linha edge, double distance) {
		this.edge = edge;
		this.distance = distance;
	}

	@Override
	public int compareTo(EdgeDistancePack o) {
		return Double.compare(distance, o.distance);
	}

    public Linha getEdge() {
        return edge;
    }

    public static double distance(double x1, double x2, double y1, double y2) {
        double a = x1 - x2;
        double b = y1 - y2;
        return Math.sqrt(a * a + b * b);
    }

    public void setEdge(Linha edge) {
        this.edge = edge;
    }
}