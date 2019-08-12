package gaming.ex21;

import java.util.*;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableList;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

public enum PlayerColor {
	YELLOW(Color.GOLD),
    RED(Color.RED),
    BLUE(Color.BLUE),
	GREEN(Color.GREEN);
    private static final List<PlayerColor> vals = Arrays.asList(values());

    private final Color color;

    PlayerColor(final Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }

    public static boolean isSkippable(BooleanProperty diceThrown2, HBox resourceChoices2,
        ObservableList<CatanResource> elements2, ObjectProperty<PlayerColor> currentPlayer2) {
        return !diceThrown2.get() || resourceChoices2.isVisible()
            || elements2.stream().anyMatch(e -> e.getPlayer() == currentPlayer2.get());
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
