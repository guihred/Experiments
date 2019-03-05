package gaming.ex21;

import graphs.entities.Edge;
import java.util.HashSet;
import java.util.Set;
import javafx.animation.FadeTransition;
import javafx.scene.Group;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.util.Duration;
import simplebuilder.SimpleFadeTransitionBuilder;

public class EdgeCatan extends Group {

    private final Set<SettlePoint> points = new HashSet<>();
    private final Line line = new Line(0, 0, 0, 0);
    private final FadeTransition highlightTransition = new SimpleFadeTransitionBuilder().node(line)
            .duration(Duration.millis(200)).fromValue(1).toValue(0).build();
    private CatanResource element;

    public EdgeCatan(SettlePoint a, SettlePoint b) {
        double x = a.getLayoutX() + b.getLayoutX();
        double y = a.getLayoutY() + b.getLayoutY();
        relocate(x / 2, y / 2);
        highlightTransition.play();
        double value = -a.getLayoutX() + x / 2;
        line.setFill(Color.TRANSPARENT);
        line.setStartX(value);
        line.setStartY(-a.getLayoutY() + y / 2);
        double value2 = x / 2 - b.getLayoutX();
        line.setEndX(value2);
        line.setEndY(y / 2 - b.getLayoutY());
        line.setStrokeWidth(10);
        getChildren().add(line);
        points.add(a);
        points.add(b);
        setManaged(false);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof EdgeCatan)) {
            return false;
        }
        return ((EdgeCatan) obj).points.equals(points);
    }

    public CatanResource getElement() {
        return element;
    }

    public Set<SettlePoint> getPoints() {
        return points;
    }

    @Override
    public int hashCode() {
        return points.hashCode();
    }

    public boolean matchColor(PlayerColor player) {
        return points.stream()
                .anyMatch(e -> e.getElement() != null && e.getElement().getPlayer() == player);
    }

    public void setElement(Road element) {
        StackPane parent = (StackPane) element.getParent();
        parent.getChildren().remove(element);
        getChildren().add(element);
        element.setLayoutX(-element.getImage().getWidth() / 2);
        element.setLayoutY(-element.getImage().getHeight() / 2);
        toggleFade(1, true);
        double angulo = Edge.getAngulo(line.getEndX(), line.getEndY(), line.getStartX(), line.getStartY());
        element.setRotate(Math.toDegrees(angulo));
        this.element = element;
    }

    public EdgeCatan toggleFade(int r, boolean enable) {
        if (enable) {
            line.setStroke(Color.GREEN);
        }

        if (element == null) {
            highlightTransition.setRate(r);
            highlightTransition.play();
        }
        return this;
    }

    @Override
    public String toString() {
        return "(" + points + ")";
    }

}
