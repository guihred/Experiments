package gaming.ex21;

import static gaming.ex21.CatanResource.newImage;
import static gaming.ex21.ResourceType.*;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import simplebuilder.SimpleButtonBuilder;

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

	public static void combinationGrid(GridPane value, Consumer<Combination> onClick, Predicate<Combination> isDisabled,
			ObjectProperty<PlayerColor> currentPlayer, BooleanProperty diceThrown) {
		Combination[] combinations = values();
		for (int i = 0; i < combinations.length; i++) {
			Combination combination = combinations[i];
			List<ResourceType> resources = combination.getResources();
			Button button = SimpleButtonBuilder.newButton(newImage(combination.getElement(), 30, 30), "" + combination,
					e -> onClick.accept(combination));
			button.setUserData(combination);
			button.disableProperty().bind(Bindings.createBooleanBinding(() -> isDisabled.test(combination),
					currentPlayer, diceThrown));
			value.addRow(i, button);
			for (ResourceType resourceType : resources) {
				value.addRow(i, newImage(resourceType.getPure(), 20));
			}
		}
	}

}
