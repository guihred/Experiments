package graphs.app;

import java.util.List;
import java.util.Random;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

public class ConvergeLayout implements Layout {

	private Random rnd = new Random();

	private final EventHandler<ActionEvent> eventHandler;

    private Graph graph;
	public ConvergeLayout(Graph graph) {
        this.graph = graph;
        eventHandler = t1 -> convergeLayoutLoop(this.graph);
	}

    private void convergeLayoutLoop(Graph graph1) {
        List<Cell> allCells = graph1.getModel().getAllCells();
		if (allCells.size() > 100) {
			return;
		}

        int c = allCells.size() / 50;
        double bound = (c + 1) * 100d;

		Cell cell = allCells.get(rnd.nextInt(allCells.size()));
        List<Edge> edges = graph1.getModel().edges(cell);
		if (!edges.isEmpty()) {

            double media = graph1.getModel().getAllEdges().parallelStream().mapToInt(Edge::getValor).average()
                    .getAsDouble();
			double sumX = edges.parallelStream().mapToDouble(e1 -> calculateXSum(bound, media, e1)).average().getAsDouble();
			double sumY = edges.parallelStream().mapToDouble(e2 -> calculateYSum(bound, media, e2)).average().getAsDouble();
			if (sumY < bound / 2 || sumX < bound / 2) {
				double layoutX = cell.getLayoutX();
				double layoutY = cell.getLayoutY();
					cell.relocate(layoutX + sumX, layoutY + sumY);
			}

		}
	}

    private static double calculateYSum(double bound, double media, Edge e2) {
		double angulo2 = e2.getAngulo();
		Integer valor = e2.getValor();
		return Math.sin(angulo2) * bound * valor / media - Math.sin(angulo2) * e2.getModulo() * valor / media;
	}

    private static double calculateXSum(double bound, double media, Edge e1) {
		double angulo1 = e1.getAngulo();
		Integer valor = e1.getValor();
		return Math.cos(angulo1) * bound * valor / media - Math.cos(angulo1) * e1.getModulo() * valor / media;
	}

	@Override
    public void execute() {
        graph.clean();
		for (int j = 0; j < 500; j++) {
			getEventHandler().handle(null);
		}
	}

	public EventHandler<ActionEvent> getEventHandler() {
		return eventHandler;
	}
}