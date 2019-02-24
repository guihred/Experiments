package gaming.ex21;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import utils.ResourceFXUtils;

public class CatanModel {
    private List<Terrain> terrains = new ArrayList<>();
    private List<SettlePoint> settlePoints = new ArrayList<>();

    private DragContext dragContext = new DragContext();
    private StackPane center;
    private List<EdgeCatan> edges;

    private void addTerrains(StackPane root) {
        List<Integer> numbers = getNumbers();
        List<ResourceType> cells = createResources();
        double radius = Terrain.RADIUS * Math.sqrt(3);
        for (int i = 3, j = 0, l = 0; j < cells.size(); j += i, i += j > 11 ? -1 : 1, l++) {
            List<ResourceType> resources = cells.subList(j, j + i);
            for (int k = 0; k < resources.size(); k++) {
                Terrain terrain = new Terrain(resources.get(k));
                double f = -radius / 2 * (i - 3);
                double x = radius * k + f + radius;
                double y = radius * l * Math.sqrt(3) / 2;
                terrain.relocate(x, y);
                if (resources.get(k) != ResourceType.DESERT) {
                    terrain.setNumber(numbers.remove(0));
                }
                createSettlePoints(terrain, x, y);
                terrains.add(terrain);
                root.getChildren().add(terrain);
            }
        }

        root.setManaged(false);
        settlePoints.forEach(e -> System.out.println(e.getIdPoint() + " "
                + e.getNeighbors().stream().map(SettlePoint::getIdPoint).collect(Collectors.toList())));

        edges = settlePoints.stream().flatMap(s -> s.getNeighbors().stream().map(t -> new EdgeCatan(s, t))).distinct()
                .collect(Collectors.toList());
        root.getChildren().addAll(edges);
        root.getChildren().addAll(settlePoints);

    }

    private List<ResourceType> createResources() {
        EnumMap<ResourceType, Integer> resourcesMap = new EnumMap<>(ResourceType.class);
        resourcesMap.put(ResourceType.DESERT, 1);
        resourcesMap.put(ResourceType.BRICK, 3);
        resourcesMap.put(ResourceType.ROCK, 3);
        resourcesMap.put(ResourceType.SHEEP, 4);
        resourcesMap.put(ResourceType.WHEAT, 4);
        resourcesMap.put(ResourceType.WOOD, 4);
        List<ResourceType> resourceTypes = resourcesMap.entrySet().stream()
                .flatMap(e -> Stream.generate(e::getKey).limit(e.getValue())).collect(Collectors.toList());
        Collections.shuffle(resourceTypes);
        return resourceTypes;
    }

    private void createSettlePoints(Terrain cell, double x, double y) {
        for (SettlePoint p : getCircles(x, y)) {
            if (settlePoints.stream().noneMatch(e -> intersects(p, e))) {
                settlePoints.add(p);
                p.addTerrain(cell);
            } else {
                p.removeNeighbors();
            }
            settlePoints.stream().filter(e -> intersects(p, e)).findFirst()
                    .ifPresent(e -> e.addTerrain(cell).addAllNeighbors(p));

        }
    }

    private boolean edgeAcceptRoad(EdgeCatan edge, Road road) {
        return edge.matchColor(road.getPlayer())
                || edges.stream().anyMatch(e -> e.getElement() != null && e.getPoints().stream()
                        .anyMatch(p -> edge.getPoints().contains(p) && e.matchColor(road.getPlayer())));
    }

    private boolean edgeAcceptRoad(Optional<EdgeCatan> firstEdge, Road road) {
        if (!firstEdge.isPresent()) {
            return false;
        }
        EdgeCatan edgeCatan = firstEdge.get();
        return edgeAcceptRoad(edgeCatan, road);
    }

    private List<SettlePoint> getCircles(double xOff, double yOff) {
        List<SettlePoint> circles = new ArrayList<>();
        double off = Math.PI / 6;
        for (int i = 0; i < 6; i++) {
            double d = Math.PI / 3;
            double x = Math.cos(off + d * i) * Terrain.RADIUS + Terrain.RADIUS;
            double y = Math.sin(off + d * i) * Terrain.RADIUS + Terrain.RADIUS;
            double centerX = xOff + x - Terrain.RADIUS / 10.;
            double centerY = yOff + y;
            SettlePoint e = new SettlePoint(centerX, centerY);
            circles.add(e);
            if (circles.size() > 1) {
                e.addNeighbor(circles.get(i - 1));
            }
            if (circles.size() == 6) {
                e.addNeighbor(circles.get(0));
            }
        }
        return circles;
    }

    private int getLimit(int e) {
        if (e == 7) {
            return 0;
        }
        if (e == 2 || e == 12) {
            return 1;
        }
        return 2;
    }

