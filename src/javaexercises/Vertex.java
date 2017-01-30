package javaexercises;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

class Vertex {
	public static final boolean NAMED = true;

	public static void chain(String nome1, String nome2, List<Vertex> vertices) {
		Vertex v1 = vertices.stream().filter(v -> v.name.equals(nome1)).findFirst().get();
		Vertex v2 = vertices.stream().filter(v -> v.name.equals(nome2)).findFirst().get();
		for (Vertex v : vertices) {
			v.dijkstra(vertices);
		}

		Map<Vertex, Integer> dijkstra = v1.dijkstra(vertices);
		Set<Vertex> chain = new LinkedHashSet<>();
		Vertex path = v2;
		chain.add(path);
		Integer integer = dijkstra.get(v2);
		if (integer != null && integer < Integer.MAX_VALUE) {
			while (path != v1) {
				path = path.pathTo(v1, vertices);
				chain.add(path);
			}
		}
		System.out.println(chain.stream().map(v -> v.getName()).sequential().collect(Collectors.joining("->")));
	}

	public static void sortTopology(List<Vertex> vertices) {
		int counter = 0;

		Queue<Vertex> q = new LinkedList<>();
		for (Vertex v : vertices) {
			for (Vertex w : v.adjacents()) {
				w.indegree++;
			}

			if (v.indegree == 0) {
				q.add(v);
			}
		}
		while (!q.isEmpty()) {
			Vertex v = q.poll();
			v.topNum = ++counter;

			for (Vertex w : v.adjacents()) {
				if (--w.indegree == 0) {
					q.add(w);
				}
			}

		}
		vertices.forEach(v -> System.out.println(v.id + "=" + v.topNum));

		if (counter != vertices.size()) {
			System.out.println("CYCLE FOUND");
		}
	}

	static List<Edge> kruskal(List<Vertex> totalVertices) {

		int numVertices = totalVertices.size();
		List<Edge> totalEdges = totalVertices.stream()
				.flatMap((Vertex v) -> v.edges.entrySet().stream().map((Entry<Vertex, Integer> e) -> new Edge(v, e.getKey(), e.getValue())))
				.collect(Collectors.toList());
		DisjSets ds = new DisjSets(numVertices);
		PriorityQueue<Edge> pq = new PriorityQueue<>(totalEdges);
		List<Edge> mst = new ArrayList<>();
		while (mst.size() != numVertices - 1) {
			Edge e1 = pq.poll();
			int uset = ds.find(e1.u.id - 1);
			int vset = ds.find(e1.v.id - 1);
			if (uset != vset) {
				mst.add(e1);
				ds.union(uset, vset);
			}
		}
		return mst;
	}

	public static List<Edge> prim(List<Vertex> vertices) {
		Map<Vertex, Integer> heap = new HashMap<>();
		Map<Vertex, Vertex> mstHolder = new HashMap<>();
		for (Vertex v : vertices) {
			heap.put(v, Integer.MAX_VALUE);
		}

		while (!heap.isEmpty()) {
			Entry<Vertex, Integer> minVertex = heap.entrySet().stream().min(Comparator.comparing(Entry<Vertex, Integer>::getValue)).get();
			heap.remove(minVertex.getKey());
			Set<Entry<Vertex, Integer>> entrySet = minVertex.getKey().edges.entrySet();
			for (Entry<Vertex, Integer> edge : entrySet) {
				if (heap.containsKey(edge.getKey()) && heap.get(edge.getKey()) > edge.getValue()) {
					heap.put(edge.getKey(), edge.getValue());
					mstHolder.put(edge.getKey(), minVertex.getKey());
				}
			}

		}
		return mstHolder.entrySet().stream().map(e -> new Edge(e.getValue(), e.getKey(), e.getValue().weight(e.getKey())))
				.collect(Collectors.toList());

	}

	public void assignNum(Map<Vertex, Integer> num, int c) {
		int counter = c;

		num.put(this, counter++);
		Set<Vertex> adjacents = adjacents();
		for (Vertex w : adjacents) {
			if (!num.containsKey(w)) {
				w.parent = this;
				w.assignNum(num, counter);

			}

		}
	}

	void assignLow(Map<Vertex, Integer> num, Map<Vertex, Integer> low) {
		Vertex v = this;
		low.put(v, num.get(v));
		Set<Vertex> adjacents = adjacents();
		for (Vertex w : adjacents) {
			if (num.get(w) > num.get(v)) {
				w.assignLow(num, low);
				if (low.get(w) >= num.get(v)) {
					System.out.println(v.getName() + " is an articulation point");
				}
				low.put(v, Integer.min(low.get(v), low.get(w)));

			} else if (v.parent != w) {
				low.put(v, Integer.min(low.get(v), num.get(w)));
			}

		}
	}

