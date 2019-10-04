package gaming.ex21;

import graphs.entities.Edge;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javafx.animation.FadeTransition;
import javafx.beans.NamedArg;
import javafx.scene.Group;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.util.Duration;
import simplebuilder.SimpleFadeTransitionBuilder;

public class EdgeCatan extends Group {

    private final Set<SettlePoint> points = new LinkedHashSet<>();
    private final Line line = new Line(0, 0, 0, 0);
    private final FadeTransition highlightTransition = new SimpleFadeTransitionBuilder().node(line)
        .duration(Duration.millis(200)).fromValue(1).toValue(0).build();
    private CatanResource element;


    public EdgeCatan(@NamedArg("a") final SettlePoint a, @NamedArg("b") final SettlePoint b) {
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
    public boolean edgeAcceptRoad(final Road road) {
        return getElement() == null && (matchColor(road.getPlayer()) || getPoints().stream().anyMatch(p -> p.getEdges()
            .stream().anyMatch(e -> e.getElement() != null && e.getElement().getPlayer() == road.getPlayer())));
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        return ((EdgeCatan) obj).points.equals(points);
    }

    public EdgeCatan fadeIn(final boolean enable) {
        return toggleFade(-1, enable);
    }

    public EdgeCatan fadeOut(final boolean enable) {
        return toggleFade(1, enable);
    }

    public SettlePoint getA() {
        return points.stream().skip(0).findFirst().orElse(null);
    }

    public SettlePoint getB() {
        return points.stream().skip(1).findFirst().orElse(null);
    }

    public CatanResource getElement() {
        return element;
    }

    @Override
    public int hashCode() {
        return points.hashCode();
    }

    public boolean matchColor(final PlayerColor player) {
        return points.stream().anyMatch(e -> e.getElement() != null && e.getElement().getPlayer() == player);
    }

    public void setElement(final Road element) {
        StackPane parent = (StackPane) element.getParent();
        parent.getChildren().remove(element);
        getChildren().add(element);
        element.setLayoutX(-element.getImage().getWidth() / 2);
        element.setLayoutY(-element.getImage().getHeight() / 2);
        fadeOut(true);
        double angulo = Edge.getAngulo(line.getEndX(), line.getEndY(), line.getStartX(), line.getStartY());
        element.setRotate(Math.toDegrees(angulo) - 90);
        this.element = element;
    }

    @Override
    public String toString() {
        return "(" + points + ")";
    }

    Set<SettlePoint> getPoints() {
        return points;
    }

    private EdgeCatan toggleFade(final int r, final boolean enable) {
        line.setStroke(enable ? Color.GREEN : Color.RED);

        if (element == null) {
            highlightTransition.setRate(r);
            highlightTransition.play();
        }
        return this;
    }

    public static long countRoadSize(PlayerColor player, Collection<EdgeCatan> edges) {
        List<EdgeCatan> playersEdges = edges.stream().filter(e -> e.getElement() != null)
            .filter(e -> e.getElement().getPlayer() == player).collect(Collectors.toList());
        if (playersEdges.size() >= 5) {
            List<SettlePoint> collect2 = playersEdges.stream().flatMap(e -> e.getPoints().stream()).distinct()
                .collect(Collectors.toList());

            return collect2.stream().map(m -> dijkstra(m, collect2, playersEdges))
                .mapToInt(d -> d.values().stream().mapToInt(e -> e).filter(e -> e != Integer.MAX_VALUE).max().orElse(0))
                .max().orElse(0);
        }

        return 0;
    }

    public static boolean edgeAcceptRoad(final EdgeCatan edge, final Road road) {

        if (edge == null) {
            return false;
        }
        return edge.edgeAcceptRoad(road);
    }

    private static Collection<SettlePoint> adjacents(final SettlePoint v, final List<EdgeCatan> allEdges) {
        return allEdges.stream().filter(e -> e.getPoints().contains(v))
            .flatMap(e -> e.getPoints().stream().filter(p -> p != v)).collect(Collectors.toList());
    }

    private static Integer cost(final SettlePoint v, final SettlePoint w, final List<EdgeCatan> allEdges) {
        return (int) allEdges.stream().filter(e -> e.getPoints().contains(v) && e.getPoints().contains(w)).count();
    }

    private static Map<SettlePoint, Boolean> createDistanceMap(final SettlePoint source,
        final Map<SettlePoint, Integer> distance, final List<SettlePoint> allCells) {
        Map<SettlePoint, Boolean> known = new HashMap<>();
        for (SettlePoint v : allCells) {
            distance.put(v, Integer.MAX_VALUE);
            known.put(v, false);
        }
        distance.put(source, 0);
        return known;
    }

    private static Map<SettlePoint, Integer> dijkstra(final SettlePoint s, final List<SettlePoint> allSettlePoints,
        final List<EdgeCatan> allEdges) {
        Map<SettlePoint, Integer> distance = new HashMap<>();
        Map<SettlePoint, Boolean> known = createDistanceMap(s, distance, allSettlePoints);
        while (known.entrySet().stream().anyMatch(e -> !e.getValue())) {
            SettlePoint v = getMinDistanceSettlePoint(distance, known);
            known.put(v, true);
            for (SettlePoint w : adjacents(v, allEdges)) {
                if (!known.get(w)) {
                    Integer cvw = cost(v, w, allEdges);
                    if (distance.get(v) + cvw < distance.get(w)) {
                        int value = distance.get(v) + cvw;
                        distance.put(w, value);
                    }
                }
            }
        }
        return distance;
    }

    private static SettlePoint getMinDistanceSettlePoint(final Map<SettlePoint, Integer> distance,
        final Map<SettlePoint, Boolean> known) {
        return distance.entrySet().stream().filter(e -> !known.get(e.getKey()))
            .min(Comparator.comparing(Entry<SettlePoint, Integer>::getValue))
            .orElseThrow(() -> new RuntimeException("There should be someone")).getKey();
    }
}
