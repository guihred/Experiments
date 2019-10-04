package gaming.ex21;
import static gaming.ex21.CatanCard.inArea;
import static gaming.ex21.CatanLogger.row;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
import javafx.scene.layout.StackPane;

public class CatanModel {
	private final List<Terrain> terrains = new ArrayList<>();
	private final List<SettlePoint> settlePoints = new ArrayList<>();
	private List<EdgeCatan> edges;
	private final Map<PlayerColor, List<CatanCard>> cards = PlayerColor.newMapList();
	private final Map<PlayerColor, List<DevelopmentType>> usedCards = PlayerColor.newMapList();
	private final ObjectProperty<PlayerColor> currentPlayer = new SimpleObjectProperty<>();
	private final ObservableList<CatanResource> elements = FXCollections.observableArrayList();
	private final DragContext dragContext = new DragContext();
	private final BooleanProperty diceThrown = new SimpleBooleanProperty(false);
	private SelectResourceType resourcesToSelect = SelectResourceType.DEFAULT;
	private int turnCount;
	private final ObservableList<Deal> deals = FXCollections.observableArrayList();
	private final Thief thief = new Thief();
	private final List<Port> ports = Port.getPorts();
	private final List<DevelopmentType> developmentCards = DevelopmentType.getDevelopmentCards();
	private Pane center;
	private HBox resourceChoices;
	private Button exchangeButton;
	private Button makeDeal;
	private UserChart userChart;

	public CatanModel(StackPane center) {
		this.center = center;
	}

	public boolean anyPlayerPoints(int minPoints) {
		return PlayerColor.vals().stream()
				.mapToLong(e -> userChart.countPoints(e, settlePoints, usedCards, edges))
				.max().orElse(0) >= minPoints;
	}

	public void combinationGrid(GridPane value) {
		Combination.combinationGrid(value, this::onCombinationClicked, this::disableCombination, currentPlayer,
				diceThrown);
	}

	public ObjectProperty<PlayerColor> currentPlayerProperty() {
		return currentPlayer;
	}

	public Map<PlayerColor, List<CatanCard>> getCards() {
		return cards;
	}

	public PlayerColor getCurrentPlayer() {
		return currentPlayer.get();
	}

	public ObservableList<Deal> getDeals() {
		return deals;
	}

	public List<DevelopmentType> getDevelopmentCards() {
		return developmentCards;
	}

	public BooleanProperty getDiceThrown() {
		return diceThrown;
	}

	public List<EdgeCatan> getEdges() {
		return edges;
	}

	public ObservableList<CatanResource> getElements() {
		return elements;
	}

	public Button getExchangeButton() {
		return exchangeButton;
	}

	public Button getMakeDeal() {
		return makeDeal;
	}

	public PlayerColor getPlayerWinner() {
		return userChart.getWinner(settlePoints, usedCards, edges, cards);
	}

	public List<Port> getPorts() {
		return ports;
	}

	public HBox getResourceChoices() {
		return resourceChoices;
	}

	public SelectResourceType getResourcesToSelect() {
		return resourcesToSelect;
	}

	public List<SettlePoint> getSettlePoints() {
		return settlePoints;
	}

	public List<Terrain> getTerrains() {
		return terrains;
	}

	public Map<PlayerColor, List<DevelopmentType>> getUsedCards() {
		return usedCards;
	}

	public UserChart getUserChart() {
		return userChart;
	}

