package gaming.ex21;

import java.util.Random;

public enum DevelopmentType {
	KNIGHT("knight.png"),
	ROAD_BUILDING("roadbuilding.png"),
	YEAR_OF_PLENTY("yearofplenty.png"),
	MONOPOLY("monopoly.png"),
	UNIVERSITY("university.png"),;
	private static Random random = new Random();

	private final String image;

	private DevelopmentType(final String image) {
		this.image = image;
	}

	public String getImage() {
		return image;
	}

	public static DevelopmentType randomDevelopment() {
		DevelopmentType[] values = DevelopmentType.values();
		return values[random.nextInt(values.length)];
	}
}
