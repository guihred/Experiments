package gaming.ex21;

import java.util.List;
import java.util.Optional;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

class CatanDragContext {
    private double x;
    private double y;
    private CatanResource element;
    private SettlePoint point;
    private EdgeCatan edge;
    private Terrain terrain;

    public void dragElement(MouseEvent event, List<SettlePoint> settlePoints2, List<Terrain> terrains2,
        List<EdgeCatan> edges2) {
        dragElement(this, event, settlePoints2, terrains2, edges2);
    }

    public void edgeFadeOut(boolean isEnabled) {
        if (edge != null) {
            edge.fadeOut(isEnabled);
            edge = null;
        }
    }

    public EdgeCatan getEdge() {
        return edge;
    }

    public CatanResource getElement() {
        return element;
    }

    public SettlePoint getPoint() {
        return point;
    }

    public Terrain getTerrain() {
        return terrain;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public void pointFadeOut() {
        if (point != null) {
            point.toggleFade(1);
            point = null;
        }
    }

    public void pressElement(MouseEvent event, Pane center2, ObservableList<CatanResource> elements2) {
        pressElement(this, event, center2, elements2);
    }

    public EdgeCatan setEdge(EdgeCatan edge) {
        this.edge = edge;
        return edge;
    }

    public void setElement(CatanResource element) {
        this.element = element;
    }

    public SettlePoint setPoint(SettlePoint point) {
        this.point = point;
        return point;
    }

    public Terrain setTerrain(Terrain terrain) {
        this.terrain = terrain;
        return terrain;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void toggleTerrain(int r) {
        if (terrain != null) {
            terrain.toggleFade(r);
            terrain = null;
        }
    }

    private static void dragElement(CatanDragContext dragContext2, MouseEvent event, List<SettlePoint> settlePoints2,
        List<Terrain> terrains2, List<EdgeCatan> edges2) {
        double offsetX = event.getX() + dragContext2.getX();
        double offsetY = event.getY() + dragContext2.getY();
        if (dragContext2.getElement() != null) {
            CatanResource c = dragContext2.getElement();
            c.relocate(offsetX, offsetY);
            dragContext2.pointFadeOut();
            dragContext2.toggleTerrain(-1);
            if (dragContext2.getElement() instanceof Village) {
                settlePoints2.stream().filter(e -> CatanHelper.inArea(event.getX(), event.getY(), e)).findFirst()
                    .ifPresent(e -> dragContext2.setPoint(e.fadeIn()));
            }
            if (dragContext2.getElement() instanceof City) {
                settlePoints2.stream().filter(e -> CatanHelper.inArea(event.getX(), event.getY(), e))
                    .filter(e -> e.isSuitableForCity((City) dragContext2.getElement())).findFirst()
                    .ifPresent(e -> dragContext2.setPoint(e.fadeOut()));
            }
            if (dragContext2.getElement() instanceof Thief) {
                terrains2.stream().filter(e -> CatanHelper.inArea(event.getX(), event.getY(), e)).findFirst()
                    .ifPresent(e -> dragContext2.setTerrain(e.fadeIn()));
            }
            if (dragContext2.getElement() instanceof Road) {
                Road element = (Road) dragContext2.getElement();
                dragContext2.edgeFadeOut(EdgeCatan.edgeAcceptRoad(dragContext2.getEdge(), element));
                edges2.stream().filter(e -> CatanHelper.inArea(event.getX(), event.getY(), e)).findFirst()
                    .ifPresent(e -> dragContext2.setEdge(e.fadeIn(EdgeCatan.edgeAcceptRoad(e, element))));
            }
        }
    }

    private static void pressElement(CatanDragContext dragContext2, MouseEvent event, Pane center2,
        ObservableList<CatanResource> elements2) {
        Optional<Node> resourcePressed = center2.getChildren().parallelStream()
            .filter(e -> e.getBoundsInParent().contains(event.getX(), event.getY())).findFirst();
        if (resourcePressed.isPresent()) {
            Node node = resourcePressed.get();
            dragContext2.setX(node.getBoundsInParent().getMinX() - event.getX());
            dragContext2.setY(node.getBoundsInParent().getMinY() - event.getY());
            if (node instanceof CatanResource && elements2.contains(node)) {
                dragContext2.setElement((CatanResource) node);
                elements2.remove(node);
            }
        }
    }
}