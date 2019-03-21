package gaming.ex21;

import graphs.entities.Edge;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.beans.property.IntegerProperty;
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
import simplebuilder.SimpleTextBuilder;

public class Port extends Group {

    public static final double SIZE = Terrain.RADIUS * 0.9;
    private final ResourceType type;
    private final ObservableList<SettlePoint> points = FXCollections.observableArrayList();
    private final IntegerProperty number = new SimpleIntegerProperty(2);

    private HBox status;

    public Port(final ResourceType type) {
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
	    status = new HBox(text, newResource, new SimpleTextBuilder().text("->?").size(12).build());
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

    private Text newInterrogation() {
	Text e = new Text("?");
	e.setFont(Font.font(12));
	e.setLayoutX(SIZE * 5 / 10);
	e.setLayoutY(SIZE * 10 / 24.);
	return e;
    }

    private Text newNumberText() {
	Text e = new Text();
	e.setFont(Font.font(12));
	e.textProperty().bind(number.asString().concat(":1"));
	e.setLayoutX(SIZE / 2.5);
	e.setLayoutY(SIZE * 13 / 20.);
	return e;
    }

    private ImageView newResource() {
	ImageView e = CatanResource.newImage(type.getPure(), SIZE / 4.);
	e.setLayoutX(SIZE / 2.5);
	e.setLayoutY(SIZE * 5 / 24.);
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

    public static List<Port> getPorts() {
	return Stream.of(ResourceType.values())
		.flatMap(t -> Stream.generate(() -> t).limit(t == ResourceType.DESERT ? 4 : 1)).map(Port::new)
		.collect(Collectors.toList());
    }

    public static void relocatePorts(final List<SettlePoint> settlePoints2, final List<Port> ports2) {
	final double radius = Terrain.RADIUS * Math.sqrt(3) / 4;
	List<SettlePoint> s = settlePoints2.stream().collect(Collectors.toList());
	Collections.shuffle(s);
	List<List<SettlePoint>> portLocations = s.stream().filter(p -> p.getNeighbors().size() == 2)
		.flatMap(p0 -> p0.getNeighbors().stream().map(p1 -> Arrays.asList(p0, p1)))
		.collect(Collectors.toList());
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
		port.relocate(x - Terrain.RADIUS / 2., y - Terrain.RADIUS / 2.);
	    } else {
		double x = points.stream().mapToDouble(SettlePoint::getLayoutX).average().orElse(0);
		double y = points.stream().mapToDouble(SettlePoint::getLayoutY).average().orElse(0);
		double angulo = Math.PI / 2 - Edge.getAngulo(radius * 3, radius * 2.7, x, y);
		double m = Math.sin(angulo) * radius;
		double n = Math.cos(angulo) * radius;
		port.relocate(x + m - Terrain.RADIUS / 2., y + n - Terrain.RADIUS / 2.);
	    }
	}
    }

    private static ImageView newBoat() {
	return CatanResource.newImage("boat.png", SIZE);
    }

}
