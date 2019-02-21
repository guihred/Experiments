package gaming.ex21;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javafx.scene.layout.StackPane;

public class CatanModel {
    private List<Terrain> terrains = new ArrayList<>();
    private List<SettlePoint> settlePoints = new ArrayList<>();

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

    private void initialize(StackPane root) {
        addTerrains(root);
    }

    public static void create(StackPane root) {
        new CatanModel().initialize(root);

    }
}