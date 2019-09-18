package gaming.ex21;

import static gaming.ex21.CatanCard.inArea;
import static gaming.ex21.CatanResource.newImage;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import simplebuilder.SimpleButtonBuilder;

public class CatanModel {
    protected final List<Terrain> terrains = new ArrayList<>();
    protected final List<SettlePoint> settlePoints = new ArrayList<>();
    protected final List<EdgeCatan> edges;
    protected final Map<PlayerColor, List<CatanCard>> cards = PlayerColor.newMapList();
    protected final Map<PlayerColor, List<DevelopmentType>> usedCards = PlayerColor.newMapList();
    protected final ObjectProperty<PlayerColor> currentPlayer = new SimpleObjectProperty<>();
    protected final ObservableList<CatanResource> elements = FXCollections.observableArrayList();
    private final DragContext dragContext = new DragContext();
    protected final BooleanProperty diceThrown = new SimpleBooleanProperty(false);
    private final Pane center;
    protected SelectResourceType resourcesToSelect = SelectResourceType.DEFAULT;
    private int turnCount;
    private final HBox resourceChoices = ResourceType.createResourceChoices(this::onSelectResource);
    private final Button exchangeButton = SimpleButtonBuilder.newButton("Exchange", e -> setResourceSelect(SelectResourceType.EXCHANGE));
    private final Button makeDeal = SimpleButtonBuilder.newButton("Make Deal", e -> setResourceSelect(SelectResourceType.MAKE_DEAL));
    protected final ObservableList<Deal> deals = FXCollections.observableArrayList();
    protected final Thief thief = new Thief();
    protected final List<Port> ports = Port.getPorts();
    protected final List<DevelopmentType> developmentCards = DevelopmentType.getDevelopmentCards();
    private final UserChart userChart = new UserChart();

    public CatanModel(Pane center, Pane right) {
        this.center = center;
        edges = Terrain.addTerrains(center, settlePoints, terrains, ports);
        center.setOnMousePressed(this::handleMousePressed);
        center.setOnMouseDragged(this::handleMouseDragged);
        center.setOnMouseReleased(this::handleMouseReleased);
        elements.addListener(ListHelper.onChangeElement(center));
        currentPlayer.addListener((ob, old, newV) -> onChangePlayer(newV));
        right.getChildren().add(userChart);
        Button skipButton = SimpleButtonBuilder.newButton("Skip Turn", e -> onSkipTurn());
        skipButton.disableProperty().bind(Bindings.createBooleanBinding(this::isSkippable, diceThrown,
            resourceChoices.visibleProperty(), currentPlayer, elements));
        Button throwButton = SimpleButtonBuilder.newButton("Throw Dices", e -> throwDice());
        throwButton.disableProperty().bind(diceThrown);
        VBox dealsBox = ListHelper.newDeal(deals, t -> Deal.isDealUnfeasible(t, currentPlayer, cards), this::onMakeDeal,
            currentPlayer, diceThrown);
        right.getChildren().add(new HBox(new VBox(skipButton, throwButton, exchangeButton, makeDeal), dealsBox));

        makeDeal.setDisable(true);
        right.getChildren().add(resourceChoices);
        right.getChildren().add(addCombinations());
        currentPlayer.set(PlayerColor.BLUE);
        onSkipTurn();
        userChart.setOnWin(CatanModel::create);
    }

    public boolean anyPlayerPoints(int i, CatanModel model) {
        return PlayerColor.vals().stream()
            .mapToLong(e -> userChart.countPoints(e, model.settlePoints, model.usedCards, model.edges)).max()
            .orElse(0) >= i;
    }

    public PlayerColor getCurrentPlayer() {
        return currentPlayer.get();
    }

    public ObservableList<CatanResource> getElements() {
        return elements;
    }

