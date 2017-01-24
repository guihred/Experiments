package javaexercises.graphs;

class EdgeDistancePack implements Comparable<EdgeDistancePack> {

	public Linha edge;
	public double distance;

	public EdgeDistancePack(Linha edge, double distance) {
		this.edge = edge;
		this.distance = distance;
	}

	@Override
	public int compareTo(EdgeDistancePack o) {
		return Double.compare(distance, o.distance);
	}

}