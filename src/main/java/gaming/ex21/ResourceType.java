package gaming.ex21;

public enum ResourceType {
    WOOD("forest.png", "wood.png"),
    BRICK("barrenarea.png", "brick.png"),
    ROCK("mountain.png", "rock.png"),
    SHEEP("grassfield.png", "sheep.png"),
    WHEAT("cropfield.png", "wheat.png"),
    DESERT("desert.png", null),;
    private final String terrain;
    private final String resource;

    ResourceType(String terrain, String resource) {
        this.terrain = terrain;
        this.resource = resource;
    }

    public String getResource() {
        return resource;
    }

    public String getTerrain() {
        return terrain;
    }
}
