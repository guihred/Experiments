package gaming.ex21;

public enum ResourceType {
    WOOD("forest.png", "wood.png", "purewood.png"),
	BRICK("barrenarea.png", "bricks.png", "purebricks.png"),
    ROCK("mountain.png", "rock.png", "purerock.png"),
    SHEEP("grassfield.png", "sheep.png", "puresheep.png"),
    WHEAT("cropfield.png", "wheat.png", "purewheat.png"),
    DESERT("desert.png"),;
    private final String terrain;
    private final String resource;
    private String pure;

    ResourceType(final String terrain) {
        this.terrain = terrain;
        resource = null;
    }

    ResourceType(final String terrain, final String resource, final String pure) {
        this.terrain = terrain;
        this.resource = resource;
        this.pure = pure;
    }

    public String getPure() {
        return pure;
    }

    public String getResource() {
        return resource;
    }

    public String getTerrain() {
        return terrain;
    }

}
