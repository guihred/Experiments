package javaexercises.graphs;

import exercism.MatrixSolver;
import java.util.List;
import java.util.Random;

public class GabrielTopology extends GenTopology {

	public GabrielTopology(int size, Graph graph) {
		super(graph, "Gabriel", size);
	}

	@Override
	public void execute() {
		graph.clean();
		graph.getModel().removeAllCells();
		graph.getModel().removeAllEdges();
		Random rnd = new Random();
		int bound = 100;
		int nextInt = rnd.nextInt(180) - 180;
		double x = bound * Math.cos(Math.toRadians(nextInt));
		double y = bound * Math.sin(Math.toRadians(nextInt));
		for (int i = 0; i < size; i++) {
			Cell cell = graph.getModel().addCell(CircleTopology.identifier(i), CellType.CIRCLE);
			nextInt = rnd.nextInt(360);
			x += bound * Math.cos(Math.toRadians(nextInt));
			y += bound * Math.sin(Math.toRadians(nextInt));
			cell.relocate(x, y);
		}
		List<Cell> cells = graph.getModel().getAddedCells();
		for (Cell cell : cells) {
			double x1 = cell.getLayoutX();
			double y1 = cell.getLayoutY();
			for (Cell cell2 : cells) {
				double m = (x1 + cell2.getLayoutX()) / 2;
				double n = (y1 + cell2.getLayoutY()) / 2;
				double r = distance(x1, cell2.getLayoutX(), y1, cell2.getLayoutY());
				if (cells.stream().filter(c -> c != cell && c != cell2).noneMatch(c -> distance(m, c.getLayoutX(), n, c.getLayoutY()) <= r / 2)) {
					graph.getModel().addBiEdge(cell.cellId, cell2.cellId, 1);
				}

			}
		}
		graph.endUpdate();

	}

	public double distance(double x1, double x2, double y1, double y2) {
		double a = x1 - x2;
		double b = y1 - y2;
		return Math.sqrt(a * a + b * b);
	}

	public static double determinant(Cell a, Cell b, Cell c, Cell d) {
		double ay = a.getLayoutY();
		double ax = a.getLayoutX();
		double by = b.getLayoutY();
		double bx = b.getLayoutX();
		double cy = c.getLayoutY();
		double cx = c.getLayoutX();
		double dy = d.getLayoutY();
		double dx = d.getLayoutX();
		double[][] matrix = new double[][] { { ax, ay, ax * ax + ay * ay, 1 }, { bx, by, bx * bx + by * by, 1 }, { cx, cy, cx * cx + cy * cy, 1 },
				{ dx, dy, dx * dx + dy * dy, 1 } };
		return MatrixSolver.determinant(matrix);

	}


}