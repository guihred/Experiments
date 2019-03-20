package gaming.ex21;

import static gaming.ex21.ResourceType.BRICK;
import static gaming.ex21.ResourceType.ROCK;
import static gaming.ex21.ResourceType.SHEEP;
import static gaming.ex21.ResourceType.WHEAT;
import static gaming.ex21.ResourceType.WOOD;

import java.util.Arrays;
import java.util.List;

public enum Combination {
    ROAD("road.png", WOOD, BRICK),
    VILLAGE("village.png", WHEAT, WOOD, SHEEP, BRICK),
    CITY("city.png", WHEAT, WHEAT, ROCK, ROCK, ROCK),
    DEVELOPMENT("development.png", WHEAT, ROCK, SHEEP);

    private final List<ResourceType> resources;
    private final String element;

    Combination(final String element, final ResourceType... type) {
	this.element = element;
	resources = Arrays.asList(type);
    }

    public String getElement() {
	return element;
    }

    public List<ResourceType> getResources() {
	return resources;
    }

}
