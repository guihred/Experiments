package graphs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.slf4j.Logger;
import utils.HasLogging;

public final class GraphAlgorithms {
    public static final Logger LOGGER = HasLogging.log();
	private GraphAlgorithms() {
	}

	public static void main(String[] args) {
        List<Vertex> vertices = createGraph8();

        vertices.forEach(v -> LOGGER.info("{}", v));

		Map<Vertex, Integer> num = new HashMap<>();
		Map<Vertex, Integer> low = new HashMap<>();

		vertices.get(0).assignNum(num, 0);
		vertices.get(0).assignLow(num, low);
        LOGGER.info("{}", num);
        LOGGER.info("{}", low);
        LOGGER.info("{}", Vertex.kruskal(vertices));
        LOGGER.info("{}", Vertex.prim(vertices));
		Vertex.sortTopology(vertices);

	}

	public static List<Vertex> createGraph1() {
		List<Vertex> vertices = IntStream.range(1, 8).mapToObj(Vertex::new).collect(Collectors.toList());
		int[][] adj = { { 2, 3, 4 }, { 4, 5 }, { 6 }, { 3, 6, 7 }, { 4, 7 }, {}, { 6 } };
		for (int i = 0; i < adj.length; i++) {
			vertices.get(i).put(IntStream.of(adj[i]).mapToObj(j -> vertices.get(j - 1)).toArray(Vertex[]::new));
		}
		return vertices;
	}

	public static List<Vertex> createGraph2() {
		List<Vertex> vertices = IntStream.range(1, 12).mapToObj(Vertex::new).collect(Collectors.toList());
		int[][] adj = { { 2, 5 }, // A
				{ 3 }, // B
				{ 11 }, // C
				{ 1, 5 },// D
				{ 3, 6, 9 },// E
				{ 3, 11 }, // F
				{ 4, 5, 8 },// G
				{ 5, 9 },// H
				{ 6, 11 },// I
				{ 1, 4, 7 },// S
				{},// T
		};
		for (int i = 0; i < adj.length; i++) {
			vertices.get(i).put(IntStream.of(adj[i]).mapToObj(j -> vertices.get(j - 1)).toArray(Vertex[]::new));
		}
		return vertices;
	}

	public static List<Vertex> createGraph3() {
		List<Vertex> graph = IntStream.range(1, 8).mapToObj(Vertex::new).collect(Collectors.toList());
		Vertex a = graph.get(0);
		Vertex b = graph.get(1);
		Vertex c = graph.get(2);
		Vertex d = graph.get(3);
		Vertex e = graph.get(4);
		Vertex f = graph.get(5);
		Vertex g = graph.get(6);
		a.put(b, 5).put(c, 3);// A
		b.put(c, 2).put(e, 3).put(g, 1);// B
		c.put(e, 7).put(d, 7);// C
		d.put(a, 2).put(f, 6);// D
		e.put(d, 2).put(f, 1);// E

		return graph;
	}

	public static List<Vertex> createGraph4() {
		String[] words = { "fine", "line", "mine", "nine", "pine", "vine", "wine", "wide", "wife", "wipe", "wire", "wind", "wing", "wink", "wins",
				"none", "gone", "note", "vote", "site", "bite" };

		AtomicInteger id = new AtomicInteger(0);
		List<Vertex> graph = Stream.of(words).map(a -> new Vertex(id.incrementAndGet(), a)).collect(Collectors.toList());
		for (Vertex v : graph) {
			for (Vertex w : graph) {
				if (oneCharOff(w.getName(), v.getName())) {
					w.put(v, 1);
					v.put(w, 1);
				}
			}
		}

		return graph;
	}

	public static List<Vertex> createGraph5() {
		List<Vertex> graph = IntStream.range(1, 7).mapToObj(Vertex::new).collect(Collectors.toList());
		Vertex a = graph.get(0);
		Vertex b = graph.get(1);
		Vertex c = graph.get(2);
		Vertex d = graph.get(3);
		Vertex e = graph.get(4);
		Vertex f = graph.get(5);
		a.put(b, 1).put(c, 2).put(d, 4);// A
		b.put(d, 2);// B
		c.put(f, 3);// C
		d.put(f, 3);// D
		e.put(a, 4).put(b, 2);// E

		return graph;
	}

	public static List<Vertex> createGraph6() {
		List<Vertex> graph = IntStream.range(1, 8).mapToObj(Vertex::new).collect(Collectors.toList());
		Vertex v1 = graph.get(0);
		Vertex v2 = graph.get(1);
		Vertex v3 = graph.get(2);
		Vertex v4 = graph.get(3);
		Vertex v5 = graph.get(4);
		Vertex v6 = graph.get(5);
		Vertex v7 = graph.get(6);
		v1.biput(v2, 2).biput(v4, 1).biput(v3, 4);// A
		v2.biput(v4, 3).biput(v5, 10);// B
		v3.biput(v4, 2).biput(v6, 5);// C
		v4.biput(v5, 7).biput(v7, 4).biput(v6, 8);// D
		v5.biput(v7, 6);// E
		v6.biput(v7, 1);// E

		return graph;
	}

	public static List<Vertex> createGraph7() {
		List<Vertex> graph = IntStream.range(1, 11).mapToObj(Vertex::new).collect(Collectors.toList());
		Vertex a = graph.get(0);
		Vertex b = graph.get(1);
		Vertex c = graph.get(2);
		Vertex d = graph.get(3);
		Vertex e = graph.get(4);
		Vertex f = graph.get(5);
		Vertex g = graph.get(6);
		Vertex h = graph.get(7);
		Vertex i = graph.get(8);
		Vertex j = graph.get(9);
		a.biput(b, 3).biput(e, 4).biput(d, 4);// A
		b.biput(e, 2).biput(f, 3).biput(c, 10);// B
		c.biput(f, 6).biput(g, 1);// C
		d.biput(e, 5).biput(h, 6);// D
		e.biput(f, 11).biput(i, 1).biput(h, 2);// E
		f.biput(g, 2).biput(i, 3).biput(j, 11);// F
		g.biput(j, 8);// G
		h.biput(i, 4);// H
		i.biput(j, 7);// I

		return graph;
	}

	public static List<Vertex> createGraph8() {
		List<Vertex> graph = IntStream.range(1, 12).mapToObj(Vertex::new).collect(Collectors.toList());
		Vertex a = graph.get(0);
		Vertex b = graph.get(1);
		Vertex c = graph.get(2);
		Vertex d = graph.get(3);
		Vertex e = graph.get(4);
        Vertex g = graph.get(6);
		Vertex f = graph.get(5);
		Vertex h = graph.get(7);
		Vertex i = graph.get(8);
		Vertex j = graph.get(9);
		Vertex k = graph.get(10);
		a.biput(c, d);
		b.biput(e, c);
		c.biput(f, d);
		e.biput(h, i, f);
		f.biput(g);
		h.biput(j);
		i.biput(k);
		j.biput(k);

		return graph;
	}

	// Returns true if word1 and word2 are the same length
	// and differ in only one character.
	public static boolean oneCharOff(String word1, String word2) {
		if (word1.length() != word2.length()) {
			return false;
		}

		int diffs = 0;

		for (int i = 0; i < word1.length(); i++) {
			if (word1.charAt(i) != word2.charAt(i) && ++diffs > 1) {
				return false;
			}
		}

		return diffs == 1;
	}

}
