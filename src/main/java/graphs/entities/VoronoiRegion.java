package graphs.entities;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import utils.MatrixSolver;

public class VoronoiRegion extends Group {

	public VoronoiRegion(Ponto p, List<Triangle> triangles) {
        Comparator<double[]> comparator = Comparator
                .comparing((double[] pon) -> Edge.getAngulo(pon[0], pon[1], x(p.getC()), y(p.getC())));
		List<double[]> centerPoints = triangles.stream().map(t -> centerCircle(t.getA().getC(), t.getB().getC(), t.getC().getC())).collect(Collectors.toList());
		for (double[] es : centerPoints) {
			Circle circle = new Circle(2);
			circle.setLayoutX(es[0]);
			circle.setLayoutY(es[1]);
			circle.setFill(Color.RED);
			getChildren().add(0, circle);
		}
		double w = p.getC().getBoundsInLocal().getWidth() / 2;
		double h = p.getC().getBoundsInLocal().getHeight() / 2;
		List<double[]> pontosImportantes = triangles.stream().flatMap(Triangle::allPoints).filter(pon -> !p.equals(pon)).distinct().map(p::add)
				.map(po -> po.mult(0.5)).map(po -> new double[] { po.getX() + w, po.getY() + h }).collect(Collectors.toList());

		double[] array = centerPoints.stream().sorted(comparator).flatMap((double[]t) -> Stream.of(cen(t))).mapToDouble(d -> d).toArray();

		Polygon polygon = new Polygon(array);
		double x = x(p.getC());
		double y = y(p.getC());
		pontosImportantes.add(new double[] { x, y });
		for (int i = 0; i < pontosImportantes.size(); i++) {
			double[] ds = pontosImportantes.get(i);
			if (!polygon.contains(ds[0], ds[1])) {
				centerPoints.add(new double[] { ds[0], ds[1] });
				double avgx = centerPoints.stream().mapToDouble(e -> e[0]).average().getAsDouble();
				double avgy = centerPoints.stream().mapToDouble(e -> e[1]).average().getAsDouble();
				Comparator<double[]> comparing = Comparator
						.comparing((double[] pon) -> Edge.getAngulo(pon[0], pon[1], avgx, avgy));
				array = centerPoints.stream().sorted(comparing).flatMap((double[] t) -> Stream.of(cen(t)))
						.mapToDouble(d -> d).toArray();
				polygon = new Polygon(array);
			}
		}
		polygon.setStroke(Color.BURLYWOOD);
        polygon.fillProperty().bind(p.getC().colorProperty());
		getChildren().add(0, polygon);
	}

    private static Double[] cen(double[] a) {
		Double[] b = new Double[a.length];
		for (int i = 0; i < a.length; i++) {
			b[i] = a[i];
		}
		return b;
	}

	private static double[] centerCircle(Cell a, Cell b, Cell c) {
		double ay = y(a);
		double ax = x(a);
		double bx = x(b);
		double by = y(b);
		double cy = y(c);
		double cx = x(c);
		double[] coef2 = new double[] { -ay * ay - ax * ax + bx * bx + by * by, -ay * ay - ax * ax + cx * cx + cy * cy };
		double[][] matr = new double[][] { { 2 * (bx - ax), 2 * (by - ay) }, { 2 * (cx - ax), 2 * (cy - ay) }, };

		return MatrixSolver.solve(matr, coef2);
	}

    private static double x(Cell d) {
		return d.getLayoutX() + d.getBoundsInLocal().getWidth() / 2;
	}

    private static double y(Cell d) {
		return d.getLayoutY() + d.getBoundsInLocal().getHeight() / 2;
	}

}