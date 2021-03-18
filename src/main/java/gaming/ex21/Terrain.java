package gaming.ex21;

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
import javafx.scene.Parent;
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
    private final Circle circle =
            new SimpleCircleBuilder().radius(CatanResource.RADIUS / 5.).fill(Color.BEIGE).stroke(Color.BLACK).build();
    private final FillTransition highlightTransition = new SimpleFillTransitionBuilder().shape(circle)
            .duration(Duration.millis(200)).fromValue(Color.BEIGE).toValue(Color.GREEN).build();

    public Terrain(@NamedArg("type") ResourceType type) {
        this.type = type;
        circle.setVisible(type != ResourceType.DESERT);
        stack = new StackPane(getPolygon(), getCircle(), getNumberText());
        getChildren().add(stack);
        setManaged(false);
    }

    public Terrain fadeIn() {
        return toggleFade(1);
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

    private Terrain fadeOut() {
        return toggleFade(-1);
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

    public static List<Integer> getNumbers() {
        List<Integer> numbers = IntStream.rangeClosed(2, 12)
            .flatMap(e -> IntStream.generate(() -> e).limit(getLimit(e))).boxed().collect(Collectors.toList());
        Collections.shuffle(numbers);
        return numbers;
    }

    public static void replaceThief(Collection<Terrain> terrains2, Thief thief2,
        List<CatanResource> elements2, PlayerColor currentPlayer2) {
        terrains2.stream().filter(t -> t.getThief() != null).forEach(Terrain::fadeOut);
        Parent parent = thief2.getParent();
        if (parent instanceof Group) {
            ((Group) parent).getChildren().remove(thief2);
        }
        thief2.setPlayer(currentPlayer2);
        if (!elements2.contains(thief2)) {
            elements2.add(thief2);
        }
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

}
