package gaming.ex21;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.Group;
import javafx.scene.image.ImageView;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import utils.ResourceFXUtils;

public class Port extends Group {

    private static final double SIZE = Terrain.RADIUS * 0.9;
    private final ResourceType type;
    private final IntegerProperty number = new SimpleIntegerProperty(2);

    public Port(final ResourceType type) {
        this.type = type;
        Group e = new Group();
        e.getChildren().add(newBoat());
        if (type != ResourceType.DESERT) {
            e.getChildren().add(newResource());
        } else {
            e.getChildren().add(newInterrogation());
        }
        e.getChildren().add(newNumberText());
        getChildren().add(e);
        setManaged(false);
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

}