	public void handleMouseDragged(MouseEvent event) {
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

	public void handleMousePressed(MouseEvent event) {
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

	public void handleMouseReleased(MouseEvent event) {
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
		updatePoints(getCurrentPlayer());
	}

	public boolean isDealUnfeasible(Deal deal) {
		return Deal.isDealUnfeasible(deal, currentPlayer, cards);
	}

	public boolean isSkippable() {
		return PlayerColor.isSkippable(diceThrown, resourceChoices, elements, currentPlayer);
	}

	public void makeDealButton(ResourceType selectedType) {
		List<ResourceType> dealTypes = cards.get(getCurrentPlayer()).stream().filter(e -> e.getResource() != null)
				.filter(CatanCard::isSelected).filter(e -> e.getResource() != selectedType).map(CatanCard::getResource)
				.collect(Collectors.toList());
		if (!dealTypes.isEmpty()) {
			PlayerColor proposer = getCurrentPlayer();
			deals.add(new Deal(proposer, selectedType, dealTypes));
			CatanLogger.log(row(this), CatanAction.MAKE_DEAL);
		}
		resourceChoices.setVisible(false);
		getMakeDeal().setDisable(true);
		resourcesToSelect = SelectResourceType.DEFAULT;
	}

	public void onChangePlayer(PlayerColor newV) {
		updatePoints(newV);
		userChart.setColor(newV);
		List<CatanCard> currentCards = cards.get(getCurrentPlayer());
		userChart.setCards(currentCards);
	}

	public void onCombinationClicked(Combination combination) {
		List<CatanCard> currentCards = cards.get(getCurrentPlayer());
		if (CatanCard.containsEnough(currentCards, combination.getResources())) {
			List<ResourceType> resources = combination.getResources().stream().collect(Collectors.toList());
			for (int i = 0; i < resources.size(); i++) {
				ResourceType r = resources.get(i);
				currentCards.remove(currentCards.stream().filter(e -> e.getResource() == r).findFirst().orElse(null));
			}
			if (Combination.VILLAGE == combination) {
				elements.add(new Village(getCurrentPlayer()));
			} else if (Combination.CITY == combination) {
				elements.add(new City(getCurrentPlayer()));
			} else if (Combination.ROAD == combination) {
				elements.add(new Road(getCurrentPlayer()));
			} else if (Combination.DEVELOPMENT == combination) {
				currentCards.add(new CatanCard(developmentCards.remove(0), this::onSelectCard));
			}
			CatanLogger.log(row(this), combination);
		}
		invalidateDice();
		onChangePlayer(getCurrentPlayer());
		currentCards.forEach(e -> e.setSelected(true));
		currentCards.forEach(this::onSelectCard);
	}

	public void onMakeDeal(Deal deal) {
		List<CatanCard> listProposer = cards.get(deal.getProposer());
		List<CatanCard> list = cards.get(getCurrentPlayer());
		ResourceType wantedType = deal.getWantedType();
		Optional<CatanCard> currentUserCard = list.stream().filter(e -> e.getResource() == wantedType).findFirst();
		if (currentUserCard.isPresent()) {
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
			CatanCard catanCard = currentUserCard.get();
			list.remove(catanCard);
			listProposer.add(catanCard);
			list.addAll(cardsGiven);
			listProposer.removeAll(cardsGiven);
			deals.remove(deal);
			CatanLogger.log(row(this), CatanAction.ACCEPT_DEAL);
		}
		onChangePlayer(getCurrentPlayer());
		invalidateDice();
	}

	public void onSelectResource(ResourceType selectedType) {
		if (resourcesToSelect == SelectResourceType.MAKE_DEAL) {
			makeDealButton(selectedType);
		} else if (resourcesToSelect == SelectResourceType.MONOPOLY) {
			monopolyOfResource(selectedType);
		} else if (resourcesToSelect == SelectResourceType.YEAR_OF_PLENTY) {
			cards.get(getCurrentPlayer()).forEach(e1 -> e1.setSelected(false));
			cards.get(getCurrentPlayer()).add(new CatanCard(selectedType, this::onSelectCard));
			resourcesToSelect = SelectResourceType.EXCHANGE;
		} else if (resourcesToSelect == SelectResourceType.EXCHANGE) {
			cards.get(getCurrentPlayer()).removeIf(CatanCard::isSelected);
			cards.get(getCurrentPlayer()).add(new CatanCard(selectedType, this::onSelectCard));
			resourceChoices.setVisible(false);
			resourcesToSelect = SelectResourceType.DEFAULT;
		}
		cards.get(getCurrentPlayer()).forEach(e -> e.setSelected(false));
		onChangePlayer(getCurrentPlayer());
		getExchangeButton().setDisable(true);
		invalidateDice();
		getMakeDeal().setDisable(true);
		CatanLogger.log(row(this), selectedType);
	}

	public void onSkipTurn() {
		PlayerColor value = getCurrentPlayer();
		PlayerColor[] values = PlayerColor.values();
		int next = getDirection();
		PlayerColor playerColor = values[(value.ordinal() + next + values.length) % values.length];
		currentPlayer.set(playerColor);
		diceThrown.set(false);
		getExchangeButton().setDisable(true);
		cards.get(getCurrentPlayer()).forEach(e -> e.setSelected(false));
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
			onChangePlayer(getCurrentPlayer());
			invalidateDice();
		}
		deals.removeIf(d -> d.getProposer() == playerColor);
		CatanLogger.log(row(this), CatanAction.SKIP_TURN);
	}

	public void setCurrentPlayer(PlayerColor value) {
		currentPlayer.set(value);
	}

	public void setEdges(List<EdgeCatan> addTerrains) {
		edges = addTerrains;
	}

	public void setExchangeButton(Button exchangeButton) {
		this.exchangeButton = exchangeButton;
	}

	public void setMakeDeal(Button makeDeal) {
		this.makeDeal = makeDeal;
	}

	public void setResourceChoices(HBox resourceChoices) {
		this.resourceChoices = resourceChoices;
	}

	public void setResourceSelect(SelectResourceType deal) {
		if (resourcesToSelect == SelectResourceType.DEFAULT) {
			resourcesToSelect = deal;
			resourceChoices.setVisible(true);
		}
		if (deal == SelectResourceType.MAKE_DEAL) {
			getMakeDeal().setDisable(true);
		}
	}

	public void setUserChart(UserChart userChart) {
		this.userChart = userChart;
	}

	public void throwDice() {
		int diceValue = userChart.throwDice();
		settlePoints.stream().filter(e -> e.getElement() != null)
				.flatMap(e -> e.getElement() instanceof City ? Stream.of(e, e) : Stream.of(e))
				.forEach(e -> cards.get(e.getElement().getPlayer()).addAll(e.getTerrains().stream()
						.filter(t -> t.getNumber() == diceValue).filter(t -> t.getThief() == null)
						.map(t -> new CatanCard(t.getType(), this::onSelectCard)).collect(Collectors.toList())));

		diceThrown.set(true);
		if (diceValue == 7) {
			replaceThief();
			Thief.removeHalfOfCards(cards);
		}
		onChangePlayer(getCurrentPlayer());
		CatanLogger.log(row(this), CatanAction.THROW_DICE);
	}

	private boolean disableCombination(Combination combination) {
		PlayerColor key = getCurrentPlayer();
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

	private int getDirection() {
		if (turnCount == 4) {
			return 0;
		} else if (turnCount > 4 && turnCount < 8) {
			return -1;
		} else {
			return 1;
		}
	}

	private void invalidateDice() {
		diceThrown.set(!diceThrown.get());
		diceThrown.set(!diceThrown.get());
	}

	private boolean isPositioningPhase() {
		return turnCount <= 8;
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
		cards.get(getCurrentPlayer()).addAll(cardsTransfered);
		resourcesToSelect = SelectResourceType.DEFAULT;
		resourceChoices.setVisible(false);
	}

	private void onReleaseCity(MouseEvent event, City element) {
		Optional<SettlePoint> findFirst = settlePoints.stream().filter(e -> inArea(event, e))
				.filter(e -> e.isSuitableForCity(element)).findFirst();
		if (findFirst.isPresent()) {
			findFirst.get().setElement(element);
			CatanLogger.log(row(this), CatanAction.PLACE_CITY);
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
			CatanLogger.log(row(this), CatanAction.PLACE_ROAD);
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
			CatanLogger.log(row(this), CatanAction.PLACE_VILLAGE);
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
		long totalCards = cards.get(getCurrentPlayer()).stream().filter(CatanCard::isSelected).count();
		List<ResourceType> distinct = cards.get(getCurrentPlayer()).stream().filter(CatanCard::isSelected)
				.filter(e -> e.getResource() != null).map(CatanCard::getResource).distinct()
				.collect(Collectors.toList());
		boolean containsPort = Port.containsPort(distinct, totalCards, ports, currentPlayer);
		long distinctCount = distinct.size();
		getExchangeButton().setDisable((totalCards != 4 || distinctCount != 1) && !containsPort);
		DevelopmentType development = catanCard.getDevelopment();
		if (catanCard.isSelected()) {
			if (development != null) {
				onSelectDevelopment(catanCard, development);
			}
			CatanLogger.log(row(this), catanCard);
		}
		getMakeDeal().setDisable(distinctCount == 0
				|| deals.stream().filter(e -> e.getProposer() == getCurrentPlayer()).count() > 4);
	}

	private void onSelectDevelopment(CatanCard catanCard, DevelopmentType development) {
		cards.get(getCurrentPlayer()).remove(catanCard);
		usedCards.get(getCurrentPlayer()).add(development);
		switch (development) {
			case KNIGHT:
				replaceThief();
				break;
			case MONOPOLY:
				setResourceSelect(SelectResourceType.MONOPOLY);
				break;
			case ROAD_BUILDING:
				elements.add(new Road(getCurrentPlayer()));
				elements.add(new Road(getCurrentPlayer()));
				break;
			case UNIVERSITY:
				break;
			case YEAR_OF_PLENTY:
				setResourceSelect(SelectResourceType.YEAR_OF_PLENTY);
				break;
			default:
				break;
		}
		onChangePlayer(getCurrentPlayer());
		invalidateDice();
	}

	private void replaceThief() {
		terrains.stream().filter(t -> t.getThief() != null).forEach(Terrain::fadeOut);
		Parent parent = thief.getParent();
		if (parent instanceof Group) {
			((Group) parent).getChildren().remove(thief);
		}
		thief.setPlayer(getCurrentPlayer());
		if (!elements.contains(thief)) {
			elements.add(thief);
		}
	}

	private void stealResource(Terrain terrain) {
		List<PlayerColor> playersToSteal = settlePoints.stream()
				.filter(p -> p.getElement() != null && p.getTerrains().contains(terrain))
				.filter(p -> p.getElement().getPlayer() != getCurrentPlayer()).map(p -> p.getElement().getPlayer())
				.collect(Collectors.toList());
		if (!playersToSteal.isEmpty()) {
			Collections.shuffle(playersToSteal);
			List<CatanCard> list = cards.get(playersToSteal.get(0));
			Collections.shuffle(list);
			Optional<CatanCard> catanCard = list.parallelStream().filter(e -> e.getResource() != null).findFirst();
			if (catanCard.isPresent()) {
				CatanCard o = catanCard.get();
				list.remove(o);
				cards.get(getCurrentPlayer()).add(o);
			}
		}
		onChangePlayer(getCurrentPlayer());
		invalidateDice();
		CatanLogger.log(row(this), CatanAction.PLACE_THIEF);
	}

	private void updatePoints(PlayerColor newV) {
		userChart.setPoints(newV, settlePoints, usedCards, edges);
		userChart.updatePorts(newV, ports, settlePoints, currentPlayer);
		invalidateDice();
	}

}
