package javaexercises.graphs;

import java.util.Objects;
import java.util.Random;

public class RandomTopology extends GenTopology {

	public RandomTopology(int size, Graph graph) {
		super(graph, "Random", size);
	}

	@Override
	public void execute() {
		graph.clean();
		graph.getModel().removeAllCells();
		graph.getModel().removeAllEdges();
		Random random = new Random();
		for (int i = 0; i < size; i++) {
			Cell addCell = graph.getModel().addCell(identifier(i), CellType.CIRCLE);
			addCell.relocate(random.nextGaussian() * 500, random.nextGaussian() * 500);
		}
		int nextInt = random.nextInt(size * (size - 1) / 2) + 1;
		for (int i = 0; i < nextInt; i++) {
			String sourceId;
			String targetId;
			Integer valor = random.nextInt(size) + 1;
			do {
				sourceId = identifier(random.nextInt(size));
				targetId = identifier(random.nextInt(size));
				if (!sourceId.equals(targetId) && graph.getModel().addedCost(sourceId, targetId) == null) {
					Integer cost = graph.getModel().addedCost(targetId, sourceId);
					if (cost != null) {
						valor = cost;
					}

					graph.getModel().addEdge(sourceId, targetId, valor);
					break;
				}
			} while (graph.getModel().addedCost(sourceId, targetId) != null || sourceId.equals(targetId));
		}
		graph.endUpdate();
	}

	public static String identifier(int i) {
		if (i > 25) {
			return cellIdentifier(i + 1);
		}
		return Objects.toString((char) ('A' + i));

	}

	final static char[] digits = { ' ', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V',
			'W',
			'X', 'Y', 'Z' };

	public static String cellIdentifier(int n) {
		int i = -n;
		/* Use the faster version */
		char buf[] = new char[33];
		int charPos = 32;
		while (i <= -26) {
			buf[charPos--] = digits[-(i % 26)];
			i = i / 26;
		}
		buf[charPos] = digits[-i];
		return new String(buf, charPos, 33 - charPos);
	}

}