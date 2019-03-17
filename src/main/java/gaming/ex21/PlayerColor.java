package gaming.ex21;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
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

    public static <T> Map<PlayerColor, List<T>> newMapList() {
        Map<PlayerColor, List<T>> map = new EnumMap<>(PlayerColor.class);
        for (PlayerColor playerColor : PlayerColor.values()) {
            map.put(playerColor, new ArrayList<>());
        }
        return map;
    }
}
