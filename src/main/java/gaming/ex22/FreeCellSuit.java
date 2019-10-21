package gaming.ex22;

import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import simplebuilder.SimpleSvgPathBuilder;

public enum FreeCellSuit {
    SPADES(Color.BLACK, "m0 10a-5 5 0 1 0 8.65 5a5 5 0 1 0 8.65 -5l-8.65 -10zm6.25 12l5 0l-2.5 -8.75z"),
    DIAMONDS(Color.RED, "m10 0l8.65 10l-8.65 10l-8.65 -10z"),
    CLUBS(Color.BLACK, "m5 10a-5 5 0 1 0 4 7a5 5 0 1 0 4 -7a5 5 0 1 0 -8 0zm0.25 15l7.5 0l-3.75 -8.75z"),
    HEARTS(Color.RED, "m0 10a-5 -5 0 1 1 8.65 -5a5 -5 0 1 1 8.65 5l-8.65 10z");

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
