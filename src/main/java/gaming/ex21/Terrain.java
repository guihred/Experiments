package gaming.ex21;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import utils.ResourceFXUtils;

public class Terrain extends Group {

    public static final int RADIUS = 70;
    private final ResourceType type;
    private final IntegerProperty number = new SimpleIntegerProperty();

    public Terrain(final ResourceType type) {
        this.type = type;
        getChildren().add(new StackPane(getPolygon(), getCircle(), getNumberText()));
        setManaged(false);
    }

    public int getNumber() {
		return number.get();
	}

    public ResourceType getType() {
        return type;
    }

	public void setNumber(final int number) {
		this.number.set(number);
	}
    private Circle getCircle() {
        Circle e2 = new Circle(RADIUS / 5, Color.BEIGE);
        e2.setStroke(Color.BLACK);
        e2.setVisible(type != ResourceType.DESERT);
        return e2;
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
            double x = Math.cos(off + d * i) * RADIUS;
            double y = Math.sin(off + d * i) * RADIUS;
            polygon.getPoints().addAll(x, y);
        }
        polygon.setFill(new ImagePattern(new Image(ResourceFXUtils.toExternalForm("catan/" + type.getTerrain()))));
        return polygon;
    }

}
