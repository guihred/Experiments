package gaming.ex21;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javafx.animation.FillTransition;
import javafx.beans.NamedArg;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import simplebuilder.SimpleCircleBuilder;
import simplebuilder.SimpleFillTransitionBuilder;

public class Terrain extends Group {

    private Thief thief;
    private final ResourceType type;
    private final IntegerProperty number = new SimpleIntegerProperty();
    private final StackPane stack;
    private final Circle circle;
    private final FillTransition highlightTransition;

    public Terrain(@NamedArg("type") final ResourceType type) {
        this.type = type;
        circle = new SimpleCircleBuilder().radius(CatanResource.RADIUS / 5.).fill(Color.BEIGE)
            .visible(type != ResourceType.DESERT).stroke(Color.BLACK).build();
        stack = new StackPane(getPolygon(), getCircle(), getNumberText());
        getChildren().add(stack);
        highlightTransition = new SimpleFillTransitionBuilder().shape(circle).duration(Duration.millis(200))
            .fromValue(Color.BEIGE).toValue(Color.GREEN).build();
        setManaged(false);
    }

    public void createSettlePoints(final double x, final double y, List<SettlePoint> settlePoints2) {
        Terrain cell = this;
        for (SettlePoint p : Terrain.getSettlePoints(x, y)) {
            if (settlePoints2.stream().noneMatch(e -> intersects(p, e))) {
                settlePoints2.add(p);
                p.addTerrain(cell);
            } else {
                p.removeNeighbors();
            }
            settlePoints2.stream().filter(e -> intersects(p, e)).findFirst()
                .ifPresent(e -> e.addTerrain(cell).addAllNeighbors(p));
        }
    }

    public Terrain fadeIn() {
        return toggleFade(1);
    }

    public Terrain fadeOut() {
        return toggleFade(-1);
    }

    public int getNumber() {
        return number.get();
    }

    public Thief getThief() {
        return thief;
    }

    public ResourceType getType() {
        return type;
    }

    public void removeThief() {
        if (thief != null) {
            stack.getChildren().remove(thief);
        }
    }

    public void setNumber(final int number) {
        this.number.set(number);
    }

    public void setThief(Thief thief) {
        if (thief != null) {
            StackPane parent = (StackPane) thief.getParent();
            parent.getChildren().remove(thief);
            getChildren().add(thief);
            thief.setLayoutX(CatanResource.RADIUS * Math.sqrt(3) / 2 - CatanResource.RADIUS / 4.);
            thief.setLayoutY(CatanResource.RADIUS - CatanResource.RADIUS / 4.);
            highlightTransition.setToValue(Color.RED);
        } else {
            if (Color.RED.equals(highlightTransition.getToValue())) {
                fadeOut();
            }
        }

        this.thief = thief;
    }

    public Terrain toggleFade(final int r) {
        highlightTransition.setToValue(thief != null ? Color.RED : Color.GREEN);
        highlightTransition.setRate(r);
        highlightTransition.play();
        return this;
    }

    private Circle getCircle() {
        return circle;
    }

    private Text getNumberText() {
        Text e = new Text();
        e.setTextOrigin(VPos.CENTER);
        e.setTextAlignment(TextAlignment.CENTER);
        e.setFont(Font.font(20));
        e.textProperty().bind(number.asString());
        e.setVisible(type != ResourceType.DESERT);
        return e;
    }

    private Polygon getPolygon() {
        Polygon polygon = new Polygon();
        double off = Math.PI / 6;
        for (int i = 0; i < 6; i++) {
            double d = Math.PI / 3;
            double x = Math.cos(off + d * i) * CatanResource.RADIUS;
            double y = Math.sin(off + d * i) * CatanResource.RADIUS;
            polygon.getPoints().addAll(x, y);
        }
        if (type != null) {
            polygon.setFill(CatanResource.newPattern(type.getTerrain()));
        }
        return polygon;
    }

    public static List<EdgeCatan> addTerrains(Pane root, List<SettlePoint> settlePoints, Collection<Terrain> terrains,
        List<Port> ports) {
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
                terrain.createSettlePoints(x, y, settlePoints);
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

    public static List<Integer> getNumbers() {
        List<Integer> numbers = IntStream.rangeClosed(2, 12)
            .flatMap(e -> IntStream.generate(() -> e).limit(getLimit(e))).boxed().collect(Collectors.toList());
        Collections.shuffle(numbers);
        return numbers;
    }

    public static List<SettlePoint> getSettlePoints(final double xOff, final double yOff) {
        List<SettlePoint> points = new ArrayList<>();
        double off = Math.PI / 6;
        for (int i = 0; i < 6; i++) {
            double d = Math.PI / 3;
            double x = Math.cos(off + d * i) * CatanResource.RADIUS + CatanResource.RADIUS;
            double y = Math.sin(off + d * i) * CatanResource.RADIUS + CatanResource.RADIUS;
            double centerX = xOff + x - CatanResource.RADIUS / 10.;
            double centerY = yOff + y;
            SettlePoint e = new SettlePoint();
            e.relocate(centerX, centerY);
            points.add(e);
            if (points.size() > 1) {
                e.addNeighbor(points.get(i - 1));
            }
            if (points.size() == 6) {
                e.addNeighbor(points.get(0));
            }
        }
        return points;
    }

    private static int getLimit(final int e) {
        if (e == 7) {
            return 0;
        }
        if (e == 2 || e == 12) {
            return 1;
        }
        return 2;
    }

    private static boolean intersects(final SettlePoint p, final SettlePoint e) {
        return e.getBoundsInParent().intersects(p.getBoundsInParent());
    }

}
