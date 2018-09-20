package graphs.app;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Text;

public class Edge extends Group implements Comparable<Edge> {

	private static final int ARROW_SIZE = 5;
	protected Cell source;
	protected Cell target;
	protected BooleanProperty selected = new SimpleBooleanProperty(false);
	private Line line;
	private final Integer valor;

	public Edge(Cell source, Cell target) {
		this(source, target, 1);
	}

	public Edge(Cell source, Cell target, Integer valor) {
		this(source, target, valor, true);
	}

	public Edge(Cell source, Cell target, Integer valor, boolean directed) {

		this.source = source;
		this.target = target;
		this.valor = valor;

		source.addCellChild(target);
		target.addCellParent(source);

		line = new Line();
		line.fillProperty().bind(Bindings.when(selected).then(Color.RED).otherwise(Color.BLACK));
		line.strokeProperty().bind(Bindings.when(selected).then(Color.RED).otherwise(Color.BLACK));
        line.startXProperty().bind(source.layoutXProperty().add(source.getBoundsInParent().getWidth() / 2.0));
        line.startYProperty().bind(source.layoutYProperty().add(source.getBoundsInParent().getHeight() / 2.0));
        line.endXProperty().bind(target.layoutXProperty().add(target.getBoundsInParent().getWidth() / 2.0));
        line.endYProperty().bind(target.layoutYProperty().add(target.getBoundsInParent().getHeight() / 2.0));

		getChildren().add(line);
		if (directed) {
			Polygon view1 = new Polygon(-Math.sqrt(3) * ARROW_SIZE / 2, 0, Math.sqrt(3) * ARROW_SIZE / 2, ARROW_SIZE, Math.sqrt(3) * ARROW_SIZE / 2,
					-ARROW_SIZE);
			view1.strokeProperty().bind(Bindings.when(selected).then(Color.RED).otherwise(Color.BLACK));
			view1.fillProperty().bind(Bindings.when(selected).then(Color.RED).otherwise(Color.BLACK));
			double width = target.getBoundsInParent().getWidth() / 3 * 2;
			view1.layoutXProperty()
					.bind(Bindings.createDoubleBinding(() -> line.getEndX() + Math.cos(getAngulo()) * width,
							line.endYProperty(), line.startYProperty(), line.endXProperty(), line.startXProperty()));
			view1.layoutYProperty()
					.bind(Bindings.createDoubleBinding(() -> line.getEndY() + Math.sin(getAngulo()) * width,
							line.endYProperty(), line.startYProperty(), line.endXProperty(), line.startXProperty()));
			view1.rotateProperty().bind(Bindings.createDoubleBinding(() -> Math.toDegrees(getAngulo()),
					line.endYProperty(), line.startYProperty(), line.endXProperty(),
							line.startXProperty()));
			getChildren().addAll(view1);
		}

		Text text = new Text(Integer.toString(valor));
		text.visibleProperty().bind(Graph.SHOW_WEIGHT);
		text.layoutXProperty().bind(line.startXProperty().add(line.endXProperty()).divide(2));
		text.layoutYProperty().bind(line.startYProperty().add(line.endYProperty()).divide(2));
		getChildren().addAll(text);

	}

	public final double getAngulo() {
		double a = line.getEndX() - line.getStartX();
		double b = line.getEndY() - line.getStartY();
		return a > 0 ? Math.PI + Math.atan(b / a) : Math.atan(b / a);
	}

	public static double getAngulo(double ax, double ay, double bx, double by) {
		double a = ax - bx;
		double b = ay - by;
		return a > 0 ? Math.PI + Math.atan(b / a) : Math.atan(b / a);
	}

	public double getModulo() {
		double a = line.getStartX() - line.getEndX();
		double b = line.getStartY() - line.getEndY();
		return Math.sqrt(a * a + b * b);
	}

	public Cell getSource() {
		return source;
	}

	public Cell getTarget() {
		return target;
	}

	public Integer getValor() {
		return valor;
	}

	public void setSelected(boolean selected) {
		this.selected.set(selected);
	}

	@Override
	public int compareTo(Edge o) {
		return Integer.compare(valor, o.valor);
	}

	@Override
	public String toString() {
		return source + "->" + target + "(" + valor + ")";
	}

}