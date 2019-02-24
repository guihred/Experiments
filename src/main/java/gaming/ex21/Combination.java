package gaming.ex21;

import static gaming.ex21.ResourceType.*;

import java.util.Arrays;
import java.util.List;

public enum Combination {
    ROAD(WOOD, BRICK),
    VILLAGE(WHEAT, WOOD, SHEEP, BRICK),
    CITY(WHEAT, WHEAT, ROCK, ROCK, ROCK),
    DEVELOPMENT(WHEAT, ROCK, SHEEP),
    ;
    private final List<ResourceType> resources;

    Combination(ResourceType... type) {
        resources = Arrays.asList(type);
    }

    public List<ResourceType> getResources() {
        return resources;
    }

}
