package gaming.ex21;

import static gaming.ex21.ResourceType.containsEnough;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
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
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import utils.CommonsFX;

public class CatanModel {
    private final List<Terrain> terrains = new ArrayList<>();
    private final List<SettlePoint> settlePoints = new ArrayList<>();
    private final List<EdgeCatan> edges;
    private final Map<PlayerColor, List<CatanCard>> cards = PlayerColor.newMapList();
    private final Map<PlayerColor, List<DevelopmentType>> usedCards = PlayerColor.newMapList();
    private final ObjectProperty<PlayerColor> currentPlayer = new SimpleObjectProperty<>();
    private final ObservableList<CatanResource> elements = FXCollections.observableArrayList();
    private final DragContext dragContext = new DragContext();
    private final BooleanProperty diceThrown = new SimpleBooleanProperty(false);
    private final StackPane center;
    private SelectResourceType resourcesToSelect = SelectResourceType.EXCHANGE;
    private final Group cardGroup = new Group();
    private int turnCount;
    private final HBox resourceChoices = ResourceType.createResourceChoices(this::onSelectResource);
    private final Button exchangeButton = CommonsFX.newButton("Exchange", e -> {
	resourcesToSelect = SelectResourceType.EXCHANGE;
	resourceChoices.setVisible(true);
    });
    private final Button makeDeal = CommonsFX.newButton("Make Deal", e -> {
	resourcesToSelect = SelectResourceType.MAKE_DEAL;
	resourceChoices.setVisible(true);
    });
    private final ObservableList<Deal> deals = FXCollections.observableArrayList();
    private final Thief thief = new Thief();
    private final List<Port> ports = Port.getPorts();
    private final List<DevelopmentType> developmentCards = DevelopmentType.getDevelopmentCards();
    private final Pane right;
    private final VBox availablePorts = new VBox();
    private final ExtraPoint largestArmy = new ExtraPoint("largestarmy.png");
    private final ExtraPoint longestRoad = new ExtraPoint("longestroad.png");
    private final UserChart userChart = new UserChart();

    public CatanModel(StackPane root, Pane value) {
	center = root;
	right = value;
	edges = addTerrains(center);
	center.setOnMousePressed(this::handleMousePressed);
	center.setOnMouseDragged(this::handleMouseDragged);
	center.setOnMouseReleased(this::handleMouseReleased);
	elements.addListener(ListHelper.onChangeElement(center));
	currentPlayer.addListener((ob, old, newV) -> onChangePlayer(newV));
	VBox vBox = ListHelper.newDeal(deals, this::isDealUnfeasible, this::onMakeDeal, currentPlayer, diceThrown);
	right.getChildren().add(new HBox(userChart, new VBox(availablePorts, vBox), largestArmy, longestRoad));
	right.getChildren().add(cardGroup);
	Button skipButton = CommonsFX.newButton("Skip Turn", e -> onSkipTurn());
	skipButton.disableProperty().bind(Bindings.createBooleanBinding(this::isSkippable, diceThrown,
		resourceChoices.visibleProperty(), currentPlayer, elements));
	largestArmy.visibleProperty().bind(currentPlayer.isEqualTo(largestArmy.playerProperty()));
	longestRoad.visibleProperty().bind(currentPlayer.isEqualTo(longestRoad.playerProperty()));
	Button throwButton = CommonsFX.newButton("Throw Dices", e -> throwDice());
	throwButton.disableProperty().bind(diceThrown);
	right.getChildren().add(new VBox(skipButton, throwButton, exchangeButton, makeDeal));

	makeDeal.setDisable(true);
	right.getChildren().add(resourceChoices);
	right.getChildren().add(addCombinations());
	currentPlayer.set(PlayerColor.BLUE);
	onSkipTurn();
    }

    private Node addCombinations() {
	GridPane value = new GridPane();
	Combination[] combinations = Combination.values();
	for (int i = 0; i < combinations.length; i++) {
	    Combination combination = combinations[i];
	    List<ResourceType> resources = combination.getResources();
	    ImageView el = CatanResource.newImage(combination.getElement(), 30, 30);
	    Button button = CommonsFX.newButton(el, "" + combination, e -> onCombinationClicked(combination));
	    button.disableProperty().bind(
		    Bindings.createBooleanBinding(() -> disableCombination(combination), currentPlayer, diceThrown));
	    value.addRow(i, button);
	    for (ResourceType resourceType : resources) {
		ImageView e2 = CatanResource.newImage(resourceType.getPure(), 20);
		value.addRow(i, e2);
	    }
	}
	return value;
    }

