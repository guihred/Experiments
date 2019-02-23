package gaming.ex21;

import javafx.scene.paint.Color;

public enum PlayerColor {
    GOLD(Color.GOLD),
    RED(Color.RED),
    BLUE(Color.BLUE),
    BEIGE(Color.BEIGE);
    private final Color color;

    private PlayerColor(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }
}