    public PlayerColor getPlayerWinner() {
        return PlayerColor.vals().stream()
            .max(Comparator.comparing((PlayerColor e) -> getUserChart().countPoints(e, settlePoints, usedCards, edges))
                .thenComparing((PlayerColor e) -> cards.get(e).size()))
            .orElse(getUserChart().getColor());
    }

    public UserChart getUserChart() {
        return userChart;
    }

    public boolean isDealUnfeasible(Deal deal) {
        return Deal.isDealUnfeasible(deal, currentPlayer, cards);
    }
    private Node addCombinations() {
        GridPane value = new GridPane();
        Combination[] combinations = Combination.values();
        for (int i = 0; i < combinations.length; i++) {
            Combination combination = combinations[i];
            List<ResourceType> resources = combination.getResources();
            Button button = SimpleButtonBuilder.newButton(newImage(combination.getElement(), 30, 30), "" + combination, e -> onCombinationClicked(combination));
            button.disableProperty()
                .bind(Bindings.createBooleanBinding(() -> disableCombination(combination), currentPlayer, diceThrown));
            value.addRow(i, button);
            for (ResourceType resourceType : resources) {
                value.addRow(i, newImage(resourceType.getPure(), 20));
            }
        }
        return value;
    }

    private boolean disableCombination(Combination combination) {
        PlayerColor key = currentPlayer.get();
        List<CatanCard> list = cards.get(key);
        if (list == null) {
            return true;
        }
        if (!CatanCard.containsEnough(list, combination.getResources())) {
            return true;
        }
        Map<Class<?>, Long> elementCount = settlePoints.stream()
            .filter(s -> s.getElement() != null && s.getElement().getPlayer() == key).map(SettlePoint::getElement)
            .collect(Collectors.groupingBy(CatanResource::getClass, Collectors.counting()));

        elementCount.putAll(edges.stream().filter(s -> s.getElement() != null && s.getElement().getPlayer() == key)
            .map(EdgeCatan::getElement).collect(Collectors.groupingBy(CatanResource::getClass, Collectors.counting())));
        if (combination == Combination.CITY) {
            return elementCount.getOrDefault(City.class, 0L) >= 4
                || settlePoints.stream().noneMatch(s -> s.acceptCity(key));
        }
        if (combination == Combination.VILLAGE) {
            return elementCount.getOrDefault(Village.class, 0L) >= 5
                || settlePoints.stream().noneMatch(s -> s.acceptVillage(key));
        }
        if (combination == Combination.ROAD) {
            return elementCount.getOrDefault(Road.class, 0L) >= 15;
        }

        return developmentCards.isEmpty();
    }

    private int getDirection() {
        if (turnCount == 4) {
            return 0;
        } else if (turnCount > 4 && turnCount < 8) {
            return -1;
        } else {
            return 1;
        }
    }

    private void handleMouseDragged(MouseEvent event) {
        double offsetX = event.getX() + dragContext.getX();
        double offsetY = event.getY() + dragContext.getY();
        if (dragContext.getElement() != null) {
            CatanResource c = dragContext.getElement();
            c.relocate(offsetX, offsetY);
            dragContext.pointFadeOut();
            dragContext.toggleTerrain(-1);
            if (dragContext.getElement() instanceof Village) {
                settlePoints.stream().filter(e -> inArea(event, e)).findFirst()
                    .ifPresent(e -> dragContext.setPoint(e.fadeIn()));
            }
            if (dragContext.getElement() instanceof City) {
                settlePoints.stream().filter(e -> inArea(event, e))
                    .filter(e -> e.isSuitableForCity((City) dragContext.getElement())).findFirst()
                    .ifPresent(e -> dragContext.setPoint(e.fadeOut()));
            }
            if (dragContext.getElement() instanceof Thief) {
                terrains.stream().filter(e -> inArea(event, e)).findFirst()
                    .ifPresent(e -> dragContext.setTerrain(e.fadeIn()));
            }
            if (dragContext.getElement() instanceof Road) {
                Road element = (Road) dragContext.getElement();
                dragContext.edgeFadeOut(EdgeCatan.edgeAcceptRoad(dragContext.getEdge(), element));
                edges.stream().filter(e -> inArea(event, e)).findFirst()
                    .ifPresent(e -> dragContext.setEdge(e.fadeIn(EdgeCatan.edgeAcceptRoad(e, element))));
            }

        }
    }

