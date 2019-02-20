package gaming.ex21;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class CatanModel {
    private List<Terrain> terrains = new ArrayList<>();
    private List<SettlePoint> settlePoints = new ArrayList<>();

    private void addTerrains(StackPane root) {
        List<Integer> numbers = getNumbers();
        List<Circle> points = new ArrayList<>();
        List<ResourceType> cells = createResources();
        double radius = Terrain.RADIUS * Math.sqrt(3);
        for (int i = 3, j = 0, l = 0; j < createResources().size(); j += i, i += j > 11 ? -1 : 1, l++) {
            List<ResourceType> subList = cells.subList(j, j + i);
            for (int k = 0; k < subList.size(); k++) {
                ResourceType resourceType = subList.get(k);
                Terrain cell = new Terrain(resourceType);
                double f = -radius / 2 * (i - 3);
                double x = radius * k + f + radius;
                double y = radius * l * Math.sqrt(3) / 2;
                cell.relocate(x, y);
                if (resourceType != ResourceType.DESERT) {
                    cell.setNumber(numbers.remove(0));
                }
                List<Circle> circles = getCircles(x, y);
                for (Circle circle : circles) {
                    if (points.stream().noneMatch(e -> e.intersects(circle.getBoundsInLocal()))) {
                        points.add(circle);
                    }
                }
                terrains.add(cell);
                root.getChildren().add(cell);
            }
        }
        settlePoints = points.stream().map(SettlePoint::new).collect(Collectors.toList());
        root.getChildren().addAll(settlePoints);
        root.setManaged(false);
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

    private List<Circle> getCircles(double xOff,double yOff) {
        List<Circle> circles = new ArrayList<>();
        double off = Math.PI / 6;
        for (int i = 0; i < 6; i++) {
            double d = Math.PI / 3;
            double x = Math.cos(off + d * i) * Terrain.RADIUS + Terrain.RADIUS;
            double y = Math.sin(off + d * i) * Terrain.RADIUS + Terrain.RADIUS;
            Circle circle = new Circle(xOff + x - Terrain.RADIUS / 10., yOff + y, Terrain.RADIUS / 5, Color.ALICEBLUE);
            circles.add(circle);
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
        List<Integer> collect = IntStream.range(2, 13)
                .flatMap(e -> IntStream.generate(() -> e).limit(getLimit(e))).boxed()
                .collect(Collectors.toList());
        Collections.shuffle(collect);
        return collect;
    }

    private void initialize(StackPane root) {
        addTerrains(root);
    }

    public static void create(StackPane root) {
        new CatanModel().initialize(root);

    }
}
