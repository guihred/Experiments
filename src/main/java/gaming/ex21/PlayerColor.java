package gaming.ex21;

import javafx.scene.paint.Color;

public enum PlayerColor {
    GOLD(Color.GOLD),
    RED(Color.RED),
    BLUE(Color.BLUE),
	GREEN(Color.GREEN);
    private final Color color;

    private PlayerColor(final Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }
}