    private void handleMousePressed(MouseEvent event) {
        Optional<Node> resourcePressed = center.getChildren().parallelStream()
            .filter(e -> e.getBoundsInParent().contains(event.getX(), event.getY())).findFirst();
        if (resourcePressed.isPresent()) {
            Node node = resourcePressed.get();
            dragContext.setX(node.getBoundsInParent().getMinX() - event.getX());
            dragContext.setY(node.getBoundsInParent().getMinY() - event.getY());
            if (node instanceof CatanResource && elements.contains(node)) {
                dragContext.setElement((CatanResource) node);
                elements.remove(node);
            }
        }
    }

    private void handleMouseReleased(MouseEvent event) {
        if (dragContext.getElement() instanceof Village) {
            onReleaseVillage(event, (Village) dragContext.getElement());
        }
        if (dragContext.getElement() instanceof City) {
            onReleaseCity(event, (City) dragContext.getElement());
        }
        if (dragContext.getElement() instanceof Road) {
            onReleaseRoad(event, (Road) dragContext.getElement());
        }
        if (dragContext.getElement() instanceof Thief) {
            onReleaseThief(event);
        }
        updatePoints(currentPlayer.get());
    }

    private void invalidateDice() {
        diceThrown.set(!diceThrown.get());
        diceThrown.set(!diceThrown.get());
    }

    private boolean isPositioningPhase() {
        return turnCount <= 8;
    }

    private boolean isSkippable() {
        return PlayerColor.isSkippable(diceThrown, resourceChoices, elements, currentPlayer);
    }

    private void makeDealButton(ResourceType selectedType) {
        List<ResourceType> dealTypes = cards.get(currentPlayer.get()).stream().filter(e -> e.getResource() != null)
            .filter(CatanCard::isSelected).filter(e -> e.getResource() != selectedType).map(CatanCard::getResource)
            .collect(Collectors.toList());
        if (!dealTypes.isEmpty()) {
            PlayerColor proposer = currentPlayer.get();
            deals.add(new Deal(proposer, selectedType, dealTypes));
            CatanLogger.log(this, CatanAction.MAKE_DEAL);
        }
        resourceChoices.setVisible(false);
        makeDeal.setDisable(true);
        resourcesToSelect = SelectResourceType.DEFAULT;
    }

    private void monopolyOfResource(ResourceType selectedType) {
        List<CatanCard> cardsTransfered = new ArrayList<>();
        PlayerColor[] values = PlayerColor.values();
        for (PlayerColor color : values) {
            List<CatanCard> collect2 = cards.get(color).stream().filter(e -> e.getResource() == selectedType)
                .collect(Collectors.toList());
            cards.get(color).removeAll(collect2);
            cardsTransfered.addAll(collect2);
        }
        cards.get(currentPlayer.get()).addAll(cardsTransfered);
        resourcesToSelect = SelectResourceType.DEFAULT;
        resourceChoices.setVisible(false);
    }

    private void onChangePlayer(PlayerColor newV) {
        updatePoints(newV);
        getUserChart().setColor(newV);
        List<CatanCard> currentCards = cards.get(currentPlayer.get());
        getUserChart().setCards(currentCards);
    }

