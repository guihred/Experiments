package javaexercises.graphs;

import java.util.List;
import java.util.Random;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

public class ConvergeLayout implements Layout {

	private Random rnd = new Random();

	private final EventHandler<ActionEvent> eventHandler;
	public ConvergeLayout(Graph graph) {
		eventHandler = t1 -> convergeLayoutLoop(graph);
	}

	private void convergeLayoutLoop(Graph graph) {
		List<Cell> allCells = graph.getModel().getAllCells();
		if (allCells.size() > 100) {
			return;
		}

		double bound = (allCells.size() / 50 + 1) * 100;

		Cell cell = allCells.get(rnd.nextInt(allCells.size()));
		List<Edge> edges = graph.getModel().edges(cell);
		if (!edges.isEmpty()) {

			double media = graph.getModel().getAllEdges().parallelStream().mapToInt(Edge::getValor).average().getAsDouble();
			double sumX = edges.parallelStream().mapToDouble(e1 -> {
				double angulo1 = e1.getAngulo();
				Integer valor = e1.getValor();
				return Math.cos(angulo1) * bound * valor / media - Math.cos(angulo1) * e1.getModulo() * valor / media;
			}).average().getAsDouble();
			double sumY = edges.parallelStream().mapToDouble(e2 -> {
				double angulo2 = e2.getAngulo();
				Integer valor = e2.getValor();
				return Math.sin(angulo2) * bound * valor / media - Math.sin(angulo2) * e2.getModulo() * valor / media;
			}).average().getAsDouble();
			if (sumY < bound / 2 || sumX < bound / 2) {
				double layoutX = cell.getLayoutX();
				double layoutY = cell.getLayoutY();
					cell.relocate(layoutX + sumX, layoutY + sumY);
			}

		}
	}

	@Override
	public void execute() {
		for (int j = 0; j < 500; j++) {
			getEventHandler().handle(null);
		}
	}

	public EventHandler<ActionEvent> getEventHandler() {
		return eventHandler;
	}
}