package gaming.ex21;

import graphs.entities.Edge;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Toggle;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import simplebuilder.SimpleToggleGroupBuilder;
import utils.CommonsFX;

public class CatanModel {
	private List<Terrain> terrains = new ArrayList<>();
	private List<SettlePoint> settlePoints = new ArrayList<>();
	private List<EdgeCatan> edges;
	private Map<PlayerColor, List<CatanCard>> cards = newMapList();
	private Map<PlayerColor, List<DevelopmentType>> usedCards = newMapList();
	private ObjectProperty<PlayerColor> currentPlayer = new SimpleObjectProperty<>();
	private ObservableList<CatanResource> elements = FXCollections.observableArrayList();
	private DragContext dragContext = new DragContext();
	private BooleanProperty diceThrown = new SimpleBooleanProperty(false);
	private StackPane center;
	private int resourcesToSelect = 0;
	private Group cardGroup = new Group();
	private Text userPoints = new Text("0");
    private ImageView userImage = CatanResource.newImage("user.png", Color.BLUE, 100);
	private Dice dice1 = new Dice();
	private Dice dice2 = new Dice();
	private int turnCount;
	private int layX = -Terrain.RADIUS / 2;
	private int layY = -Terrain.RADIUS / 2;
	private Button exchangeButton = new Button("Exchange");
	private HBox resourceChoices = createResourceChoices();
	private Button makeDeal = CommonsFX.newButton("Make Deal", e -> {
		resourcesToSelect = 4;
		resourceChoices.setVisible(true);
	});
	private VBox deals = new VBox();
	private Thief thief = makeDraggable(new Thief());

	private List<Port> ports = Stream.of(ResourceType.values())
			.flatMap(t -> Stream.generate(() -> t).limit(t == ResourceType.DESERT ? 4 : 1)).map(Port::new)
			.collect(Collectors.toList());
	private final List<DevelopmentType> developmentCards = getDevelopmentCards();

	private Circle circle = new Circle(2);
	private Pane right;

	private VBox availablePorts = new VBox();

	private ExtraPoint largestArmy = new ExtraPoint("largestarmy.png");

	private ExtraPoint longestRoad = new ExtraPoint("longestroad.png");

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

