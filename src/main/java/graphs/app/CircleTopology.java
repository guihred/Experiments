package graphs.app;

import graphs.entities.CellType;
import graphs.entities.Graph;
import java.util.Random;

public class CircleTopology extends BaseTopology {


	public CircleTopology(int size, Graph graph) {
		super(graph, "Circle", size);
	}

	@Override
	public void execute() {
		graph.clean();
		graph.getModel().removeAllCells();
		graph.getModel().removeAllEdges();
		for (int i = 0; i < getSize(); i++) {
			graph.getModel().addCell(identifier(i), CellType.CIRCLE);
		}
		Random random = new Random();
		int nextInt = random.nextInt(getSize() * (getSize() - 1) / 2) + 1;
		for (int i = 0; i < nextInt; i++) {
            String targetId;
			String sourceId;
			Integer valor = random.nextInt(getSize()) + 1;
			do {
                targetId = identifier(random.nextInt(getSize()));
				sourceId = identifier(random.nextInt(getSize()));
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
        CircleLayout.generateCircle(graph.getModel().getAllCells(), graph.getModel().getAllEdges());
	}



}