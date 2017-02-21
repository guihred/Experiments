package javaexercises.graphs;


import java.util.Random;

public class RandomTopology extends BaseTopology {

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


}