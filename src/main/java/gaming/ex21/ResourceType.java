package gaming.ex21;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import simplebuilder.SimpleToggleGroupBuilder;

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


    public static HBox createResourceChoices(Consumer<ResourceType> onSelect, HBox res) {
        SimpleToggleGroupBuilder group = new SimpleToggleGroupBuilder();
        for (ResourceType type : ResourceType.values()) {
            if (type.getPure() != null) {
                ImageView node = CatanResource.newImage(type.getPure(), 20);
                group.addToggle(node, type);
            }
        }
        res.getChildren().addAll(group.getTogglesAs(Node.class).toArray(new Node[0]));
        res.setVisible(false);
        res.managedProperty().bind(res.visibleProperty());
        group.onChange((ob, old, n) -> {
            if (n != null) {
                ResourceType selectedType = (ResourceType) n.getUserData();
                onSelect.accept(selectedType);
                group.select(null);
            }
        });
        return res;
    }

    public static List<ResourceType> createResources() {
        EnumMap<ResourceType, Integer> resourcesMap = new EnumMap<>(ResourceType.class);
        resourcesMap.put(ResourceType.DESERT, 1);
        resourcesMap.put(ResourceType.BRICK, 3);
        resourcesMap.put(ResourceType.ROCK, 3);
        resourcesMap.put(ResourceType.SHEEP, 4);
        resourcesMap.put(ResourceType.WHEAT, 4);
        resourcesMap.put(ResourceType.WOOD, 4);
        List<ResourceType> resourceTypes = resourcesMap.entrySet().stream()
            .flatMap(e -> Stream.generate(e::getKey).limit(e.getValue())).collect(Collectors.toList());
        Collections.shuffle(resourceTypes);
        return resourceTypes;
    }

    public static ResourceType[] getResources() {
        return new ResourceType[] { 
            ResourceType.BRICK, 
            ResourceType.ROCK, 
            ResourceType.SHEEP, 
            ResourceType.WHEAT,
            ResourceType.WOOD };
        }
}