	private void addTerrains(final StackPane root) {
		List<Integer> numbers = getNumbers();
		List<ResourceType> cells = createResources();
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
				createSettlePoints(terrain, x, y);
				terrains.add(terrain);
				root.getChildren().add(terrain);
			}
		}
		circle.setLayoutX(radius * 3);
		circle.setLayoutY(radius * 2.7);
		root.setManaged(false);
		edges = settlePoints.stream().flatMap(s -> s.getNeighbors().stream().map(t -> new EdgeCatan(s, t))).distinct()
				.collect(Collectors.toList());
		edges.forEach(e -> e.getPoints().forEach(p -> p.getEdges().add(e)));
		Collections.shuffle(ports);
		relocatePorts(radius / 4);
		root.getChildren().addAll(edges);
		root.getChildren().addAll(ports);
		root.getChildren().addAll(settlePoints);

	}

	private int amountDevelopment(final DevelopmentType t) {
		if (t == DevelopmentType.KNIGHT) {
			return 14;
		} else if (t == DevelopmentType.UNIVERSITY) {
			return 5;
		} else {
			return 2;
		}
	}

	private boolean containsEnough(final Combination combination, final List<CatanCard> list) {
		return containsEnough(list, combination.getResources());
	}

	private boolean containsEnough(final List<CatanCard> list, final List<ResourceType> resourcesNeeded) {
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

	private long countPoints(final PlayerColor newV) {
		long count = settlePoints.stream().filter(s -> s.getElement() instanceof Village)
				.filter(e -> e.getElement().getPlayer() == newV).count();
		count += settlePoints.stream().filter(s -> s.getElement() instanceof City)
				.filter(e -> e.getElement().getPlayer() == newV).count() * 2;
		count += usedCards.get(newV).stream().filter(e -> e == DevelopmentType.UNIVERSITY).count();
		long armySize = usedCards.get(newV).stream().filter(e1 -> e1 == DevelopmentType.KNIGHT).count();
		if (armySize >= 3 && largestArmy.getRecord() < armySize) {
			largestArmy.setPlayer(newV);
			largestArmy.setRecord(armySize);
		}
		if (largestArmy.getPlayer() == newV) {
			count += 2;
		}
		long roadSize = countRoadSize(newV);
		if (roadSize >= 5 && longestRoad.getRecord() < roadSize) {
			longestRoad.setPlayer(newV);
			longestRoad.setRecord(roadSize);
		}
		if (longestRoad.getPlayer() == newV) {
			count += 2;
		}

		return count;
	}

	private long countRoadSize(final PlayerColor newV) {
		List<EdgeCatan> collect = edges.stream().filter(e -> e.getElement() != null)
				.filter(e -> e.getElement().getPlayer() == newV).collect(Collectors.toList());
		if (collect.size() >= 5) {
			List<SettlePoint> collect2 = collect.stream().flatMap(e -> e.getPoints().stream()).distinct()
					.collect(Collectors.toList());

			return collect2.stream().map(m -> dijkstra(m, collect2, collect)).mapToInt(
					d -> d.values().stream().mapToInt(e -> e).filter(e -> e != Integer.MAX_VALUE).max().orElse(0)).max()
					.orElse(0);
		}

		return 0;
	}

	private HBox createResourceChoices() {
		SimpleToggleGroupBuilder group = new SimpleToggleGroupBuilder();
		for (ResourceType type : ResourceType.values()) {
			if (type.getPure() != null) {
                ImageView node = CatanResource.newImage(type.getPure(), 20);
				group.addToggle(node, type);
			}
		}
		HBox res = new HBox(group.getTogglesAs(Node.class).toArray(new Node[0]));
		res.setVisible(false);
		res.managedProperty().bind(res.visibleProperty());
		group.onChange((ob, old, n) -> onSelectResource(group, n));
		return res;
	}

	private List<ResourceType> createResources() {
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

	private void createSettlePoints(final Terrain cell, final double x, final double y) {
		for (SettlePoint p : getCircles(x, y)) {
			if (settlePoints.stream().noneMatch(e -> intersects(p, e))) {
				settlePoints.add(p);
				p.addTerrain(cell);
			} else {
				p.removeNeighbors();
			}
			settlePoints.stream().filter(e -> intersects(p, e)).findFirst()
					.ifPresent(e -> e.addTerrain(cell).addAllNeighbors(p));

		}
	}

	private boolean disableCombination(final Combination combination) {
		PlayerColor key = currentPlayer.get();
		List<CatanCard> list = cards.get(key);
		if (list == null) {
			return true;
		}
		boolean b = !containsEnough(combination, list);
		if (b) {
			return b;
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
					|| settlePoints.stream().noneMatch(s -> s.acceptVillage(key))

			;
		}
		if (combination == Combination.ROAD) {
			return elementCount.getOrDefault(Road.class, 0L) >= 15;
		}

		return developmentCards.isEmpty();
	}

	private boolean edgeAcceptRoad(final EdgeCatan edge, final Road road) {
		return edge.edgeAcceptRoad(road);
	}

	private boolean edgeAcceptRoad(final Optional<EdgeCatan> firstEdge, final Road road) {
		if (!firstEdge.isPresent()) {
			return false;
		}
		EdgeCatan edgeCatan = firstEdge.get();
		return edgeAcceptRoad(edgeCatan, road);
	}

	private List<SettlePoint> getCircles(final double xOff, final double yOff) {
		List<SettlePoint> circles = new ArrayList<>();
		double off = Math.PI / 6;
		for (int i = 0; i < 6; i++) {
			double d = Math.PI / 3;
			double x = Math.cos(off + d * i) * Terrain.RADIUS + Terrain.RADIUS;
			double y = Math.sin(off + d * i) * Terrain.RADIUS + Terrain.RADIUS;
			double centerX = xOff + x - Terrain.RADIUS / 10.;
			double centerY = yOff + y;
			SettlePoint e = new SettlePoint(centerX, centerY);
			circles.add(e);
			if (circles.size() > 1) {
				e.addNeighbor(circles.get(i - 1));
			}
			if (circles.size() == 6) {
				e.addNeighbor(circles.get(0));
			}
		}
		return circles;
	}

	private List<DevelopmentType> getDevelopmentCards() {
		List<DevelopmentType> developments = Stream.of(DevelopmentType.values())
				.flatMap(t -> Stream.generate(() -> t).limit(amountDevelopment(t))).collect(Collectors.toList());
		Collections.shuffle(developments);
		return developments;
	}

	private int getLimit(final int e) {
		if (e == 7) {
			return 0;
		}
		if (e == 2 || e == 12) {
			return 1;
		}
		return 2;
	}

	private List<Integer> getNumbers() {
		List<Integer> numbers = IntStream.range(2, 13).flatMap(e -> IntStream.generate(() -> e).limit(getLimit(e)))
				.boxed().collect(Collectors.toList());
		Collections.shuffle(numbers);
		return numbers;
	}

	private void handleMouseDragged(final MouseEvent event) {
		double offsetX = event.getScreenX() + dragContext.x;
		double offsetY = event.getScreenY() + dragContext.y;
		if (dragContext.element != null) {
			CatanResource c = dragContext.element;
			c.relocate(offsetX, offsetY);
			if (dragContext.point != null) {
				dragContext.point.toggleFade(1);
				dragContext.point = null;
			}
			if (dragContext.terrain != null) {
				dragContext.terrain.toggleFade(-1);
				dragContext.terrain = null;
			}
			if (dragContext.element instanceof Village) {
				settlePoints.stream().filter(e -> inArea(event, e)).findFirst()
						.ifPresent(e -> dragContext.point = e.toggleFade(-1));
			}
			if (dragContext.element instanceof City) {
				settlePoints.stream().filter(e -> inArea(event, e))
						.filter(e -> isSuitableForCity(e, (City) dragContext.element)).findFirst()
						.ifPresent(e -> dragContext.point = e.toggleFade(-1));
			}
			if (dragContext.element instanceof Thief) {
				terrains.stream().filter(e -> inArea(event, e)).findFirst()
						.ifPresent(e -> dragContext.terrain = e.toggleFade(1));
			}
			if (dragContext.element instanceof Road) {
				Road element = (Road) dragContext.element;
				if (dragContext.edge != null) {
					dragContext.edge.toggleFade(1, edgeAcceptRoad(dragContext.edge, element));
					dragContext.edge = null;
				}
				edges.stream().filter(e -> inArea(event, e)).findFirst()
						.ifPresent(e -> dragContext.edge = e.toggleFade(-1, edgeAcceptRoad(e, element)));
			}

		}
	}

	private void handleMousePressed(final MouseEvent event) {
		Node node = (Node) event.getSource();
		dragContext.x = node.getBoundsInParent().getMinX() - event.getScreenX();
		dragContext.y = node.getBoundsInParent().getMinY() - event.getScreenY();
		if (node instanceof CatanResource && elements.contains(node)) {
			dragContext.element = (CatanResource) node;
			elements.remove(node);
		}
	}

	private void handleMouseReleased(final MouseEvent event) {
		if (dragContext.element instanceof Village) {
			onReleaseVillage(event, (Village) dragContext.element);
		}
		if (dragContext.element instanceof City) {
			onReleaseCity(event, (City) dragContext.element);
		}
		if (dragContext.element instanceof Road) {
			onReleaseRoad(event, (Road) dragContext.element);
		}
		if (dragContext.element instanceof Thief) {
			onReleaseThief(event);
		}
		updatePoints(currentPlayer.get());
	}

	private boolean inArea(final MouseEvent event, final Node e) {
		return e.getBoundsInParent().contains(event.getSceneX(), event.getSceneY());
	}

	private void initialize(final StackPane center1, final Pane right1) {
		center = center1;
		right = right1;
		addTerrains(center1);
		elements.addListener(this::onElementsChange);
		currentPlayer.addListener((ob, old, newV) -> onChangePlayer(newV));
		right.getChildren()
				.add(new HBox(userImage, new VBox(userPoints, availablePorts), largestArmy, longestRoad, deals));
		right.getChildren().add(new HBox(dice1, dice2));
		right.getChildren().add(cardGroup);
		Button skipButton = CommonsFX.newButton("Skip Turn", e -> onSkipTurn());
		skipButton.disableProperty()
				.bind(Bindings.createBooleanBinding(this::isSkippable, diceThrown, resourceChoices.visibleProperty(),
						currentPlayer, elements));
		largestArmy.visibleProperty().bind(currentPlayer.isEqualTo(largestArmy.playerProperty()));
		longestRoad.visibleProperty().bind(currentPlayer.isEqualTo(longestRoad.playerProperty()));
		Button throwButton = CommonsFX.newButton("Throw Dices", e -> throwDice());
		throwButton.disableProperty().bind(diceThrown);
		right.getChildren().add(new VBox(skipButton, throwButton, exchangeButton, makeDeal));
		exchangeButton.setOnAction(e -> resourceChoices.setVisible(true));
		makeDeal.setDisable(true);
		right.getChildren().add(resourceChoices);
		right.getChildren().add(addCombinations());
		currentPlayer.set(PlayerColor.BLUE);

		onSkipTurn();
	}

	private boolean intersects(final SettlePoint p, final SettlePoint e) {
		return e.getBoundsInParent().intersects(p.getBoundsInParent());
	}

	private void invalidateDice() {
		diceThrown.set(!diceThrown.get());
		diceThrown.set(!diceThrown.get());
	}

	private boolean isPositioningPhase() {
		return turnCount <= 8;
	}

	private Boolean isSkippable() {
		return !diceThrown.get() || resourceChoices.isVisible()
				|| elements.stream().anyMatch(e -> e.getPlayer() == currentPlayer.get());
	}

	private boolean isSuitableForCity(final SettlePoint e, final City element) {
		return e.isSuitableForCity(element);
	}

	private <E extends CatanResource> E makeDraggable(final E e) {
		e.setOnMousePressed(this::handleMousePressed);
		e.setOnMouseDragged(this::handleMouseDragged);
		e.setOnMouseReleased(this::handleMouseReleased);
		return e;
	}

	private CatanCard newCard(final DevelopmentType type) {
		CatanCard card = new CatanCard(type);
		card.setOnMouseClicked(e -> onSelectCard(card));
		return card;
	}

	private CatanCard newCard(final ResourceType t) {
		CatanCard catanCard = new CatanCard(t);
		catanCard.setOnMouseClicked(e -> onSelectCard(catanCard));
		return catanCard;
	}

	private void onChangePlayer(final PlayerColor newV) {
		updatePoints(newV);
        userImage.setImage(CatanResource.newImage("user.png", newV.getColor()));
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
				catanCard.relocate(layoutX, layoutY);
				layoutX += 10;
			}
			layoutX = 0;
			layoutY += CatanCard.PREF_HEIGHT;
		}

	}

	private void onCombinationClicked(final Combination combination) {
		List<CatanCard> list = cards.get(currentPlayer.get());
		boolean containsEnough = containsEnough(combination, list);
		if (containsEnough) {

			List<ResourceType> resources = combination.getResources().stream().collect(Collectors.toList());
			for (int i = 0; i < resources.size(); i++) {
				ResourceType r = resources.get(i);
				list.remove(list.stream().filter(e -> e.getResource() == r).findFirst().orElse(null));
			}
			switch (combination) {
				case VILLAGE:
					elements.add(makeDraggable(new Village(currentPlayer.get())));
					break;
				case CITY:
					elements.add(makeDraggable(new City(currentPlayer.get())));
					break;
				case ROAD:
					elements.add(makeDraggable(new Road(currentPlayer.get())));
					break;
				case DEVELOPMENT:

					list.add(newCard(developmentCards.remove(0)));
					break;

				default:
					break;
			}
		}
		invalidateDice();
		onChangePlayer(currentPlayer.get());
		list.forEach(e -> e.setSelected(true));
		list.forEach(this::onSelectCard);
	}

	private void onElementsChange(final Change<? extends Node> e) {
		while (e.next()) {
			List<? extends Node> addedSubList = e.getList();
			double layoutX = 0;
			double layoutY = center.getScene().getHeight()
					- addedSubList.stream().mapToDouble(e1 -> e1.getBoundsInLocal().getHeight()).min().orElse(0) * 2;
			for (Node node : addedSubList) {
				node.setLayoutX(layoutX);
				node.setLayoutY(layoutY);
				if (node.getParent() == null) {
					center.getChildren().add(node);
				}
				layoutX += node.getBoundsInLocal().getWidth() + 20;
			}
		}
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
				if (first.isPresent()) {
					cardsGiven.add(first.get());
				} else {
					return;
				}

			}
			list.remove(catanCard);
			listProposer.add(catanCard);
			list.addAll(cardsGiven);
			listProposer.removeAll(cardsGiven);
			deals.getChildren().removeIf(e -> e instanceof Button && deal.equals(((Button) e).getGraphic()));

		}
		onChangePlayer(currentPlayer.get());
		invalidateDice();
	}

	private void onReleaseCity(final MouseEvent event, final City element) {
		Optional<SettlePoint> findFirst = settlePoints.stream().filter(e -> inArea(event, e))
				.filter(t -> !t.isPointDisabled()).filter(e -> isSuitableForCity(e, element)).findFirst();
		if (findFirst.isPresent()) {
			findFirst.get().setElement(element);
		} else {
			elements.add(0, dragContext.element);
		}
		if (dragContext.point != null) {
			dragContext.point.toggleFade(1);
			dragContext.point = null;
		}
		dragContext.element = null;
	}

	private void onReleaseRoad(final MouseEvent event, final Road road) {
		Optional<EdgeCatan> edgeHovered = edges.stream().filter(e -> inArea(event, e))
				.filter(e -> edgeAcceptRoad(e, road)).findFirst();
		if (edgeHovered.isPresent()) {
			edgeHovered.get().setElement(road);
		} else {
			elements.add(0, dragContext.element);
		}
		if (dragContext.edge != null) {
			dragContext.edge.toggleFade(1, edgeAcceptRoad(edgeHovered, road));
			dragContext.edge = null;
		}
		dragContext.element = null;
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
			elements.add(0, dragContext.element);
		}
		if (dragContext.terrain != null) {
			dragContext.terrain.toggleFade(1);
			dragContext.terrain = null;
		}
		dragContext.element = null;
	}

	private void onReleaseVillage(final MouseEvent event, final Village village) {
		Optional<SettlePoint> findFirst = settlePoints.stream().filter(e -> inArea(event, e))
				.filter(t -> !t.isPointDisabled()).filter(t -> isPositioningPhase() || pointAcceptVillage(t, village))
				.findFirst();
		if (findFirst.isPresent()) {
			findFirst.get().setElement(village);
		} else {
			elements.add(0, dragContext.element);
		}
		if (dragContext.point != null) {
			dragContext.point.toggleFade(1);
			dragContext.point = null;
		}
		dragContext.element = null;
	}

	private void onSelectCard(final CatanCard catanCard) {
		catanCard.setSelected(!catanCard.isSelected());
		long count = cards.get(currentPlayer.get()).stream().filter(CatanCard::isSelected).count();
		List<ResourceType> distinct = cards.get(currentPlayer.get()).stream().filter(CatanCard::isSelected)
				.filter(e -> e.getResource() != null).map(CatanCard::getResource).distinct()
				.collect(Collectors.toList());
		long c = distinct.size();
		boolean containsPort = c == 1 && ports.stream().anyMatch(p -> p.getNumber() == count
				&& (p.getType() == distinct.get(0) || p.getType() == ResourceType.DESERT) && p.getPoints().stream()
						.anyMatch(s -> s.getElement() != null && s.getElement().getPlayer() == currentPlayer.get()));
		exchangeButton.setDisable((count != 4 || c != 1) && !containsPort);
		DevelopmentType development = catanCard.getDevelopment();
		if (catanCard.isSelected() && development != null) {
			onSelectDevelopment(catanCard, development);
		}
		makeDeal.setDisable(c == 0);
	}

	private void onSelectDevelopment(final CatanCard catanCard, final DevelopmentType development) {
		cards.get(currentPlayer.get()).remove(catanCard);
		usedCards.get(currentPlayer.get()).add(development);
		switch (development) {
			case KNIGHT:
				replaceThief();
				break;
			case MONOPOLY:
				resourcesToSelect = 3;
				resourceChoices.setVisible(true);
				break;
			case ROAD_BUILDING:
				elements.add(makeDraggable(new Road(currentPlayer.get())));
				elements.add(makeDraggable(new Road(currentPlayer.get())));
				break;
			case UNIVERSITY:
				break;
			case YEAR_OF_PLENTY:
				resourcesToSelect = 2;
				resourceChoices.setVisible(true);
				break;
			default:
				break;
		}
		onChangePlayer(currentPlayer.get());
		invalidateDice();
	}

	private void onSelectResource(final SimpleToggleGroupBuilder group, final Toggle n) {
		if (n == null) {
			return;
		}
		ResourceType selectedType = (ResourceType) n.getUserData();
		if (resourcesToSelect == 4) {
			List<ResourceType> dealTypes = cards.get(currentPlayer.get()).stream().filter(e -> e.getResource() != null)
					.filter(CatanCard::isSelected).map(CatanCard::getResource).collect(Collectors.toList());
			if (!dealTypes.isEmpty()) {
				PlayerColor proposer = currentPlayer.get();
				Deal deal = new Deal(proposer, selectedType, dealTypes);
				Button dealButton = CommonsFX.newButton(deal, "", e -> onMakeDeal(deal));
				dealButton.disableProperty().bind(Bindings.createBooleanBinding(() -> currentPlayer.get() == proposer
						|| cards.get(currentPlayer.get()).stream().noneMatch(e -> e.getResource() == selectedType)
						|| !containsEnough(cards.get(proposer), dealTypes), currentPlayer, diceThrown)

				);
				deals.getChildren().add(dealButton);
			}
			resourceChoices.setVisible(false);
			makeDeal.setDisable(true);
			resourcesToSelect = 0;
		} else if (resourcesToSelect == 3) {
			List<CatanCard> cardsTransfered = new ArrayList<>();
			PlayerColor[] values = PlayerColor.values();
			for (PlayerColor color : values) {
				List<CatanCard> collect2 = cards.get(color).stream().filter(e -> e.getResource() == selectedType)
						.collect(Collectors.toList());
				cards.get(color).removeAll(collect2);
				cardsTransfered.addAll(collect2);
			}
			cards.get(currentPlayer.get()).addAll(cardsTransfered);
			resourcesToSelect = 0;
			resourceChoices.setVisible(false);
		} else {
			cards.get(currentPlayer.get()).removeIf(CatanCard::isSelected);
			cards.get(currentPlayer.get()).add(newCard(selectedType));
			if (resourcesToSelect > 0) {
				resourcesToSelect--;
			}
			if (resourcesToSelect == 0) {
				resourceChoices.setVisible(false);
			}
		}
		cards.get(currentPlayer.get()).forEach(e -> e.setSelected(false));
		onChangePlayer(currentPlayer.get());
		group.select(null);
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
			elements.add(makeDraggable(new Village(playerColor)));
			elements.add(makeDraggable(new Road(playerColor)));
		} else if (turnCount == 9) {
			settlePoints.stream().filter(e -> e.getElement() != null).forEach(
					e -> cards.get(e.getElement().getPlayer()).addAll(e.getTerrains().stream().map(Terrain::getType)
							.filter(t -> t != ResourceType.DESERT).map(this::newCard).collect(Collectors.toList())));
			onChangePlayer(currentPlayer.get());
			invalidateDice();
		}
		deals.getChildren().removeIf(d -> ((Deal) ((Button) d).getGraphic()).getProposer() == playerColor);
	}

	private boolean pointAcceptVillage(final SettlePoint point, final Village village) {
		return point.pointAcceptVillage(village);
	}

	private void relocatePorts(final double radius) {

		List<SettlePoint> s = settlePoints.stream().collect(Collectors.toList());
		Collections.shuffle(s);
		List<List<SettlePoint>> portLocations = s.stream().filter(p -> p.getNeighbors().size() == 2)
				.flatMap(p0 -> p0.getNeighbors().stream().map(p1 -> Arrays.asList(p0, p1)))
				.collect(Collectors.toList());
		for (int j = 0; j < ports.size() && !portLocations.isEmpty(); j++) {
			Port port = ports.get(j);
			List<SettlePoint> points = portLocations.remove(0);
			Optional<SettlePoint> first = points.stream().filter(l -> l.getNeighbors().size() == 3).findFirst();
			portLocations.removeIf(p -> p.contains(points.get(0)));
			portLocations.removeIf(p -> p.contains(points.get(1)));
			port.getPoints().addAll(points);
			if (first.isPresent()) {
				ObservableList<SettlePoint> neighbors = first.get().getNeighbors()
						.filtered(p -> p.getNeighbors().size() == 2);
				double x = neighbors.stream().mapToDouble(SettlePoint::getLayoutX).average().orElse(0);
				double y = neighbors.stream().mapToDouble(SettlePoint::getLayoutY).average().orElse(0);
				port.relocate(x + layX, y + layY);
			} else {
				double x = points.stream().mapToDouble(SettlePoint::getLayoutX).average().orElse(0);
				double y = points.stream().mapToDouble(SettlePoint::getLayoutY).average().orElse(0);
				double angulo = Math.PI / 2 - Edge.getAngulo(circle.getLayoutX(), circle.getLayoutY(), x, y);
				double m = Math.sin(angulo) * radius;
				double n = Math.cos(angulo) * radius;
				port.relocate(x + m + layX, y + n + layY);
			}
		}

	}

	private void replaceThief() {
		terrains.stream().filter(t -> t.getThief() != null).forEach(t -> t.toggleFade(-1));
		Parent parent = thief.getParent();
		if (parent instanceof Group) {
			((Group) parent).getChildren().remove(thief);
		}
		thief.setPlayer(currentPlayer.get());
		elements.add(thief);
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
		int a = dice1.throwDice() + dice2.throwDice();
		settlePoints.stream().filter(e -> e.getElement() != null)
				.flatMap(e -> e.getElement() instanceof City ? Stream.of(e, e) : Stream.of(e))
				.forEach(e -> cards.get(e.getElement().getPlayer())
						.addAll(e.getTerrains().stream().filter(t -> t.getNumber() == a)
								.filter(t -> t.getThief() == null).map(t -> newCard(t.getType()))
								.collect(Collectors.toList())));
		onChangePlayer(currentPlayer.get());
		diceThrown.set(true);
		if (a == 7) {
			replaceThief();
		}

	}

	private void updatePoints(final PlayerColor newV) {
		userPoints.setText(countPoints(newV) + " Points");
		ports.stream().filter(p -> !availablePorts.getChildren().contains(p.newStatus()))
				.filter(p -> settlePoints.stream().filter(s -> s.getElement() != null)
						.filter(s -> s.getElement().getPlayer() == newV).anyMatch(s -> p.getPoints().contains(s)))
				.forEach(p -> {
					HBox newStatus = p.newStatus();
					availablePorts.getChildren().add(newStatus);
					newStatus.visibleProperty().bind(currentPlayer.isEqualTo(newV));

				});
		if (countPoints(newV) >= 10) {
			CommonsFX.displayDialog("Player " + newV + " Won", "Reset", () -> {
				center.getChildren().clear();
				right.getChildren().clear();
				new CatanModel().initialize(center, right);
			});
		}
	}

	public static void create(final StackPane root, final Pane value) {
		new CatanModel().initialize(root, value);
	}

	private static Collection<SettlePoint> adjacents(final SettlePoint v, final List<EdgeCatan> allEdges) {
		return allEdges.stream().filter(e -> e.getPoints().contains(v))
				.flatMap(e -> e.getPoints().stream().filter(p -> p != v)).collect(Collectors.toList());
	}

	private static Integer cost(final SettlePoint v, final SettlePoint w, final List<EdgeCatan> allEdges) {
		return (int) allEdges.stream().filter(e -> e.getPoints().contains(v) && e.getPoints().contains(w)).count();
	}

	private static Map<SettlePoint, Boolean> createDistanceMap(final SettlePoint source,
			final Map<SettlePoint, Integer> distance, final List<SettlePoint> allCells) {
		Map<SettlePoint, Boolean> known = new HashMap<>();
		for (SettlePoint v : allCells) {
			distance.put(v, Integer.MAX_VALUE);
			known.put(v, false);
		}
		distance.put(source, 0);
		return known;
	}

	private static Map<SettlePoint, Integer> dijkstra(final SettlePoint s, final List<SettlePoint> allSettlePoints,
			final List<EdgeCatan> allEdges) {
		Map<SettlePoint, Integer> distance = new HashMap<>();
		Map<SettlePoint, Boolean> known = createDistanceMap(s, distance, allSettlePoints);
		while (known.entrySet().stream().anyMatch(e -> !e.getValue())) {
			SettlePoint v = getMinDistanceSettlePoint(distance, known);
			known.put(v, true);
			for (SettlePoint w : adjacents(v, allEdges)) {
				if (!known.get(w)) {
					Integer cvw = cost(v, w, allEdges);
					if (distance.get(v) + cvw < distance.get(w)) {
						int value = distance.get(v) + cvw;
						distance.put(w, value);
					}
				}
			}
		}
		return distance;
	}

	private static SettlePoint getMinDistanceSettlePoint(final Map<SettlePoint, Integer> distance,
			final Map<SettlePoint, Boolean> known) {
		return distance.entrySet().stream().filter(e -> !known.get(e.getKey()))
				.min(Comparator.comparing(Entry<SettlePoint, Integer>::getValue))
				.orElseThrow(() -> new RuntimeException("There should be someone")).getKey();
	}

	private static <T> ObservableMap<PlayerColor, List<T>> newMapList() {
		ObservableMap<PlayerColor, List<T>> observableHashMap = FXCollections.observableHashMap();

		for (PlayerColor playerColor : PlayerColor.values()) {
			observableHashMap.put(playerColor, new ArrayList<>());
		}
		return observableHashMap;
	}
}
