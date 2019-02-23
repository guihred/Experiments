package gaming.ex21;

import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import simplebuilder.SimpleFadeTransitionBuilder;

public class SettlePoint extends Group {
    private static int i = 1;

    private CatanResource element;
    private final Circle circle = new Circle(20, Color.BLACK);
    private final ObservableList<Terrain> terrains = FXCollections.observableArrayList();
    private final ObservableList<SettlePoint> neighbors = FXCollections.observableArrayList();
    private final int id;
    private final FadeTransition highlightTransition = new SimpleFadeTransitionBuilder().node(circle)
            .duration(Duration.millis(200)).fromValue(1).toValue(0).build();

    public SettlePoint(double x, double y) {
        highlightTransition.play();
        relocate(x, y);
        getChildren().add(circle);
        id = i++;
        setManaged(false);

    }

    public void addAllNeighbors(SettlePoint p) {
        if (p != this) {
            p.getNeighbors().forEach(this::addNeighbor);
        }
    }

    public void addNeighbor(SettlePoint point) {
        if (!getNeighbors().contains(point)) {
            getNeighbors().add(point);
        }
        if (!point.getNeighbors().contains(this)) {
            point.getNeighbors().add(this);
        }
    }

    public void addTerrain(Terrain terrain) {
        if (!terrains.contains(terrain)) {
            terrains.add(terrain);
        }
    }

    public Circle getCircle() {
        return circle;
    }

    public CatanResource getElement() {
        return element;
    }

    public int getIdPoint() {
        return id;
    }

    public ObservableList<SettlePoint> getNeighbors() {
        return neighbors;
    }

    public ObservableList<Terrain> getTerrains() {
        return terrains;
    }

    @Override
    public int hashCode() {
        return id;
    }

    public boolean isPointDisabled() {
        return neighbors.stream().anyMatch(e -> e.element != null);
    }

    public void removeNeighbors() {
        for (SettlePoint settlePoint : neighbors) {
            settlePoint.neighbors.remove(this);
        }
    }

    public void setElement(CatanResource element) {
        StackPane parent = (StackPane) element.getParent();
        parent.getChildren().remove(element);
        getChildren().add(element);
        element.setLayoutX(-element.getImage().getWidth() / 2);
        element.setLayoutY(-element.getImage().getHeight() / 2);
        toggleFade(1);
        this.element = element;
    }

    public SettlePoint toggleFade(int r) {
        if (isPointDisabled()) {
            circle.setFill(Color.RED);
        }
        if (element == null) {
            highlightTransition.setRate(r);
            highlightTransition.play();
        }
        return this;
    }

    @Override
    public String toString() {
        return "(" + id + ")";
    }

}