    private void onCombinationClicked(Combination combination) {
        List<CatanCard> currentCards = cards.get(currentPlayer.get());
        if (CatanCard.containsEnough(currentCards, combination.getResources())) {
            List<ResourceType> resources = combination.getResources().stream().collect(Collectors.toList());
            for (int i = 0; i < resources.size(); i++) {
                ResourceType r = resources.get(i);
                currentCards.remove(currentCards.stream().filter(e -> e.getResource() == r).findFirst().orElse(null));
            }
            if (Combination.VILLAGE == combination) {
                elements.add(new Village(currentPlayer.get()));
            } else if (Combination.CITY == combination) {
                elements.add(new City(currentPlayer.get()));
            } else if (Combination.ROAD == combination) {
                elements.add(new Road(currentPlayer.get()));
            } else if (Combination.DEVELOPMENT == combination) {
                currentCards.add(new CatanCard(developmentCards.remove(0), this::onSelectCard));
            }
            CatanLogger.log(this, combination);
        }
        invalidateDice();
        onChangePlayer(currentPlayer.get());
        currentCards.forEach(e -> e.setSelected(true));
        currentCards.forEach(this::onSelectCard);
    }

    private void onMakeDeal(Deal deal) {
        List<CatanCard> listProposer = cards.get(deal.getProposer());
        List<CatanCard> list = cards.get(currentPlayer.get());
        ResourceType wantedType = deal.getWantedType();
        Optional<CatanCard> currentUserCard = list.stream().filter(e -> e.getResource() == wantedType).findFirst();
        if (currentUserCard.isPresent()) {
            CatanCard catanCard = currentUserCard.get();
            List<ResourceType> dealTypes = deal.getDealTypes();
            List<CatanCard> cardsGiven = new ArrayList<>();
            for (ResourceType resourceType : dealTypes) {
                Optional<CatanCard> first = listProposer.stream().filter(c -> !cardsGiven.contains(c))
                    .filter(c -> c.getResource() == resourceType).findFirst();
                if (!first.isPresent()) {
                    return;
                }
                cardsGiven.add(first.get());
            }
            list.remove(catanCard);
            listProposer.add(catanCard);
            list.addAll(cardsGiven);
            listProposer.removeAll(cardsGiven);
            deals.remove(deal);
            CatanLogger.log(this, CatanAction.ACCEPT_DEAL);
        }
        onChangePlayer(currentPlayer.get());
        invalidateDice();
    }

    private void onReleaseCity(MouseEvent event, City element) {
        Optional<SettlePoint> findFirst = settlePoints.stream().filter(e -> inArea(event, e))
            .filter(e -> e.isSuitableForCity(element)).findFirst();
        if (findFirst.isPresent()) {
            findFirst.get().setElement(element);
            CatanLogger.log(this, CatanAction.PLACE_CITY);
        } else {
            elements.add(0, dragContext.getElement());
        }
        dragContext.pointFadeOut();
        dragContext.setElement(null);
    }

    private void onReleaseRoad(MouseEvent event, Road road) {
        Optional<EdgeCatan> edgeHovered = edges.stream().filter(e -> inArea(event, e))
            .filter(e -> EdgeCatan.edgeAcceptRoad(e, road)).findFirst();
        if (edgeHovered.isPresent()) {
            edgeHovered.get().setElement(road);
            CatanLogger.log(this, CatanAction.PLACE_ROAD);
        } else {
            elements.add(0, dragContext.getElement());
        }
        dragContext.edgeFadeOut(EdgeCatan.edgeAcceptRoad(edgeHovered.orElse(null), road));
        dragContext.setElement(null);
    }

    private void onReleaseThief(MouseEvent event) {
        Optional<Terrain> edgeHovered = terrains.stream().filter(e -> inArea(event, e))
            .filter(e -> e.getThief() == null).findFirst();
        if (edgeHovered.isPresent()) {
            terrains.forEach(t -> t.setThief(null));
            Terrain terrain = edgeHovered.get();
            terrain.setThief(thief);
            stealResource(terrain);
            elements.removeIf(e -> e == thief);
        } else {
            elements.add(0, dragContext.getElement());
        }
        dragContext.toggleTerrain(1);
        dragContext.setElement(null);
    }

