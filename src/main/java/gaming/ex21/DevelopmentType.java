package gaming.ex21;

public enum DevelopmentType {
	KNIGHT("knight.png"),
	ROAD_BUILDING("roadbuilding.png"),
	YEAR_OF_PLENTY("yearofplenty.png"),
	MONOPOLY("monopoly.png"),
	UNIVERSITY("university.png"),;
	private final String image;

	private DevelopmentType(final String image) {
		this.image = image;
	}

	public String getImage() {
		return image;
	}

}