    private List<EdgeCatan> addTerrains(final StackPane root) {
	List<Integer> numbers = Terrain.getNumbers();
	List<ResourceType> cells = ResourceType.createResources();
	final double radius = Terrain.RADIUS * Math.sqrt(3);
	for (int i = 3, j = 0, l = 0; j < cells.size(); j += i, i += j > 11 ? -1 : 1, l++) {
	    List<ResourceType> resources = cells.subList(j, j + i);
	    for (int k = 0; k < resources.size(); k++) {
		Terrain terrain = new Terrain(resources.get(k));
		double f = -radius / 2 * (i - 3);
		double x = radius * k + f + radius * 3 / 2;
		double y = radius * l * Math.sqrt(3) / 2 + radius / 3;
		terrain.relocate(x, y);
		if (resources.get(k) != ResourceType.DESERT) {
		    terrain.setNumber(numbers.remove(0));
		}
		final Terrain cell = terrain;
		cell.createSettlePoints(x, y, settlePoints);
		terrains.add(terrain);
		root.getChildren().add(terrain);
	    }
	}

	List<EdgeCatan> edges = settlePoints.stream()
		.flatMap(s -> s.getNeighbors().stream().map(t -> new EdgeCatan(s, t))).distinct()
		.collect(Collectors.toList());
	edges.forEach(e -> e.getPoints().forEach(p -> p.getEdges().add(e)));
	Collections.shuffle(ports);
	Port.relocatePorts(settlePoints, ports);
	root.getChildren().addAll(edges);
	root.getChildren().addAll(ports);
	root.getChildren().addAll(settlePoints);
	return edges;

    }

    private boolean containsPort(long totalCards, List<ResourceType> distinct, long differentTypesNumber) {
	if (differentTypesNumber != 1) {
	    return false;
	}
	return ports.stream().anyMatch(p -> p.getNumber() == totalCards
		&& (p.getType() == distinct.get(0) || p.getType() == ResourceType.DESERT) && p.getPoints().stream()
			.anyMatch(s -> s.getElement() != null && s.getElement().getPlayer() == currentPlayer.get()));
    }

    private long countPoints(final PlayerColor newPlayer) {
	long pointsCount = settlePoints.stream().filter(s -> s.getElement() instanceof Village)
		.filter(e -> e.getElement().getPlayer() == newPlayer).count();
	pointsCount += settlePoints.stream().filter(s -> s.getElement() instanceof City)
		.filter(e -> e.getElement().getPlayer() == newPlayer).count() * 2;
	pointsCount += usedCards.get(newPlayer).stream().filter(e -> e == DevelopmentType.UNIVERSITY).count();
	long armySize = usedCards.get(newPlayer).stream().filter(e1 -> e1 == DevelopmentType.KNIGHT).count();
	if (armySize >= 3 && largestArmy.getRecord() < armySize) {
	    largestArmy.setPlayer(newPlayer);
	    largestArmy.setRecord(armySize);
	}
	if (largestArmy.getPlayer() == newPlayer) {
	    pointsCount += 2;
	}
	long roadSize = EdgeCatan.countRoadSize(newPlayer, edges);
	if (roadSize >= 5 && longestRoad.getRecord() < roadSize) {
	    longestRoad.setPlayer(newPlayer);
	    longestRoad.setRecord(roadSize);
	}
	if (longestRoad.getPlayer() == newPlayer) {
	    pointsCount += 2;
	}
	return pointsCount;
    }

