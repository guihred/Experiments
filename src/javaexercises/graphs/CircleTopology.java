package javaexercises.graphs;

import java.util.Random;

public class CircleTopology extends GenTopology {


	public CircleTopology(int size, Graph graph) {
		super(graph, "Circle", size);
	}

	@Override
	public void execute() {
		graph.clean();
		graph.getModel().removeAllCells();
		graph.getModel().removeAllEdges();
		for (int i = 0; i < size; i++) {
			graph.getModel().addCell(identifier(i), CellType.CIRCLE);
		}
		Random random = new Random();
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
				System.out.println("OPS");
			} while (graph.getModel().addedCost(sourceId, targetId) != null || sourceId.equals(targetId));
		}
		graph.endUpdate();
		CircleLayout.generateCircle(graph.getModel().getAllCells(), graph.getModel(), 0, 0);
		System.out.println("TERMINOU");
	}



}