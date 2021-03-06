package gaming.ex21;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.beans.NamedArg;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import simplebuilder.SimpleTextBuilder;
import utils.fx.RotateUtils;

public class Port extends Group {

    private static final int DEFAULT_FONT = 12;
    private static final String BOAT_PNG = "boat.png";
    public static final double SIZE = CatanResource.RADIUS * 0.9;
    private final ResourceType type;
    private final ObservableList<SettlePoint> points = FXCollections.observableArrayList();
    private final IntegerProperty number = new SimpleIntegerProperty(2);

    private HBox status;

    public Port(@NamedArg("type") ResourceType type) {
        this.type = type;
        Group e = new Group();
        e.getChildren().add(newBoat());
        if (type != ResourceType.DESERT) {
            e.getChildren().add(newResource());
        } else {
            number.set(3);
            e.getChildren().add(newInterrogation());
        }
        e.getChildren().add(newNumberText());
        getChildren().add(e);
        setManaged(false);

        points.addListener(this::onElementsChange);

    }

    public int getNumber() {
        return number.get();
    }

    public List<SettlePoint> getPoints() {
        return points;
    }

    public HBox getStatus() {
        if (status == null) {
            Text text = new Text();
            text.textProperty().bind(number.asString());
            Node newResource = type != ResourceType.DESERT ? newResource() : newInterrogation();
            status = new HBox(text, newResource, new SimpleTextBuilder().text("->?").size(DEFAULT_FONT).build());
            status.managedProperty().bind(status.visibleProperty());
        }
        return status;
    }

    public ResourceType getType() {
        return type;
    }

    public IntegerProperty numberProperty() {
        return number;
    }

    private Text newNumberText() {
        final double layoutY = SIZE * 13. / 20;
        return new SimpleTextBuilder().size(DEFAULT_FONT).textAlignment(TextAlignment.CENTER)
                .text(number.asString().concat(":1"))
            .layoutX(SIZE * 2 / 5).layoutY(layoutY).build();
    }

    private ImageView newResource() {
        final double d = SIZE * 5. / 24;
        ImageView e = CatanResource.newImage(type.getPure(), SIZE / 4.);
        e.setLayoutX(SIZE * 2 / 5);
        e.setLayoutY(d);
        e.setPreserveRatio(true);
        return e;
    }

    private void onElementsChange(final Change<? extends SettlePoint> e) {
        while (e.next()) {
            List<? extends SettlePoint> addedSubList = e.getList();
            for (Node node : addedSubList) {
                Line e2 = new Line();
                e2.startXProperty().bind(node.layoutXProperty().subtract(layoutXProperty()));
                e2.startYProperty().bind(node.layoutYProperty().subtract(layoutYProperty()));
                e2.setEndX(SIZE / 2);
                e2.setEndY(SIZE / 2);

                getChildren().add(0, e2);
            }
        }
    }

    public static boolean containsPort(List<ResourceType> distinct, long totalCards, Collection<Port> ports2,
        ObjectProperty<PlayerColor> currentPlayer2) {
        long differentTypesNumber = distinct.size();
        if (differentTypesNumber != 1) {
            return false;
        }
        return ports2.stream()
            .anyMatch(p -> p.getNumber() == totalCards
                && (p.getType() == distinct.get(0) || p.getType() == ResourceType.DESERT) && p.getPoints().stream()
                    .anyMatch(s -> s.getElement() != null && s.getElement().getPlayer() == currentPlayer2.get()));
    }

    public static List<Port> getPorts() {
        return Stream.of(ResourceType.values())
            .flatMap(t -> Stream.generate(() -> t).limit(t == ResourceType.DESERT ? 4 : 1)).map(Port::new)
            .collect(Collectors.toList());
    }

    public static void relocatePorts(Collection<SettlePoint> settlePoints2, List<Port> ports2) {
        List<SettlePoint> s = settlePoints2.stream().collect(Collectors.toList());
        Collections.shuffle(s);
        List<List<SettlePoint>> portLocations = s.stream().filter(p -> p.getNeighbors().size() == 2)
            .flatMap(p0 -> p0.getNeighbors().stream().map(p1 -> Arrays.asList(p0, p1))).collect(Collectors.toList());
        for (int j = 0; j < ports2.size() && !portLocations.isEmpty(); j++) {
            Port port = ports2.get(j);
            List<SettlePoint> points = portLocations.remove(0);
            Optional<SettlePoint> first = points.stream().filter(l -> l.getNeighbors().size() == 3).findFirst();
            portLocations.removeIf(p -> p.contains(points.get(0)));
            portLocations.removeIf(p -> p.contains(points.get(1)));
            port.getPoints().addAll(points);
            if (first.isPresent()) {
                ObservableList<SettlePoint> neighbors = first.get().getNeighbors()
                    .filtered(p -> p.getNeighbors().size() == 2);
                double x = neighbors.stream().mapToDouble(SettlePoint::getLayoutX).average().orElse(0);
                double y = neighbors.stream().mapToDouble(SettlePoint::getLayoutY).average().orElse(0);
                port.relocate(x - CatanResource.RADIUS / 2., y - CatanResource.RADIUS / 2.);
			} else {
				final double radius = CatanResource.RADIUS * Math.sqrt(3) / 4;
				double x = points.stream().mapToDouble(SettlePoint::getLayoutX).average().orElse(0);
				double y = points.stream().mapToDouble(SettlePoint::getLayoutY).average().orElse(0);
				double angulo = Math.PI / 2 - RotateUtils.getAngle(radius * 3, radius * 3 * 9 / 10, x, y);
				double m = Math.sin(angulo) * radius;
				double n = Math.cos(angulo) * radius;
				port.relocate(x + m - CatanResource.RADIUS / 2., y + n - CatanResource.RADIUS / 2.);
            }
        }
    }private static ImageView newBoat() {
        return CatanResource.newImage(BOAT_PNG, SIZE);
    }

    private static Text newInterrogation() {
        Text e = new Text("?");
        e.setFont(Font.font(DEFAULT_FONT + 3));
        e.setLayoutX(SIZE / 2);
        e.setLayoutY(SIZE * 5. / DEFAULT_FONT);
        return e;
    }

}
