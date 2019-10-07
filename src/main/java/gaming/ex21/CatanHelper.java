package gaming.ex21;

import static gaming.ex21.CatanResource.newImage;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import simplebuilder.SimpleButtonBuilder;

public final class CatanHelper {
    private CatanHelper() {
    }

    public static List<EdgeCatan> addTerrains(Pane root, List<SettlePoint> settlePoints, Collection<Terrain> terrains,
        List<Port> ports) {
        root.getChildren().clear();
        List<Integer> numbers = Terrain.getNumbers();
        List<ResourceType> cells = ResourceType.createResources();
        final double radius = CatanResource.RADIUS * Math.sqrt(3);
        for (int i = 3, j = 0, l = 0; j < cells.size(); j += i, i += j > 11 ? -1 : 1, l++) {
            List<ResourceType> resources = cells.subList(j, j + i);
            for (int k = 0; k < resources.size(); k++) {
                Terrain terrain = new Terrain(resources.get(k));
                double f = -radius / 2 * (i - 3);
                double x = radius * k + f + radius * 3 / 2;
                double y = radius * l * Math.sqrt(3) / 2 + radius / 3;
                terrain.relocate(x, y);
                if (resources.get(k) != ResourceType.DESERT) {
                    terrain.setNumber(numbers.remove(0));
                }
                CatanHelper.createSettlePoints(terrain, x, y, settlePoints);
                terrains.add(terrain);
                root.getChildren().add(terrain);
            }
        }
        List<EdgeCatan> catanEdges = settlePoints.stream()
            .flatMap(s -> s.getNeighbors().stream().map(t -> new EdgeCatan(s, t))).distinct()
            .collect(Collectors.toList());
        catanEdges.forEach(e -> e.getPoints().forEach(p -> p.getEdges().add(e)));
        Collections.shuffle(ports);
        Port.relocatePorts(settlePoints, ports);
        root.getChildren().addAll(catanEdges);
        root.getChildren().addAll(ports);
        root.getChildren().addAll(settlePoints);
        return catanEdges;

    }

    public static void combinationGrid(GridPane value, Consumer<Combination> onClick, Predicate<Combination> isDisabled,
        ObjectProperty<PlayerColor> currentPlayer, BooleanProperty diceThrown) {
        Combination[] combinations = Combination.values();
        for (int i = 0; i < combinations.length; i++) {
            Combination combination = combinations[i];
            List<ResourceType> resources = combination.getResources();
            Button button = SimpleButtonBuilder.newButton(newImage(combination.getElement(), 30, 30), "" + combination,
                e -> onClick.accept(combination));
            button.setUserData(combination);
            button.disableProperty()
                .bind(Bindings.createBooleanBinding(() -> isDisabled.test(combination), currentPlayer, diceThrown));
            value.addRow(i, button);
            for (ResourceType resourceType : resources) {
                value.addRow(i, newImage(resourceType.getPure(), 20));
            }
        }
    }

    public static void createSettlePoints(Terrain terrain, final double x, final double y,
        Collection<SettlePoint> points) {
        for (SettlePoint p : CatanHelper.getSettlePoints(x, y)) {
            if (points.stream().noneMatch(e -> CatanHelper.intersects(p, e))) {
                points.add(p);
                p.addTerrain(terrain);
            } else {
                p.removeNeighbors();
            }
            points.stream().filter(e -> CatanHelper.intersects(p, e)).findFirst()
                .ifPresent(e -> e.addTerrain(terrain).addAllNeighbors(p));
        }
    }

    public static List<SettlePoint> getSettlePoints(final double xOff, final double yOff) {
        List<SettlePoint> points = new ArrayList<>();
        double off = Math.PI / 6;
        for (int i = 0; i < 6; i++) {
            double d = Math.PI / 3;
            double x = Math.cos(off + d * i) * CatanResource.RADIUS + CatanResource.RADIUS;
            double y = Math.sin(off + d * i) * CatanResource.RADIUS + CatanResource.RADIUS;
            final double centerX = xOff + x - CatanResource.RADIUS / 2.5;
            final double centerY = yOff + y - CatanResource.RADIUS / 4;
            SettlePoint e = new SettlePoint();
            e.relocate(centerX, centerY);
            points.add(e);
        }
        for (int i = 0; i < points.size(); i++) {
            SettlePoint e = points.get(i);
            e.addNeighbor(points.get((i + 1) % points.size()));
        }
        return points;
    }

    public static boolean inArea(double x, double y, Node e) {
        return e.getBoundsInParent().contains(x, y);
    }

    public static boolean intersects(final SettlePoint p, final SettlePoint e) {
        return e.getBoundsInParent().intersects(p.getBoundsInParent());
    }

    public static boolean isSkippable(BooleanProperty diceThrown2, HBox resourceChoices2,
        ObservableList<CatanResource> elements2, ObjectProperty<PlayerColor> currentPlayer2) {
        return !diceThrown2.get() || resourceChoices2.isVisible()
            || elements2.stream().anyMatch(e -> e.getPlayer() == currentPlayer2.get());
    }

    public static ImageView newResource(final ResourceType type) {
        return CatanResource.newImage(type.getPure(), Port.SIZE / 4.);
    }

    static int getDirection(int turnCount2) {
        if (turnCount2 == 4) {
            return 0;
        } else if (turnCount2 > 4 && turnCount2 < 8) {
            return -1;
        } else {
            return 1;
        }
    }
}
