package javaexercises.graphs;

import java.util.*;

public class ConcentricLayout implements Layout {

	Graph graph;

	Random rnd = new Random();

	public ConcentricLayout(Graph graph) {
		this.graph = graph;
	}

	final int BOUND = 100;

	@Override
	public void execute() {

		Set<Cell> allVisited = new HashSet<>();
		Model model = graph.getModel();
		List<Cell> allCells = model.getAllCells();
		List<Set<Cell>> groups = new ArrayList<>();
		for (Cell cell : allCells) {
			if (!allVisited.contains(cell)) {
				Set<Cell> visit = visit(cell, new HashSet<>());
				allVisited.addAll(visit);
				groups.add(visit);
			}
		}
		int size = groups.size();
		groups.sort(Comparator.comparing((Set<Cell> e) -> e.size()).reversed());

		Set<Cell> cells = groups.get(0);
		int size2 = cells.size();

		int a = CircleLayout.radius(size2) + (groups.size() > 1 ? CircleLayout.radius(groups.get(1).size()) : 0);
		CircleLayout.generateCircle(cells, model, 0, 0);
		double step = 360.0 / (size - 1);
		double angle = 0;

		for (int i = 1; i < groups.size(); i++) {
			double centerX = Math.cos(Math.toRadians(angle)) * a;
			double centerY = Math.sin(Math.toRadians(angle)) * a;

			CircleLayout.generateCircle(groups.get(i), model, centerX, centerY);
			int r = CircleLayout.radius(groups.get(i).size());
			double x2 = Math.cos(Math.toRadians(angle + step)) * a;
			double y2 = Math.sin(Math.toRadians(angle + step)) * a;
			double b = x2 - centerX;
			double c = y2 - centerY;
			if (b * b + c * c < r * r) {

				angle += step;
			}
			angle += step;
		}

	}


	private Set<Cell> visit(Cell c, Set<Cell> visited) {

		if (!visited.contains(c)) {
			visited.add(c);
			graph.getModel().adjacents(c).stream().distinct().forEach(cell -> {
				visit(cell, visited);
			});
		}
		return visited;
	}


}
