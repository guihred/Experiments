package javaexercises.graphs;

import java.util.Objects;

public abstract class BaseTopology {

	private static final char[] digits = { ' ', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N',
			'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z' };
	protected Graph graph;
	protected final String name;

	protected int size;

	public BaseTopology(Graph graph, String name, int size) {
		this.graph = graph;
		this.name = name;
		this.size = size;
	}

	public BaseTopology(String name) {
		this.name = name;
	}

	public abstract void execute();

	public String getName() {
		return name;
	}

	public static String cellIdentifier(int n) {
		int i = -n;
		/* Use the faster version */
		char[] buf = new char[33];
		int charPos = 32;
		while (i <= -26) {
			buf[charPos--] = digits[-(i % 26)];
			i = i / 26;
		}
		buf[charPos] = digits[-i];
		return new String(buf, charPos, 33 - charPos);
	}

	public static String identifier(int i) {
		if (i > 25) {
			return cellIdentifier(i + 1);
		}
		return Objects.toString((char) ('A' + i));

	}
}