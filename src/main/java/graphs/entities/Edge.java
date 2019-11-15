package graphs.entities;

import java.util.Objects;
import javafx.beans.NamedArg;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Text;

public class Edge extends Group implements Comparable<Edge> {

    private static final int ARROW_SIZE = 5;
    public static final BooleanProperty SHOW_WEIGHT = new SimpleBooleanProperty(true);
    protected Cell source;
    protected Cell target;
    protected BooleanProperty selected = new SimpleBooleanProperty(false);
    private Line line;
    private Integer valor;
    private boolean directed;

    public Edge(@NamedArg("source") Cell source, @NamedArg("target") Cell target) {
        this(source, target, 1);
    }

    public Edge(@NamedArg("source") Cell source, @NamedArg("target") Cell target, @NamedArg("valor") Integer valor) {
        this(source, target, valor, true);
    }

    public Edge(@NamedArg("source") Cell source, @NamedArg("target") Cell target, @NamedArg("valor") Integer valor,
        @NamedArg("directed") boolean directed) {

        this.source = source;
        this.target = target;
        this.valor = valor;
        this.directed = directed;

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
        DoubleBinding halfWayX = Bindings.createDoubleBinding(
            () -> line.getEndX() + Math.cos(getAngulo()) * getModulo() * 5 / 11, line.endYProperty(),
            line.startYProperty(), line.endXProperty(), line.startXProperty());
        DoubleBinding halfWayY = Bindings.createDoubleBinding(
            () -> line.getEndX() == line.getStartX() ? line.getStartY() + (line.getEndY() - line.getStartY()) * 5 / 11
                : line.getEndY() + Math.sin(getAngulo()) * getModulo() * 5 / 11,
            line.endYProperty(), line.startYProperty(), line.endXProperty(), line.startXProperty());
        if (directed) {
            Polygon view1 = new Polygon(-Math.sqrt(3) * ARROW_SIZE / 2, 0, Math.sqrt(3) * ARROW_SIZE / 2, ARROW_SIZE,
                Math.sqrt(3) * ARROW_SIZE / 2, -ARROW_SIZE);
            view1.strokeProperty().bind(Bindings.when(selected).then(Color.RED).otherwise(Color.BLACK));
            view1.fillProperty().bind(Bindings.when(selected).then(Color.RED).otherwise(Color.BLACK));
            view1.layoutXProperty().bind(halfWayX);
            view1.layoutYProperty().bind(halfWayY);
            view1.rotateProperty().bind(Bindings.createDoubleBinding(() -> Math.toDegrees(getAngulo()),
                line.endYProperty(), line.startYProperty(), line.endXProperty(), line.startXProperty()));
            getChildren().addAll(view1);
        }

        DoubleBinding yCoord = Bindings.createDoubleBinding(() -> Math.sin(getAngulo() + Math.PI / 2) * 10,
            line.endYProperty(), line.startYProperty(), line.endXProperty(), line.startXProperty());
        DoubleBinding xCoord = Bindings.createDoubleBinding(() -> Math.cos(getAngulo() + Math.PI / 2) * 10,
            line.endYProperty(), line.startYProperty(), line.endXProperty(), line.startXProperty());

        Text text = new Text(Integer.toString(valor));
        text.fillProperty().bind(Bindings.when(selected).then(Color.RED).otherwise(Color.BLACK));
        text.rotateProperty().bind(Bindings.createDoubleBinding(() -> Math.toDegrees(getAngulo() + Math.PI / 2),
            line.endYProperty(), line.startYProperty(), line.endXProperty(), line.startXProperty()));
        text.visibleProperty().bind(Edge.SHOW_WEIGHT);
        text.layoutXProperty().bind(halfWayX.add(xCoord));
        text.layoutYProperty().bind(halfWayY.add(yCoord));
        getChildren().addAll(text);

    }

    @Override
    public int compareTo(final Edge o) {
        return Integer.compare(valor, o.valor);
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj;
    }

    public final double getAngulo() {
        return getAngulo(line);
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

    @Override
    public int hashCode() {
        return Objects.hash(source, target);
    }

    public boolean isDirected() {
        return directed;
    }

    public void setSelected(final boolean selected) {
        this.selected.set(selected);
    }

    public void setValor(Integer valor) {
        this.valor = valor;
    }

    @Override
    public String toString() {
        return source + "->" + target + "(" + valor + ")";
    }

    public static double getAngulo(final double ax, final double ay, final double bx, final double by) {
        double a = ax - bx;
        double b = ay - by;
        return a > 0 ? Math.PI + Math.atan(b / a) : Math.atan(b / a);
    }

    public static double getAngulo(Line line) {
        return getAngulo(line.getEndX(), line.getEndY(), line.getStartX(), line.getStartY());
    }

}