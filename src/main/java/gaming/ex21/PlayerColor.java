package gaming.ex21;

import java.util.*;
import javafx.scene.paint.Color;

public enum PlayerColor {
    GOLD(Color.GOLD),
    RED(Color.RED),
    BLUE(Color.BLUE),
	GREEN(Color.GREEN);
    private static final List<PlayerColor> vals = Arrays.asList(values());

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
    public static List<PlayerColor> vals() {
        Collections.shuffle(vals);
        return vals;
    }
}
