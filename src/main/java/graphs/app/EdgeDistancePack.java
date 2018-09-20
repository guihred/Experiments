package graphs.app;

public class EdgeDistancePack implements Comparable<EdgeDistancePack> {

	protected Linha edge;
	protected double distance;

	public EdgeDistancePack(Linha edge, double distance) {
		this.edge = edge;
		this.distance = distance;
	}

	@Override
	public int compareTo(EdgeDistancePack o) {
		return Double.compare(distance, o.distance);
	}
}