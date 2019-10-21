package gaming.ex22;

import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import simplebuilder.SimpleSvgPathBuilder;

public enum FreeCellSuit {
    SPADES(Color.BLACK,
        "m-5 10 a -5 5 0 1 0 8.65 5.00a 5 5 0 1 0 8.65 -5.00l-8.65 -10 zm6.25 12 l5 0 l-2.52 -8.75 z"),
    DIAMONDS(Color.RED, "m5 0 l8.65 10 l-8.65 10 l-8.65 -10 z"),
    CLUBS(Color.BLACK,
        "m0 10 a -5 5 0 1 0 4 6.93a 5 5 0 1 0 4 -6.93a 5 5 0 1 0 -8 0.00zm0.25 15 l7.49 0 l-3.75 -8.75 z"),
    HEARTS(Color.RED, "m-5 10 a -5 -5 0 1 1 8.65 -5.00a 5 -5 0 1 1 8.65 5.00l-8.65 10 z");

    private final String resource;
    private final Color color;

    FreeCellSuit(Color color, String resource) {
        this.color = color;
        this.resource = resource;

    }

    public Color getColor() {
        return color;
    }


    public String getResource() {
        return resource;
    }
    public SVGPath getShape() {
        return new SimpleSvgPathBuilder().content(resource).fill(color).build();
    }
}
