package gaming.ex14;

import javafx.scene.paint.Color;

public enum GhostColor {
    RED(Color.RED),
    BLUE(Color.BLUE),
    ORANGE(Color.ORANGE),
    GREEN(Color.GREEN);

	private final transient Color color;

    GhostColor(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }
}