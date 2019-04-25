package graphs;

import com.aspose.imaging.internal.Exceptions.Exception;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import utils.DisjSets;
import utils.HasLogging;

public class Vertex implements HasLogging {
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
                    getLogger().info("{} is an articulation point", v.getName());
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
                        w.path.put(id, v);
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
        return id == other.id;
    }

    public String getName() {
        if (!NAMED) {
            return Integer.toString(id);
        }

        return name;
    }

    public int getTopNum() {
        return topNum;
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

    public Integer weight(Vertex v) {
        return edges.get(v);
    }

    private Vertex getMinDistanceVertex(Map<Vertex, Integer> distance, Map<Vertex, Boolean> known) {
        return distance.entrySet().stream().filter(e -> !known.get(e.getKey()))
            .min(Comparator.comparing(Entry<Vertex, Integer>::getValue))
            .orElseThrow(() -> new Exception("There should be something")).getKey();
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

    public static List<Edge> kruskal(Collection<Vertex> totalVertices) {

        int numVertices = totalVertices.size();
        List<Edge> totalEdges = totalVertices.stream().flatMap((Vertex v) -> v.edges.entrySet().stream()
            .map((Entry<Vertex, Integer> e) -> new Edge(v, e.getKey(), e.getValue()))).collect(Collectors.toList());
        DisjSets ds = new DisjSets(numVertices);
        PriorityQueue<Edge> pq = new PriorityQueue<>(totalEdges);
        List<Edge> mst = new ArrayList<>();
        while (mst.size() != numVertices - 1) {
            Edge e1 = pq.poll();
            int uset = ds.find(e1.getU().id - 1);
            int vset = ds.find(e1.getV().id - 1);
            if (uset != vset) {
                mst.add(e1);
                ds.union(uset, vset);
            }
        }
        return mst;
    }

    public static List<Edge> prim(Iterable<Vertex> vertices) {
        Map<Vertex, Integer> heap = new HashMap<>();
        Map<Vertex, Vertex> mstHolder = new HashMap<>();
        for (Vertex v : vertices) {
            heap.put(v, Integer.MAX_VALUE);
        }

        while (!heap.isEmpty()) {
            Entry<Vertex, Integer> minVertex = heap.entrySet().stream()
                .min(Comparator.comparing(Entry<Vertex, Integer>::getValue))
                .orElseThrow(() -> new Exception("There should be someone"));
            heap.remove(minVertex.getKey());
            Set<Entry<Vertex, Integer>> entrySet = minVertex.getKey().edges.entrySet();
            for (Entry<Vertex, Integer> edge : entrySet) {
                if (heap.containsKey(edge.getKey()) && heap.get(edge.getKey()) > edge.getValue()) {
                    heap.put(edge.getKey(), edge.getValue());
                    mstHolder.put(edge.getKey(), minVertex.getKey());
                }
            }

        }
        return mstHolder.entrySet().stream()
            .map(e -> new Edge(e.getValue(), e.getKey(), e.getValue().weight(e.getKey()))).collect(Collectors.toList());

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
        vertices.forEach(v -> LOG.info("{}={}", v.id, v.topNum));

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
                        w.path.put(vertex.id, v);
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
                    w.path.put(vertex.id, v);
                    if (!q.contains(w)) {
                        q.add(w);
                    }
                }

            }
        }
        return distance;
    }
}