package graphs.app;

import graphs.entities.CellType;
import graphs.entities.Graph;
import java.security.SecureRandom;
import javafx.beans.NamedArg;

public class CircleTopology extends BaseTopology {

    private final SecureRandom random = new SecureRandom();

    public CircleTopology(@NamedArg("graph") Graph graph) {
        super(graph, "Circle", 50);
    }

    public CircleTopology(@NamedArg("size") int size, @NamedArg("graph") Graph graph) {
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
        double width = graph.getScrollPane().getViewportBounds().getWidth();
        double height = graph.getScrollPane().getViewportBounds().getHeight();
		CircleLayout.generateCircle(graph.getModel().getAllCells(), graph.getModel().getAllEdges(), width / 2,
            height / 2, 2);
	}



}