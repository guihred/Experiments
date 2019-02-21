package gaming.ex21;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class SettlePoint extends Group {
    private static int i = 1;
    private final Circle circle = new Circle(10, Color.TRANSPARENT);
    private final ObservableList<Terrain> terrains = FXCollections.observableArrayList();
    private final ObservableList<SettlePoint> neighbors = FXCollections.observableArrayList();
    private final int id;

    public SettlePoint(double x, double y) {
        relocate(x, y);
        getChildren().add(circle);
        id = i++;
        setManaged(false);
    }

    public void addAllNeighbors(SettlePoint p) {
        if (p != this) {
            for (SettlePoint settlePoint : p.getNeighbors()) {
                addNeighbor(settlePoint);
            }
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

    public int getIdPoint() {
        return id;
    }

    public ObservableList<SettlePoint> getNeighbors() {
        return neighbors;
    }

    public ObservableList<Terrain> getTerrains() {
        return terrains;
    }

    public void removeNeighbors() {
        for (SettlePoint settlePoint : neighbors) {
            settlePoint.neighbors.remove(this);
        }
    }

    @Override
    public String toString() {
        return "[id=" + id + "]";
    }
}