	final int id;
	public int topNum, indegree;

	private Map<Vertex, Integer> edges = new HashMap<>();

	Map<Integer, Vertex> path = new HashMap<>();
	Vertex parent;

	String name;

	public Vertex(int id) {
		this.id = id;
		name = "" + (char) ('A' + id - 1);
	}

	public Vertex(int id, String name) {
		this.id = id;
		this.name = name;
	}

	public Set<Vertex> adjacents() {
		return edges.keySet();
	}

	public Vertex biput(Vertex v, int weight) {
		edges.put(v, weight);
		v.edges.put(this, weight);
		return this;
	}

	public void biput(Vertex... vertices) {
		for (int i = 0; i < vertices.length; i++) {
			edges.put(vertices[i], 1);
			vertices[i].edges.put(this, 1);
		}
	}

	public Map<Vertex, Integer> dijkstra(List<Vertex> graph) {
		Map<Vertex, Integer> distance = new HashMap<>();
		Map<Vertex, Boolean> known = new HashMap<>();
		for (Vertex v : graph) {
			distance.put(v, Integer.MAX_VALUE);
			known.put(v, false);
		}
		distance.put(this, 0);
		while (known.entrySet().stream().anyMatch((e) -> !e.getValue())) {
			Vertex v = distance.entrySet().stream().filter(e -> !known.get(e.getKey())).min(Comparator.comparing(Entry<Vertex, Integer>::getValue))
					.orElse(null).getKey();
			known.put(v, true);
			for (Vertex w : v.adjacents()) {
				if (!known.get(w)) {
					Integer cvw = v.edges.get(w);
					if (distance.get(v) + cvw < distance.get(w)) {
						distance.put(w, distance.get(v) + cvw);
						w.path.put(id, v);
					}
				}
			}
		}
		return distance;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof Vertex)) {
			return false;
		}
		Vertex other = (Vertex) obj;
		return id == other.id;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	public Vertex pathTo(Vertex v, List<Vertex> graph) {
		if (path.isEmpty()) {
			v.dijkstra(graph);
			dijkstra(graph);
		}
		return path.get(v.id);
	}

	public void put(Vertex... vertices) {
		for (int i = 0; i < vertices.length; i++) {
			edges.put(vertices[i], 1);
		}
	}

	public Vertex put(Vertex v, int weight) {
		edges.put(v, weight);
		return this;
	}

	@Override
	public String toString() {

		StringBuilder sb = new StringBuilder(getName());
		sb.append("->");
		edges.forEach((v, w) -> sb.append(v.getName() + " "));

		return sb.toString();
	}

	public String getName() {
		if (!NAMED) {
			return Integer.toString(id);
		}

		return name;
	}

	public Map<Vertex, Integer> unweighted(List<Vertex> graph) {
		Map<Vertex, Integer> distance = new HashMap<>();
		Map<Vertex, Boolean> known = new HashMap<>();
		for (Vertex v : graph) {
			distance.put(v, Integer.MAX_VALUE);
			known.put(v, false);
		}
		distance.put(this, 0);
		for (int i = 0; i < graph.size(); i++) {
			for (Vertex v : graph) {
				if (!known.get(v) && distance.get(v) == i) {
					known.put(v, true);
					for (Vertex w : v.adjacents()) {
						if (distance.get(w) == Integer.MAX_VALUE) {
							distance.put(w, i + 1);
							w.path.put(id, v);
						}
					}

				}
			}
		}
		return distance;

	}

	public Integer weight(Vertex v) {
		return edges.get(v);
	}

	public Map<Vertex, Integer> weightedNegative(List<Vertex> graph) {
		Map<Vertex, Integer> distance = new HashMap<>();
		Queue<Vertex> q = new LinkedList<>();

		for (Vertex v : graph) {
			distance.put(v, Integer.MAX_VALUE);
		}
		distance.put(this, 0);
		q.add(this);
		while (!q.isEmpty()) {
			Vertex v = q.poll();

			for (Vertex w : v.adjacents()) {
				Integer cvw = v.edges.get(w);
				if (distance.get(v) + cvw < distance.get(w)) {
					distance.put(w, distance.get(v) + cvw);
					w.path.put(id, v);
					if (!q.contains(w)) {
						q.add(w);
					}
				}

			}
		}
		return distance;
	}
}