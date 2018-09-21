package ml.data;

import javafx.scene.paint.Color;

public enum Continent {
    AFRICA(Color.RED),
    ANTARCTICA(Color.WHITE),
    ASIA(Color.YELLOW),
    EUROPE(Color.CYAN),
    NORTH_AMERICA(Color.ORANGE),
    OCEANIA(Color.GREEN),
    SOUTH_AMERICA(Color.PINK);

    private final Color color;

    Continent(Color color) {
        this.color = color;

    }

    public Color getColor() {
        return color;
    }

}
