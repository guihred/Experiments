package gaming.ex21;

import static gaming.ex21.CatanResource.newImage;
import static gaming.ex21.ResourceType.*;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableList;
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

    public boolean disableCombination(PlayerColor key, Map<PlayerColor, List<CatanCard>> cards,
        Collection<SettlePoint> settlePoints, Collection<EdgeCatan> edges,
        Collection<DevelopmentType> developmentCards) {
        List<CatanCard> list = cards.get(key);
        if (list == null) {
            return true;
        }
        if (!Combination.containsEnough(list, getResources())) {
            return true;
        }
        Map<Class<?>, Long> elementCount = settlePoints.stream()
            .filter(s -> s.getElement() != null && s.getElement().getPlayer() == key).map(SettlePoint::getElement)
            .collect(Collectors.groupingBy(CatanResource::getClass, Collectors.counting()));
        
        elementCount.putAll(edges.stream().filter(s -> s.getElement() != null && s.getElement().getPlayer() == key)
            .map(EdgeCatan::getElement).collect(Collectors.groupingBy(CatanResource::getClass, Collectors.counting())));
        
        switch (this) {
            case CITY:
                return elementCount.getOrDefault(City.class, 0L) >= 4
                    || settlePoints.stream().noneMatch(s -> s.acceptCity(key));
            case VILLAGE:
                return elementCount.getOrDefault(Village.class, 0L) >= 5
                    || settlePoints.stream().noneMatch(s -> s.acceptVillage(key));
            case ROAD:
                return elementCount.getOrDefault(Road.class, 0L) >= 15;
            case DEVELOPMENT:
            default:
                return developmentCards.isEmpty();
        }
    }

    public String getElement() {
        return element;
    }

    public List<ResourceType> getResources() {
        return resources;
    }

    public void onSelectCombination(PlayerColor currentPlayer2, List<CatanCard> currentCards,
        ObservableList<CatanResource> elements2, List<DevelopmentType> developmentCards2,
        Consumer<CatanCard> onCardSelect) {
        for (int i = 0; i < resources.size(); i++) {
            ResourceType r = resources.get(i);
            currentCards.remove(currentCards.stream().filter(e -> e.getResource() == r).findFirst().orElse(null));
        }
        if (VILLAGE == this) {
            elements2.add(new Village(currentPlayer2));
        } else if (CITY == this) {
            elements2.add(new City(currentPlayer2));
        } else if (ROAD == this) {
            elements2.add(new Road(currentPlayer2));
        } else if (DEVELOPMENT == this) {
            currentCards.add(new CatanCard(developmentCards2.remove(0), onCardSelect));
        }
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
            button.disableProperty()
                .bind(Bindings.createBooleanBinding(() -> isDisabled.test(combination), currentPlayer, diceThrown));
            value.addRow(i, button);
            for (ResourceType resourceType : resources) {
                value.addRow(i, newImage(resourceType.getPure(), 20));
            }
        }
    }

    public static boolean containsEnough(Collection<CatanCard> list, Collection<ResourceType> resourcesNeeded) {
        List<ResourceType> resources = list.stream().map(CatanCard::getResource).filter(Objects::nonNull)
            .collect(Collectors.toList());
        List<ResourceType> resourcesNecessary = resourcesNeeded.stream().collect(Collectors.toList());
        for (int i = 0; i < resourcesNecessary.size(); i++) {
            if (!resources.remove(resourcesNecessary.get(i))) {
                return false;
            }
        }
        return true;
    }

}
