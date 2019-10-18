package gaming.ex22;

import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import simplebuilder.SimpleSvgPathBuilder;

public enum FreeCellSuit {
    SPADES(Color.BLACK, "M 60,0 a-20,20,0,1,1,34.6,20 a20,20,0,1,0,34.6,-20 l-34.6,-40 z M 85,48 l20,0  l-10,-35 z "),
    DIAMONDS(Color.RED, "M 60,0 l34.6,40 l-34.6,40 l-34.6,-40 z "),
    CLUBS(Color.BLACK,
        "M 60,0 a-20,20,0,1,1,16,27.7 a20,20,0,1,0,16,-27.7 a20,20,0,1,0,-32,0 z M 61,60 l30,0  l-15,-35 z "),
    HEARTS(Color.RED, "M 60,0 a-20,-20,0,1,1,34.6,-20 a20,-20,0,1,0,34.6,20 l-34.6,40 z ");

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