    private void onReleaseVillage(MouseEvent event, Village village) {
        Optional<SettlePoint> findFirst = settlePoints.stream().filter(e -> inArea(event, e))
            .filter(t -> !t.isPointDisabled()).filter(t -> isPositioningPhase() || t.pointAcceptVillage(village))
            .findFirst();
        if (findFirst.isPresent()) {
            findFirst.get().setElement(village);
            CatanLogger.log(this, CatanAction.PLACE_VILLAGE);
        } else {
            elements.add(0, dragContext.getElement());
        }
        dragContext.pointFadeOut();
        dragContext.setElement(null);
    }

    private void onSelectCard(CatanCard catanCard) {
        if (resourcesToSelect != SelectResourceType.DEFAULT) {
            return;
        }

        catanCard.setSelected(!catanCard.isSelected());
        long totalCards = cards.get(currentPlayer.get()).stream().filter(CatanCard::isSelected).count();
        List<ResourceType> distinct = cards.get(currentPlayer.get()).stream().filter(CatanCard::isSelected)
            .filter(e -> e.getResource() != null).map(CatanCard::getResource).distinct().collect(Collectors.toList());
        boolean containsPort = Port.containsPort(distinct, totalCards, ports, currentPlayer);
        long distinctCount = distinct.size();
        exchangeButton.setDisable((totalCards != 4 || distinctCount != 1) && !containsPort);
        DevelopmentType development = catanCard.getDevelopment();
        if (catanCard.isSelected()) {
            if (development != null) {
                onSelectDevelopment(catanCard, development);
            }
            CatanLogger.log(this, catanCard);
        }
        makeDeal.setDisable(
            distinctCount == 0 || deals.stream().filter(e -> e.getProposer() == getCurrentPlayer()).count() > 4);
    }

    private void onSelectDevelopment(CatanCard catanCard, DevelopmentType development) {
        cards.get(currentPlayer.get()).remove(catanCard);
        usedCards.get(currentPlayer.get()).add(development);
        switch (development) {
            case KNIGHT:
                replaceThief();
                break;
            case MONOPOLY:
                setResourceSelect(SelectResourceType.MONOPOLY);
                break;
            case ROAD_BUILDING:
                elements.add(new Road(currentPlayer.get()));
                elements.add(new Road(currentPlayer.get()));
                break;
            case UNIVERSITY:
                break;
            case YEAR_OF_PLENTY:
                setResourceSelect(SelectResourceType.YEAR_OF_PLENTY);
                break;
            default:
                break;
        }
        onChangePlayer(currentPlayer.get());
        invalidateDice();
    }

    private void onSelectResource(ResourceType selectedType) {
        if (resourcesToSelect == SelectResourceType.MAKE_DEAL) {
            makeDealButton(selectedType);
        } else if (resourcesToSelect == SelectResourceType.MONOPOLY) {
            monopolyOfResource(selectedType);
        } else if (resourcesToSelect == SelectResourceType.YEAR_OF_PLENTY) {
            cards.get(currentPlayer.get()).forEach(e1 -> e1.setSelected(false));
            cards.get(currentPlayer.get()).add(new CatanCard(selectedType, this::onSelectCard));
            resourcesToSelect = SelectResourceType.EXCHANGE;
        } else if (resourcesToSelect == SelectResourceType.EXCHANGE) {
            cards.get(currentPlayer.get()).removeIf(CatanCard::isSelected);
            cards.get(currentPlayer.get()).add(new CatanCard(selectedType, this::onSelectCard));
            resourceChoices.setVisible(false);
            resourcesToSelect = SelectResourceType.DEFAULT;
        }
        cards.get(currentPlayer.get()).forEach(e -> e.setSelected(false));
        onChangePlayer(currentPlayer.get());
        exchangeButton.setDisable(true);
        invalidateDice();
        makeDeal.setDisable(true);
        CatanLogger.log(this, selectedType);
    }

