package gaming.ex21;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum DevelopmentType {
    KNIGHT("knight.png", 14),
    ROAD_BUILDING("roadbuilding.png", 2),
    YEAR_OF_PLENTY("yearofplenty.png", 2),
    MONOPOLY("monopoly.png", 2),
    UNIVERSITY("university.png", 5),;

	private final String image;
    private final int amount;

    private DevelopmentType(final String image, int amount) {
		this.image = image;
        this.amount = amount;
	}

	public String getImage() {
		return image;
	}

	public static List<DevelopmentType> getDevelopmentCards() {
        List<DevelopmentType> developments = Stream.of(DevelopmentType.values())
                .flatMap(t -> Stream.generate(() -> t).limit(t.amount)).collect(Collectors.toList());
        Collections.shuffle(developments);
        return developments;
    }

}
