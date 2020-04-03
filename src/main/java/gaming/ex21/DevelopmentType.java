package gaming.ex21;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum DevelopmentType {
    KNIGHT("knight.png", 14) {
        @Override
        public void onSelect(Collection<Terrain> terrains, Thief thief, List<CatanResource> elements,
                PlayerColor player, Consumer<SelectResourceType> onSelect) {
            Terrain.replaceThief(terrains, thief, elements, player);
        }
    },
    ROAD_BUILDING("roadbuilding.png", 2) {
        @Override
        public void onSelect(Collection<Terrain> terrains, Thief thief, List<CatanResource> elements,
                PlayerColor player, Consumer<SelectResourceType> onSelect) {
            elements.add(new Road(player));
            elements.add(new Road(player));
        }
    },
    YEAR_OF_PLENTY("yearofplenty.png", 2) {
        @Override
        public void onSelect(Collection<Terrain> terrains, Thief thief, List<CatanResource> elements,
                PlayerColor player, Consumer<SelectResourceType> onSelect) {
            onSelect.accept(SelectResourceType.YEAR_OF_PLENTY);
        }
    },
    MONOPOLY("monopoly.png", 2) {
        @Override
        public void onSelect(Collection<Terrain> terrains, Thief thief, List<CatanResource> elements,
                PlayerColor player, Consumer<SelectResourceType> onSelect) {
            onSelect.accept(SelectResourceType.MONOPOLY);
        }
    },
    UNIVERSITY("university.png", 5) {
        @Override
        public void onSelect(Collection<Terrain> terrains, Thief thief, List<CatanResource> elements,
                PlayerColor player, Consumer<SelectResourceType> onSelect) {
            // Does Nothing
        }
    };

	private final String image;
    private final int amount;

    DevelopmentType(final String image, int amount) {
		this.image = image;
        this.amount = amount;
	}

	public String getImage() {
		return image;
	}

    public abstract void onSelect(Collection<Terrain> terrains, Thief thief, List<CatanResource> elements,
            PlayerColor player, Consumer<SelectResourceType> onSelect);

	public static List<DevelopmentType> getDevelopmentCards() {
        List<DevelopmentType> developments = Stream.of(DevelopmentType.values())
                .flatMap(t -> Stream.generate(() -> t).limit(t.amount)).collect(Collectors.toList());
        Collections.shuffle(developments);
        return developments;
    }

}