    private void onSkipTurn() {
        PlayerColor value = currentPlayer.get();
        PlayerColor[] values = PlayerColor.values();
        int next = getDirection();
        PlayerColor playerColor = values[(value.ordinal() + next + values.length) % values.length];
        currentPlayer.set(playerColor);
        diceThrown.set(false);
        exchangeButton.setDisable(true);
        cards.get(currentPlayer.get()).forEach(e -> e.setSelected(false));
        turnCount++;
        if (isPositioningPhase()) {
            diceThrown.set(true);
            elements.add(new Village(playerColor));
            elements.add(new Road(playerColor));
        } else if (turnCount == 9) {
            settlePoints.stream().filter(e -> e.getElement() != null)
                .forEach(e -> cards.get(e.getElement().getPlayer())
                    .addAll(e.getTerrains().stream().map(Terrain::getType).filter(t -> t != ResourceType.DESERT)
                        .map(t -> new CatanCard(t, this::onSelectCard)).collect(Collectors.toList())));
            onChangePlayer(currentPlayer.get());
            invalidateDice();
        }
        deals.removeIf(d -> d.getProposer() == playerColor);
        CatanLogger.log(this, CatanAction.SKIP_TURN);
    }

    private void replaceThief() {
        terrains.stream().filter(t -> t.getThief() != null).forEach(Terrain::fadeOut);
        Parent parent = thief.getParent();
        if (parent instanceof Group) {
            ((Group) parent).getChildren().remove(thief);
        }
        thief.setPlayer(currentPlayer.get());
        if (!elements.contains(thief)) {
            elements.add(thief);
        }
    }

    private void setResourceSelect(SelectResourceType deal) {
        if (resourcesToSelect == SelectResourceType.DEFAULT) {
            resourcesToSelect = deal;
            resourceChoices.setVisible(true);
        }
        if (deal == SelectResourceType.MAKE_DEAL) {
            makeDeal.setDisable(true);
        }
    }

    private void stealResource(Terrain terrain) {
        List<PlayerColor> playersToSteal = settlePoints.stream()
            .filter(p -> p.getElement() != null && p.getTerrains().contains(terrain))
            .filter(p -> p.getElement().getPlayer() != currentPlayer.get()).map(p -> p.getElement().getPlayer())
            .collect(Collectors.toList());
        if (!playersToSteal.isEmpty()) {
            Collections.shuffle(playersToSteal);
            List<CatanCard> list = cards.get(playersToSteal.get(0));
            Collections.shuffle(list);
            Optional<CatanCard> catanCard = list.parallelStream().filter(e -> e.getResource() != null).findFirst();
            if (catanCard.isPresent()) {
                CatanCard o = catanCard.get();
                list.remove(o);
                cards.get(currentPlayer.get()).add(o);
            }
        }
        onChangePlayer(currentPlayer.get());
        invalidateDice();
        CatanLogger.log(this, CatanAction.PLACE_THIEF);
    }

    private void throwDice() {
        int diceValue = getUserChart().throwDice();
        settlePoints.stream().filter(e -> e.getElement() != null)
            .flatMap(e -> e.getElement() instanceof City ? Stream.of(e, e) : Stream.of(e)).forEach(
                e -> cards.get(e.getElement().getPlayer())
                    .addAll(e.getTerrains().stream().filter(t -> t.getNumber() == diceValue)
                        .filter(t -> t.getThief() == null).map(t -> new CatanCard(t.getType(), this::onSelectCard))
                        .collect(Collectors.toList())));

        diceThrown.set(true);
        if (diceValue == 7) {
            replaceThief();
            Thief.removeHalfOfCards(cards);
        }
        onChangePlayer(currentPlayer.get());
        CatanLogger.log(this, CatanAction.THROW_DICE);
    }

    private void updatePoints(PlayerColor newV) {
        getUserChart().setPoints(newV, settlePoints, usedCards, edges);
        getUserChart().updatePorts(newV, ports, settlePoints, currentPlayer);
        invalidateDice();
    }
    public static CatanModel create(Pane root, Pane value) {
        return new CatanModel(root, value);
    }



}
