package gaming.ex21;

import java.util.List;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import utils.ResourceFXUtils;

public class Port extends Group {

    private static final double SIZE = Terrain.RADIUS * 0.9;
    private final ResourceType type;
	private final ObservableList<SettlePoint> points = FXCollections.observableArrayList();
    private final IntegerProperty number = new SimpleIntegerProperty(2);

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
    	return number.get()
    			;
    }

	public List<SettlePoint> getPoints() {
		return points;
	}

    public ResourceType getType() {
        return type;
    }

	public IntegerProperty numberProperty () {
        return number;
    }


    private ImageView newBoat() {
        ImageView boat = new ImageView(ResourceFXUtils.toExternalForm("catan/boat.png"));
        boat.setFitWidth(SIZE);
        boat.setPreserveRatio(true);
        return boat;
    }

    private Text newInterrogation() {
        Text e = new Text("?");
        e.setFont(Font.font(15));
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
		ImageView e = new ImageView(ResourceFXUtils.toExternalForm("catan/" + type.getPure()));
		e.setFitWidth(SIZE / 4.);
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

}