    private List<Integer> getNumbers() {
        List<Integer> numbers = IntStream.range(2, 13).flatMap(e -> IntStream.generate(() -> e).limit(getLimit(e)))
                .boxed().collect(Collectors.toList());
        Collections.shuffle(numbers);
        return numbers;
    }

    private void handleMouseDragged(MouseEvent event) {
        double offsetX = event.getScreenX() + dragContext.x;
        double offsetY = event.getScreenY() + dragContext.y;
        if (dragContext.element != null) {
            CatanResource c = dragContext.element;
            c.relocate(offsetX, offsetY);
            if (dragContext.element instanceof Village) {
                if (dragContext.point != null) {
                    dragContext.point.toggleFade(1);
                    dragContext.point = null;
                }
                settlePoints.stream().filter(e -> inArea(event, e)).findFirst()
                        .ifPresent(e -> dragContext.point = e.toggleFade(-1));
            }
            if (dragContext.element instanceof Road) {
                Road element = (Road) dragContext.element;
                if (dragContext.edge != null) {
                    dragContext.edge.toggleFade(1, edgeAcceptRoad(dragContext.edge, element));
                    dragContext.edge = null;
                }
                edges.stream().filter(e -> inArea(event, e)).findFirst()
                        .ifPresent(e -> dragContext.edge = e.toggleFade(-1, edgeAcceptRoad(e, element)));
            }

        }
    }

    private void handleMousePressed(MouseEvent event) {
        Node node = (Node) event.getSource();
        dragContext.x = node.getBoundsInParent().getMinX() - event.getScreenX();
        dragContext.y = node.getBoundsInParent().getMinY() - event.getScreenY();
        if (node instanceof CatanResource && center.equals(node.getParent())) {
            dragContext.element = (CatanResource) node;
        }
    }

    private void handleMouseReleased(MouseEvent event) {
        if (dragContext.element instanceof Village) {
            Optional<SettlePoint> findFirst = settlePoints.stream()
                    .filter(e -> inArea(event, e)).findFirst();
            if (findFirst.isPresent() && !findFirst.get().isPointDisabled()) {
                findFirst.get().setElement(dragContext.element);
            } else {
                dragContext.element.relocate(0, 0);
            }
            if (dragContext.point != null) {
                dragContext.point.toggleFade(1);
                dragContext.point = null;
            }
            dragContext.element = null;
        }
        if (dragContext.element instanceof Road) {
            Optional<EdgeCatan> edgeHovered = edges.stream()
                    .filter(e -> inArea(event, e)).findFirst();
            Road road = (Road) dragContext.element;
            if (edgeAcceptRoad(edgeHovered, road)) {
                edgeHovered.get().setElement(road);
            } else {
                dragContext.element.relocate(0, 0);
            }
            if (dragContext.edge != null) {
                dragContext.edge.toggleFade(1, edgeAcceptRoad(edgeHovered, road));
                dragContext.edge = null;
            }
            dragContext.element = null;
        }
    }

    private boolean inArea(MouseEvent event, Node e) {
        return e.getBoundsInParent().contains(event.getSceneX(), event.getSceneY());
    }

    private void initialize(StackPane center1, VBox value) {
        center = center1;
        addTerrains(center1);
        center.getChildren().add(makeDraggable(new Village(PlayerColor.BLUE)));
        center.getChildren().add(makeDraggable(new Village(PlayerColor.BEIGE)));
        center.getChildren().add(makeDraggable(new Village(PlayerColor.RED)));
        center.getChildren().add(makeDraggable(new Road(PlayerColor.BEIGE)));
        center.getChildren().add(makeDraggable(new Road(PlayerColor.BEIGE)));
        center.getChildren().add(makeDraggable(new Road(PlayerColor.BLUE)));
        center.getChildren().add(makeDraggable(new Road(PlayerColor.RED)));
        
        for (Combination combination : Combination.values()) {
            List<ResourceType> resources = combination.getResources();
            HBox e = new HBox();
            for (ResourceType resourceType : resources) {
                ImageView e2 = new ImageView(ResourceFXUtils.toExternalForm("catan/" + resourceType.getPure()));
                e2.setFitWidth(20);
                e2.setPreserveRatio(true);
                e.getChildren().add(e2);
            }
            value.getChildren().add(e);
        }
        
        
        
        
    }

    private boolean intersects(SettlePoint p, SettlePoint e) {
        return e.getBoundsInParent().intersects(p.getBoundsInParent());
    }

    private CatanResource makeDraggable(CatanResource e) {
        e.setOnMousePressed(this::handleMousePressed);
        e.setOnMouseDragged(this::handleMouseDragged);
        e.setOnMouseReleased(this::handleMouseReleased);
        return e;
    }

    public static void create(StackPane root, VBox value) {
        new CatanModel().initialize(root, value);
    }
}
