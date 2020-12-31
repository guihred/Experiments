package graphs;

import com.aspose.imaging.internal.Exceptions.Exception;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import utils.ex.HasLogging;

public class Vertex {
    private static final Logger LOG = HasLogging.log();

    public static final boolean NAMED = true;

    private Map<Vertex, Integer> edges = new HashMap<>();

    private final int id;

    private String name;

    private Vertex parent;

    private Map<Integer, Vertex> path = new HashMap<>();

    private int topNum;
    private int indegree;

    public Vertex(int id) {
        this.id = id;
        name = Character.toString((char) ('A' + id - 1));
    }

    public Vertex(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public Set<Vertex> adjacents() {
        return edges.keySet();
    }

    public void assignLow(Map<Vertex, Integer> num, Map<Vertex, Integer> low) {
        Vertex v = this;
        low.put(v, num.get(v));
        Set<Vertex> adjacents = adjacents();
        for (Vertex w : adjacents) {
            if (num.get(w) > num.get(v)) {
                w.assignLow(num, low);
                if (low.get(w) >= num.get(v)) {
                    LOG.info("{} is an articulation point", v.getName());
                }
                low.put(v, Integer.min(low.get(v), low.get(w)));

            } else if (!Objects.equals(v.parent, w)) {
                low.put(v, Integer.min(low.get(v), num.get(w)));
            }

        }
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

    public void biput(Vertex... vertices) {
        for (int i = 0; i < vertices.length; i++) {
            edges.put(vertices[i], 1);
            vertices[i].edges.put(this, 1);
        }
    }

    public Vertex biput(Vertex v, int weight) {
        edges.put(v, weight);
        v.edges.put(this, weight);
        return this;
    }

    public Map<Vertex, Integer> dijkstra(Iterable<Vertex> graph) {
        Map<Vertex, Integer> distance = new HashMap<>();
        Map<Vertex, Boolean> known = new HashMap<>();
        for (Vertex v : graph) {
            distance.put(v, Integer.MAX_VALUE);
            known.put(v, false);
        }
        distance.put(this, 0);
        while (known.entrySet().stream().anyMatch(e -> !e.getValue())) {
            Vertex v = getMinDistanceVertex(distance, known);
            known.put(v, true);
            for (Vertex w : v.adjacents()) {
                if (!known.get(w)) {
                    Integer cvw = v.edges.get(w);
                    if (distance.get(v) + cvw < distance.get(w)) {
                        distance.put(w, distance.get(v) + cvw);
                        w.path.put(getId(), v);
                    }
                }
            }
        }
        return distance;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !getClass().isInstance(obj)) {
            return false;
        }
        Vertex other = (Vertex) obj;
        return getId() == other.getId();
    }

    public Map<Vertex, Integer> getEdges() {
		return edges;
	}

    public int getId() {
		return id;
	}

    public String getName() {
        if (!NAMED) {
            return ""+getId();
        }

        return name;
    }

    public int getTopNum() {
        return topNum;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    public Vertex pathTo(Vertex v, List<Vertex> graph) {
        if (path.isEmpty()) {
            v.dijkstra(graph);
            dijkstra(graph);
        }
        return path.get(v.getId());
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

    public void setEdges(Map<Vertex, Integer> edges) {
		this.edges = edges;
	}

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder(getName());
        sb.append("->");
        edges.forEach((v, w) -> sb.append(v.getName() + " "));

        return sb.toString();
    }

    public Integer weight(Vertex v) {
        return edges.get(v);
    }

    public static void chain(String nome1, String nome2, List<Vertex> vertices) {
        Vertex v1 = vertices.stream().filter(v -> v.name.equals(nome1)).findFirst()
            .orElseThrow(() -> new Exception("There should be some vertex called " + nome1));
        Vertex v2 = vertices.stream().filter(v -> v.name.equals(nome2)).findFirst()
            .orElseThrow(() -> new Exception("There should be someone"));
        for (Vertex v : vertices) {
            v.dijkstra(vertices);
        }

        Map<Vertex, Integer> dijkstra = v1.dijkstra(vertices);
        Set<Vertex> chain = new LinkedHashSet<>();
        Vertex path = v2;
        chain.add(path);
        Integer integer = dijkstra.get(v2);
        if (integer != null && integer < Integer.MAX_VALUE) {
            while (!Objects.equals(path, v1)) {
                path = path.pathTo(v1, vertices);
                chain.add(path);
            }
        }

        String chainString = chain.stream().map(Vertex::getName).sequential().collect(Collectors.joining("->"));
        LOG.info(chainString);
    }

    public static void sortTopology(Collection<Vertex> vertices) {
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
        vertices.forEach(v -> LOG.info("{}={}", v.getId(), v.topNum));

        if (counter != vertices.size()) {
            LOG.info("CYCLE FOUND");
        }
    }

    public static Map<Vertex, Integer> unweighted(List<Vertex> graph) {
        return unweighted(graph, graph.get(0));
    }

    public static Map<Vertex, Integer> unweighted(List<Vertex> graph, Vertex vertex) {
        Map<Vertex, Integer> distance = new HashMap<>();
        Map<Vertex, Boolean> known = new HashMap<>();
        for (Vertex v : graph) {
            distance.put(v, Integer.MAX_VALUE);
            known.put(v, false);
        }
        distance.put(vertex, 0);
        for (int i = 0; i < graph.size(); i++) {
            int j = i;
            for (Vertex v : graph.stream().filter(q -> !known.get(q) && distance.get(q) == j)
                .collect(Collectors.toList())) {
                known.put(v, true);
                for (Vertex w : v.adjacents()) {
                    if (distance.get(w) == Integer.MAX_VALUE) {
                        distance.put(w, i + 1);
                        w.path.put(vertex.getId(), v);
                    }

                }
            }
        }
        return distance;
    }

	public static Map<Vertex, Integer> weightedNegative(List<Vertex> graph) {
        return weightedNegative(graph, graph.get(0));
    }

	public static Map<Vertex, Integer> weightedNegative(List<Vertex> graph, Vertex vertex) {
        Map<Vertex, Integer> distance = new HashMap<>();
        Queue<Vertex> q = new LinkedList<>();
        for (Vertex v : graph) {
            distance.put(v, Integer.MAX_VALUE);
        }
        distance.put(vertex, 0);
        q.add(vertex);
        while (!q.isEmpty()) {
            Vertex v = q.poll();

            for (Vertex w : v.adjacents()) {
                Integer cvw = v.edges.get(w);
                if (distance.get(v) + cvw < distance.get(w)) {
                    distance.put(w, distance.get(v) + cvw);
                    w.path.put(vertex.getId(), v);
                    if (!q.contains(w)) {
                        q.add(w);
                    }
                }

            }
        }
        return distance;
    }

	private static Vertex getMinDistanceVertex(Map<Vertex, Integer> distance, Map<Vertex, Boolean> known) {
        return distance.entrySet().stream().filter(e -> !known.get(e.getKey()))
            .min(Comparator.comparing(Entry<Vertex, Integer>::getValue))
            .orElseThrow(() -> new Exception("There should be something")).getKey();
    }
}