package javaexercises.graphs;

public abstract class GenTopology {

	protected final String name;
	protected int size;
	protected Graph graph;

	public GenTopology(String name) {
		this.name = name;
	}

	public GenTopology(Graph graph, String name, int size) {
		this.graph = graph;
		this.name = name;
		this.size = size;
	}

	public abstract void execute();

	public String getName() {
		return name;
	}

}