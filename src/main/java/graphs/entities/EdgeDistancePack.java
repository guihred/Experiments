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

    public void setEdge(Linha edge) {
        this.edge = edge;
    }
}