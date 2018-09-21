package graphs.topology;


import graphs.entities.Cell;
import graphs.entities.CellType;
import graphs.entities.Graph;
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
        int bounds = 400;
		Random random = new Random();
		for (int i = 0; i < getSize(); i++) {
            Cell addCell = graph.getModel().addCell(identifier(i), i % 2 == 0 ? CellType.CIRCLE : CellType.TRIANGLE);
            addCell.relocate(random.nextGaussian() * bounds, random.nextGaussian() * bounds);
		}
		int nextInt = random.nextInt(getSize() * (getSize() - 1) / 2) + 1;
		for (int i = 0; i < nextInt; i++) {
			String sourceId;
			String targetId;
			Integer valor = random.nextInt(getSize()) + 1;
			do {
				sourceId = identifier(random.nextInt(getSize()));
				targetId = identifier(random.nextInt(getSize()));
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