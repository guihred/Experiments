package gaming.ex21;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;

public class CatanModel {
    private List<Terrain> terrains = new ArrayList<>();
    private List<SettlePoint> settlePoints = new ArrayList<>();

    DragContext dragContext= new DragContext();
    private StackPane center;

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

        root.getChildren().addAll(settlePoints);
        root.setManaged(false);
        settlePoints.forEach(e -> System.out.println(e.getIdPoint() + " "
                + e.getNeighbors().stream().map(SettlePoint::getIdPoint).collect(Collectors.toList())));

        List<EdgeCatan> collect = settlePoints.stream().flatMap(s -> s.getNeighbors().stream().map(t -> new EdgeCatan(s, t)))
                .distinct().collect(Collectors.toList());
        root.getChildren()
                .addAll(collect);

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
            if (settlePoints.stream().noneMatch(e -> e.getBoundsInParent().intersects(p.getBoundsInParent()))) {
                settlePoints.add(p);
                p.addTerrain(cell);
            } else {
                p.removeNeighbors();
            }
            settlePoints.stream().filter(e -> e.getBoundsInParent().intersects(p.getBoundsInParent()))
                    .findFirst().ifPresent(e -> {
                        e.addTerrain(cell);
                        e.addAllNeighbors(p);
                    });

        }
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
            c.relocate(offsetX, offsetY );
            if (dragContext.point != null) {
                dragContext.point.toggleFade(1);
                dragContext.point = null;
            }
            settlePoints.stream()
                    .filter(e -> e.getBoundsInParent().contains(event.getSceneX(), event.getSceneY()))
                    .findFirst().ifPresent(e -> dragContext.point = e.toggleFade(-1));

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
                    .filter(e -> e.getBoundsInParent().contains(event.getSceneX(), event.getSceneY())).findFirst();
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
    }

    private void initialize(StackPane center1) {
        center = center1;
        addTerrains(center1);

        Village e1 = new Village(PlayerColor.BLUE);
        makeDraggable(e1);
        center.getChildren().add(e1);
        center.getChildren().add(new Road(PlayerColor.GOLD));
        Village e = new Village(PlayerColor.RED);
        makeDraggable(e);

        center.getChildren().add(e);
        center.getChildren().add(new Road(PlayerColor.BEIGE));
    }

    private void makeDraggable(Village e) {
        e.setOnMousePressed(this::handleMousePressed);
        e.setOnMouseDragged(this::handleMouseDragged);
        e.setOnMouseReleased(this::handleMouseReleased);
    }

    public static void create(StackPane root) {
        new CatanModel().initialize(root);

    }

    class DragContext{
        double x,y;
        CatanResource element;
        SettlePoint point;
    }
}
