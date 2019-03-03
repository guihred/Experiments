package gaming.ex21;

import javafx.animation.FillTransition;
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
import javafx.util.Duration;
import simplebuilder.SimpleCircleBuilder;
import simplebuilder.SimpleFillTransitionBuilder;
import utils.ResourceFXUtils;

public class Terrain extends Group {

    public static final int RADIUS = 70;
    private Thief thief;
    private final ResourceType type;
    private final IntegerProperty number = new SimpleIntegerProperty();
    private final StackPane stack;
    private final Circle circle;
    private final FillTransition highlightTransition;

    public Terrain(final ResourceType type) {
        this.type = type;
        circle = new SimpleCircleBuilder().radius(RADIUS / 5.).fill(Color.BEIGE).visible(type != ResourceType.DESERT)
                .stroke(Color.BLACK).build();
        stack = new StackPane(getPolygon(), getCircle(), getNumberText());
        getChildren().add(stack);
        highlightTransition = new SimpleFillTransitionBuilder().shape(circle).duration(Duration.millis(200))
                .fromValue(Color.BEIGE).toValue(Color.GREEN).build();
        setManaged(false);
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
            thief.setLayoutX(Terrain.RADIUS * Math.sqrt(3) / 2 - Terrain.RADIUS / 4.);
            thief.setLayoutY(Terrain.RADIUS - Terrain.RADIUS / 4.);
            highlightTransition.setToValue(Color.RED);
        } else {
            if (highlightTransition.getToValue().equals(Color.RED)) {
                toggleFade(-1);
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
            double x = Math.cos(off + d * i) * RADIUS;
            double y = Math.sin(off + d * i) * RADIUS;
            polygon.getPoints().addAll(x, y);
        }
        polygon.setFill(new ImagePattern(new Image(ResourceFXUtils.toExternalForm("catan/" + type.getTerrain()))));
        return polygon;
    }

}
