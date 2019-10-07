package gaming.ex21;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.collections.ObservableList;

public enum DevelopmentType {
    KNIGHT("knight.png", 14),
    ROAD_BUILDING("roadbuilding.png", 2),
    YEAR_OF_PLENTY("yearofplenty.png", 2),
    MONOPOLY("monopoly.png", 2),
    UNIVERSITY("university.png", 5),;

	private final String image;
    private final int amount;

    DevelopmentType(final String image, int amount) {
		this.image = image;
        this.amount = amount;
	}

	public String getImage() {
		return image;
	}

    public void onSelect(Collection<Terrain> terrains, Thief thief, ObservableList<CatanResource> elements,
        PlayerColor player, Consumer<SelectResourceType> onSelect) {
        switch (this) {
            case KNIGHT:
                Terrain.replaceThief(terrains, thief, elements, player);
                break;
            case MONOPOLY:
                onSelect.accept(SelectResourceType.MONOPOLY);
                break;
            case ROAD_BUILDING:
                elements.add(new Road(player));
                elements.add(new Road(player));
                break;
            case UNIVERSITY:
                break;
            case YEAR_OF_PLENTY:
                onSelect.accept(SelectResourceType.YEAR_OF_PLENTY);
                break;
            default:
                break;
        }
    }

	public static List<DevelopmentType> getDevelopmentCards() {
        List<DevelopmentType> developments = Stream.of(DevelopmentType.values())
                .flatMap(t -> Stream.generate(() -> t).limit(t.amount)).collect(Collectors.toList());
        Collections.shuffle(developments);
        return developments;
    }

}