    private boolean disableCombination(final Combination combination) {
	PlayerColor key = currentPlayer.get();
	List<CatanCard> list = cards.get(key);
	if (list == null) {
	    return true;
	}
	boolean notEnough = !containsEnough(list, combination.getResources());
	if (notEnough) {
	    return notEnough;
	}
	Map<Class<?>, Long> elementCount = settlePoints.stream()
		.filter(s -> s.getElement() != null && s.getElement().getPlayer() == key).map(SettlePoint::getElement)
		.collect(Collectors.groupingBy(CatanResource::getClass, Collectors.counting()));

	elementCount.putAll(edges.stream().filter(s -> s.getElement() != null && s.getElement().getPlayer() == key)
		.map(EdgeCatan::getElement)
		.collect(Collectors.groupingBy(CatanResource::getClass, Collectors.counting())));
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

    private void exchangeForOneResource(ResourceType selectedType) {
	cards.get(currentPlayer.get()).removeIf(CatanCard::isSelected);
	final ResourceType t = selectedType;
	cards.get(currentPlayer.get()).add(new CatanCard(t, this::onSelectCard));
	resourceChoices.setVisible(false);
    }

    private void handleMouseDragged(final MouseEvent event) {
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

    private void handleMousePressed(final MouseEvent event) {
	Optional<Node> findFirst = center.getChildren().parallelStream()
		.filter(e -> e.getBoundsInParent().contains(event.getX(), event.getY())).findFirst();
	if (findFirst.isPresent()) {
	    Node node = findFirst.get();
	    dragContext.setX(node.getBoundsInParent().getMinX() - event.getX());
	    dragContext.setY(node.getBoundsInParent().getMinY() - event.getY());
	    if (node instanceof CatanResource && elements.contains(node)) {
		dragContext.setElement((CatanResource) node);
		elements.remove(node);
	    }
	}
    }

    private void handleMouseReleased(final MouseEvent event) {
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

    private boolean inArea(final MouseEvent event, final Node e) {
	return e.getBoundsInParent().contains(event.getX(), event.getY());
    }

    private void invalidateDice() {
	diceThrown.set(!diceThrown.get());
	diceThrown.set(!diceThrown.get());
    }

    private Boolean isDealUnfeasible(Deal deal) {
	PlayerColor proposer = deal.getProposer();
	return currentPlayer.get() == proposer
		|| cards.get(currentPlayer.get()).stream().noneMatch(e -> e.getResource() == deal.getWantedType())
		|| !containsEnough(cards.get(proposer), deal.getDealTypes());
    }

    private boolean isPositioningPhase() {
	return turnCount <= 8;
    }

    private Boolean isSkippable() {
	return !diceThrown.get() || resourceChoices.isVisible()
		|| elements.stream().anyMatch(e -> e.getPlayer() == currentPlayer.get());
    }

    private void makeDealButton(ResourceType selectedType) {
	List<ResourceType> dealTypes = cards.get(currentPlayer.get()).stream().filter(e -> e.getResource() != null)
		.filter(CatanCard::isSelected).map(CatanCard::getResource).collect(Collectors.toList());
	if (!dealTypes.isEmpty()) {
	    PlayerColor proposer = currentPlayer.get();
	    deals.add(new Deal(proposer, selectedType, dealTypes));
	}
	resourceChoices.setVisible(false);
	makeDeal.setDisable(true);
	resourcesToSelect = SelectResourceType.EXCHANGE;
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
	resourcesToSelect = SelectResourceType.EXCHANGE;
	resourceChoices.setVisible(false);
    }

    private void onChangePlayer(final PlayerColor newV) {
	updatePoints(newV);
	userChart.setColor(newV);
	cardGroup.getChildren().clear();
	List<CatanCard> currentCards = cards.get(currentPlayer.get());
	for (CatanCard type : currentCards) {
	    cardGroup.getChildren().add(type);
	}
	Collection<List<CatanCard>> values = currentCards.stream().filter(e -> e.getResource() != null)
		.collect(Collectors.groupingBy(CatanCard::getResource)).values().stream().collect(Collectors.toList());
	double layoutX = 0;
	double layoutY = 0;
	List<CatanCard> collect = currentCards.stream().filter(e -> e.getResource() == null)
		.collect(Collectors.toList());
	values.add(collect);
	for (List<CatanCard> list : values) {
	    for (CatanCard catanCard : list) {
		catanCard.relocate(layoutY, layoutX);
		layoutX += 10;
	    }
	    layoutX = 0;
	    layoutY += CatanCard.PREF_WIDTH;
	}

    }

    private void onCombinationClicked(final Combination combination) {
	List<CatanCard> list = cards.get(currentPlayer.get());
	if (containsEnough(list, combination.getResources())) {
	    List<ResourceType> resources = combination.getResources().stream().collect(Collectors.toList());
	    for (int i = 0; i < resources.size(); i++) {
		ResourceType r = resources.get(i);
		list.remove(list.stream().filter(e -> e.getResource() == r).findFirst().orElse(null));
	    }
	    if (Combination.VILLAGE == combination) {
		elements.add(new Village(currentPlayer.get()));
	    } else if (Combination.CITY == combination) {
		elements.add(new City(currentPlayer.get()));
	    } else if (Combination.ROAD == combination) {
		elements.add(new Road(currentPlayer.get()));
	    } else if (Combination.DEVELOPMENT == combination) {
		list.add(new CatanCard(developmentCards.remove(0), this::onSelectCard));
	    }
	}
	invalidateDice();
	onChangePlayer(currentPlayer.get());
	list.forEach(e -> e.setSelected(true));
	list.forEach(this::onSelectCard);
    }

    private void onMakeDeal(final Deal deal) {
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

	}
	onChangePlayer(currentPlayer.get());
	invalidateDice();
    }

    private void onReleaseCity(final MouseEvent event, final City element) {
	Optional<SettlePoint> findFirst = settlePoints.stream().filter(e -> inArea(event, e))
		.filter(t -> !t.isPointDisabled()).filter(e -> e.isSuitableForCity(element)).findFirst();
	if (findFirst.isPresent()) {
	    findFirst.get().setElement(element);
	} else {
	    elements.add(0, dragContext.getElement());
	}
	dragContext.pointFadeOut();
	dragContext.setElement(null);
    }

    private void onReleaseRoad(final MouseEvent event, final Road road) {
	Optional<EdgeCatan> edgeHovered = edges.stream().filter(e -> inArea(event, e))
		.filter(e -> EdgeCatan.edgeAcceptRoad(e, road)).findFirst();
	if (edgeHovered.isPresent()) {
	    edgeHovered.get().setElement(road);
	} else {
	    elements.add(0, dragContext.getElement());
	}
	dragContext.edgeFadeOut(EdgeCatan.edgeAcceptRoad(edgeHovered.orElse(null), road));
	dragContext.setElement(null);
    }

    private void onReleaseThief(final MouseEvent event) {
	Optional<Terrain> edgeHovered = terrains.stream().filter(e -> inArea(event, e))
		.filter(e -> e.getThief() == null).findFirst();
	if (edgeHovered.isPresent()) {
	    terrains.forEach(t -> t.setThief(null));
	    Terrain terrain = edgeHovered.get();
	    terrain.setThief(thief);
	    stealResource(terrain);
	} else {
	    elements.add(0, dragContext.getElement());
	}
	dragContext.toggleTerrain(1);
	dragContext.setElement(null);
    }

    private void onReleaseVillage(final MouseEvent event, final Village village) {
	Optional<SettlePoint> findFirst = settlePoints.stream().filter(e -> inArea(event, e))
		.filter(t -> !t.isPointDisabled()).filter(t -> isPositioningPhase() || t.pointAcceptVillage(village))
		.findFirst();
	if (findFirst.isPresent()) {
	    findFirst.get().setElement(village);
	} else {
	    elements.add(0, dragContext.getElement());
	}
	dragContext.pointFadeOut();
	dragContext.setElement(null);
    }

    private void onSelectCard(final CatanCard catanCard) {
	catanCard.setSelected(!catanCard.isSelected());
	long totalCards = cards.get(currentPlayer.get()).stream().filter(CatanCard::isSelected).count();
	List<ResourceType> distinct = cards.get(currentPlayer.get()).stream().filter(CatanCard::isSelected)
		.filter(e -> e.getResource() != null).map(CatanCard::getResource).distinct()
		.collect(Collectors.toList());
	long distinctCount = distinct.size();
	boolean containsPort = containsPort(totalCards, distinct, distinctCount);
	exchangeButton.setDisable((totalCards != 4 || distinctCount != 1) && !containsPort);
	DevelopmentType development = catanCard.getDevelopment();
	if (catanCard.isSelected() && development != null) {
	    onSelectDevelopment(catanCard, development);
	}
	makeDeal.setDisable(distinctCount == 0);
    }

    private void onSelectDevelopment(final CatanCard catanCard, final DevelopmentType development) {
	cards.get(currentPlayer.get()).remove(catanCard);
	usedCards.get(currentPlayer.get()).add(development);
	switch (development) {
	case KNIGHT:
	    replaceThief();
	    break;
	case MONOPOLY:
	    resourcesToSelect = SelectResourceType.MONOPOLY;
	    resourceChoices.setVisible(true);
	    break;
	case ROAD_BUILDING:
	    elements.add(new Road(currentPlayer.get()));
	    elements.add(new Road(currentPlayer.get()));
	    break;
	case UNIVERSITY:
	    break;
	case YEAR_OF_PLENTY:
	    resourcesToSelect = SelectResourceType.YEAR_OF_PLENTY;
	    resourceChoices.setVisible(true);
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
	    plentyOfTwoResources(selectedType);
	} else {
	    exchangeForOneResource(selectedType);
	}
	cards.get(currentPlayer.get()).forEach(e -> e.setSelected(false));
	onChangePlayer(currentPlayer.get());
	exchangeButton.setDisable(true);
	invalidateDice();
	makeDeal.setDisable(true);
    }

    private void onSkipTurn() {
	PlayerColor value = currentPlayer.get();
	PlayerColor[] values = PlayerColor.values();
	PlayerColor playerColor = values[(value.ordinal() + 1) % values.length];
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
    }

    private void plentyOfTwoResources(ResourceType selectedType) {
	cards.get(currentPlayer.get()).forEach(e -> e.setSelected(false));
	cards.get(currentPlayer.get()).add(new CatanCard(selectedType, this::onSelectCard));
	resourcesToSelect = SelectResourceType.EXCHANGE;
    }

    private void removeHalfOfCards() {
	Random random = new Random();
	for (List<CatanCard> list : cards.values()) {
	    List<CatanCard> cardss = list.parallelStream().filter(e -> e.getDevelopment() == null)
		    .collect(Collectors.toList());
	    if (cardss.size() > 7) {
		int size = cardss.size();
		for (int i = 0; i < size / 2; i++) {
		    list.remove(cardss.remove(random.nextInt(cardss.size())));
		}
	    }
	}
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

    private void stealResource(final Terrain terrain) {
	List<PlayerColor> collect = settlePoints.stream()
		.filter(p -> p.getElement() != null && p.getTerrains().contains(terrain))
		.filter(p -> p.getElement().getPlayer() != currentPlayer.get()).map(p -> p.getElement().getPlayer())
		.collect(Collectors.toList());
	if (!collect.isEmpty()) {
	    Collections.shuffle(collect);
	    List<CatanCard> list = cards.get(collect.get(0));
	    if (!list.isEmpty()) {
		Collections.shuffle(list);
		CatanCard catanCard = list.remove(0);
		cards.get(currentPlayer.get()).add(catanCard);
	    }
	}
	onChangePlayer(currentPlayer.get());
	invalidateDice();
    }

    private void throwDice() {
	int a = userChart.throwDice();
	settlePoints.stream().filter(e -> e.getElement() != null)
		.flatMap(e -> e.getElement() instanceof City ? Stream.of(e, e) : Stream.of(e))
		.forEach(e -> cards.get(e.getElement().getPlayer()).addAll(e.getTerrains().stream()
			.filter(t -> t.getNumber() == a).filter(t -> t.getThief() == null)
			.map(t -> new CatanCard(t.getType(), this::onSelectCard)).collect(Collectors.toList())));
	onChangePlayer(currentPlayer.get());
	diceThrown.set(true);
	if (a == 7) {
	    replaceThief();
	    removeHalfOfCards();
	}
    }

    private void updatePoints(final PlayerColor newV) {
	userChart.setPoints(countPoints(newV), this::countPoints);
	ports.stream().filter(p -> !availablePorts.getChildren().contains(p.getStatus()))
		.filter(p -> settlePoints.stream().filter(s -> s.getElement() != null)
			.filter(s -> s.getElement().getPlayer() == newV).anyMatch(p.getPoints()::contains))
		.forEach(p -> {
		    HBox newStatus = p.getStatus();
		    availablePorts.getChildren().add(newStatus);
		    newStatus.visibleProperty().bind(currentPlayer.isEqualTo(newV));
		});
	if (countPoints(newV) >= 10) {
	    CommonsFX.displayDialog("Player " + newV + " Won", "Reset", () -> {
		center.getChildren().clear();
		right.getChildren().clear();
		create(center, right);
	    });
	}
    }

    public static CatanModel create(final StackPane root, final Pane value) {
	return new CatanModel(root, value);
    }
}
