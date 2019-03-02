package gaming.ex21;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Toggle;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import simplebuilder.SimpleToggleGroupBuilder;
import utils.CommonsFX;
import utils.ResourceFXUtils;

public class CatanModel {
    private List<Terrain> terrains = new ArrayList<>();
    private List<SettlePoint> settlePoints = new ArrayList<>();
	private List<EdgeCatan> edges;
	private Map<PlayerColor, List<CatanCard>> cards = FXCollections.observableHashMap();
	private ObjectProperty<PlayerColor> currentPlayer = new SimpleObjectProperty<>();
	private ObservableList<Node> elements = FXCollections.observableArrayList();
    private DragContext dragContext = new DragContext();
	private BooleanProperty diceThrown = new SimpleBooleanProperty(false);
    private StackPane center;

	private Group cardGroup = new Group();

	private ImageView userImage = new ImageView(
			CatanResource
			.convertImage(new Image(ResourceFXUtils.toExternalForm("catan/user.png")), Color.BLUE));
	

	private Dice dice1 = new Dice();

	private Dice dice2 = new Dice();

    private int turnCount;

    private Random random = new Random();
    private Button exchangeButton = new Button("Exchange");

    private HBox resourceChoices = createResourceChoices();

    private Node addCombinations() {
		GridPane value = new GridPane();
		Combination[] combinations = Combination.values();
		for (int i = 0; i < combinations.length; i++) {
			Combination combination = combinations[i];
			List<ResourceType> resources = combination.getResources();
			ImageView el = new ImageView(ResourceFXUtils.toExternalForm("catan/" + combination.getElement()));
			el.setFitWidth(30);
			el.setFitHeight(30);
			el.setPreserveRatio(true);
			value.addRow(i, CommonsFX.newButton(el, "" + combination, e -> onCombinationClicked(combination)));
            for (ResourceType resourceType : resources) {
                ImageView e2 = new ImageView(ResourceFXUtils.toExternalForm("catan/" + resourceType.getPure()));
                e2.setFitWidth(20);
                e2.setPreserveRatio(true);
				value.addRow(i, e2);
            }
		}
		return value;
	}

    private void addTerrains(final StackPane root) {
		List<Integer> numbers = getNumbers();
		List<ResourceType> cells = createResources();
		double radius = Terrain.RADIUS * Math.sqrt(3);
		for (int i = 3, j = 0, l = 0; j < cells.size(); j += i, i += j > 11 ? -1 : 1, l++) {
			List<ResourceType> resources = cells.subList(j, j + i);
			for (int k = 0; k < resources.size(); k++) {
				Terrain terrain = new Terrain(resources.get(k));
				double f = -radius / 2 * (i - 3);
				double x = radius * k + f + radius;
				double y = radius * l * Math.sqrt(3) / 2;
				terrain.relocate(x, y);
				if (resources.get(k) != ResourceType.DESERT) {
					terrain.setNumber(numbers.remove(0));
				}
				createSettlePoints(terrain, x, y);
				terrains.add(terrain);
				root.getChildren().add(terrain);
			}
		}

		root.setManaged(false);
		edges = settlePoints.stream().flatMap(s -> s.getNeighbors().stream().map(t -> new EdgeCatan(s, t))).distinct()
				.collect(Collectors.toList());
		edges.forEach(e -> e.getPoints().forEach(p -> p.getEdges().add(e)));

		root.getChildren().addAll(edges);
		root.getChildren().addAll(settlePoints);

	}

    private boolean containsEnough(final Combination combination, final List<CatanCard> list) {
        List<ResourceType> resources = list.stream().map(CatanCard::getResource).filter(Objects::nonNull)
				.collect(Collectors.toList());
		
        List<ResourceType> resourcesNecessary = combination.getResources().stream().collect(Collectors.toList());
        for (int i = 0; i < resourcesNecessary.size(); i++) {
            boolean remove = resources.remove(resourcesNecessary.get(i));
			if(!remove) {
				return false;
			}
		}
		return true;
	}
    private HBox createResourceChoices() {
        SimpleToggleGroupBuilder group = new SimpleToggleGroupBuilder();
        for (ResourceType type : ResourceType.values()) {
            if (type.getPure() != null) {
                ImageView node = new ImageView(ResourceFXUtils.toExternalForm("catan/" + type.getPure()));
                node.setFitWidth(20);
                node.setPreserveRatio(true);
                group.addToggle(node,type);
            }
        }
        HBox res = new HBox(group.getTogglesAs(Node.class).toArray(new Node[0]));
        res.setVisible(false);
        group.onChange((ob, old, n) -> onSelectResource(group, res, n));
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

    private boolean edgeAcceptRoad(final EdgeCatan edge, final Road road) {
        return edge.matchColor(road.getPlayer())
				|| edge.getPoints().stream().anyMatch(p -> p.getEdges().stream()
						.anyMatch(e -> e.getElement() != null && e.getElement().getPlayer() == road.getPlayer()));
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
            if (dragContext.element instanceof Village) {
                if (dragContext.point != null) {
                    dragContext.point.toggleFade(1);
                    dragContext.point = null;
                }
                settlePoints.stream().filter(e -> inArea(event, e)).findFirst()
                        .ifPresent(e -> dragContext.point = e.toggleFade(-1));
            }
			if (dragContext.element instanceof City) {
				if (dragContext.point != null) {
					dragContext.point.toggleFade(1);
					dragContext.point = null;
				}
				settlePoints
						.stream().filter(e -> isSuitableForCity(event, e))
						.findFirst().ifPresent(e -> dragContext.point = e.toggleFade(-1));
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
		if (node instanceof CatanResource && center.equals(node.getParent())) {
			dragContext.element = (CatanResource) node;
			elements.remove(node);
		}
	}

	private void handleMouseReleased(final MouseEvent event) {
        if (dragContext.element instanceof Village) {
            Village village = (Village) dragContext.element;
            Optional<SettlePoint> findFirst = settlePoints.stream()
                    .filter(e -> inArea(event, e))
                    .filter(t -> !t.isPointDisabled())
                    .filter(t -> isPositioningPhase() || pointAcceptVillage(t, village))
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
        if (dragContext.element instanceof City) {
            Optional<SettlePoint> findFirst = settlePoints.stream()
                    .filter(e -> inArea(event, e))
                    .filter(t -> !t.isPointDisabled())
                    .filter(e -> isSuitableForCity(event, e)).findFirst();
            if (findFirst.isPresent()) {
        		findFirst.get().setElement(dragContext.element);
        	} else {
        		elements.add(0, dragContext.element);
        	}
        	if (dragContext.point != null) {
        		dragContext.point.toggleFade(1);
        		dragContext.point = null;
        	}
        	dragContext.element = null;
        }
        if (dragContext.element instanceof Road) {
            Road road = (Road) dragContext.element;
            Optional<EdgeCatan> edgeHovered = edges.stream()
                    .filter(e -> inArea(event, e))
                    .filter(e -> edgeAcceptRoad(e, road))
                    .findFirst();
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
    }

    private boolean inArea(final MouseEvent event, final Node e) {
        return e.getBoundsInParent().contains(event.getSceneX(), event.getSceneY());
    }

    private void initialize(final StackPane center1, final Pane right) {
        center = center1;
        addTerrains(center1);
		elements.addListener(this::onElementsChange);
		for (PlayerColor playerColor : PlayerColor.values()) {
            cards.put(playerColor, new ArrayList<>());
		}
		currentPlayer.addListener((ob, old, newV) -> onChangePlayer(newV));
		right.getChildren().add(userImage);
		right.getChildren().add(cardGroup);
		Button skipButton = CommonsFX.newButton("Skip Turn", e -> onSkipTurn());
		skipButton.disableProperty().bind(diceThrown.not());

		Button throwButton = CommonsFX.newButton("Throw Dices", e -> throwDice());
		throwButton.disableProperty().bind(diceThrown);
        right.getChildren().add(new HBox(skipButton, throwButton, exchangeButton));
        exchangeButton.setOnAction(e -> resourceChoices.setVisible(true));
		right.getChildren().add(new HBox(dice1, dice2));
        right.getChildren().add(resourceChoices);
		right.getChildren().add(addCombinations());
		currentPlayer.set(PlayerColor.BLUE);
        onSkipTurn();
	}

    private boolean intersects(final SettlePoint p, final SettlePoint e) {
        return e.getBoundsInParent().intersects(p.getBoundsInParent());
    }

	private boolean isPositioningPhase() {
        return turnCount <= 8;
    }

	private boolean isSuitableForCity(final MouseEvent event, final SettlePoint e) {
		return inArea(event, e) && e.getElement() != null && e.getElement().getPlayer() == currentPlayer.get();
	}

	private CatanResource makeDraggable(final CatanResource e) {
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

		userImage.setFitWidth(100);
		userImage.setPreserveRatio(true);
		userImage.setImage(CatanResource.convertImage(new Image(ResourceFXUtils.toExternalForm("catan/user.png")),
				newV.getColor()));
		cardGroup.getChildren().clear();
		List<CatanCard> currentCards = cards.get(currentPlayer.get());
		for (CatanCard type : currentCards) {
			cardGroup.getChildren().add(type);
		}
		Collection<List<CatanCard>> values = currentCards.stream()
				.filter(e -> e.getResource() != null)
				.collect(Collectors.groupingBy(CatanCard::getResource)).values().stream().collect(Collectors.toList());
		double layoutX = 0;
		double layoutY = 0;
		List<CatanCard> collect = currentCards.stream().filter(e -> e.getResource() == null).collect(Collectors.toList());
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
					DevelopmentType[] values = DevelopmentType.values();
                    DevelopmentType type = values[random.nextInt(values.length)];
                    list.add(newCard(type));
					break;

				default:
					break;
			}
		}
		onChangePlayer(currentPlayer.get());
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

    private void onSelectCard(final CatanCard catanCard) {
        catanCard.setSelected(!catanCard.isSelected());
        long count = cards.get(currentPlayer.get()).stream().filter(CatanCard::isSelected).count();
        long c = cards.get(currentPlayer.get()).stream().filter(CatanCard::isSelected)
                .filter(e -> e.getResource() != null).map(CatanCard::getResource)
                .distinct().count();
        exchangeButton.setDisable(count != 4 || c != 1);


    }

    private void onSelectResource(final SimpleToggleGroupBuilder group, final HBox res, final Toggle n) {
        if(n==null) {
            return;
        }
        cards.get(currentPlayer.get()).removeIf(CatanCard::isSelected);
        cards.get(currentPlayer.get()).add(newCard((ResourceType) n.getUserData()));
        onChangePlayer(currentPlayer.get());
        res.setVisible(false);
        group.select(null);
        exchangeButton.setDisable(true);
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
            settlePoints.stream().filter(e -> e.getElement() != null)
                    .forEach(e -> cards.get(e.getElement().getPlayer()).addAll(
                            e.getTerrains().stream().map(Terrain::getType).filter(t -> t != ResourceType.DESERT)
                                    .map(this::newCard).collect(Collectors.toList())));
            onChangePlayer(currentPlayer.get());
        }

	}
    private boolean pointAcceptVillage(final SettlePoint point, final Village village) {

        return point.getElement() == null && point.getEdges().stream()
                .anyMatch(e -> e.getElement() != null && e.getElement().getPlayer() == village.getPlayer());
    }

	private void throwDice() {
		int a = dice1.throwDice() + dice2.throwDice();
		settlePoints.stream().filter(e -> e.getElement() != null)
                .flatMap((final SettlePoint e) -> e.getElement() instanceof City ? Stream.of(e, e) : Stream.of(e))
				.forEach(e -> cards.get(e.getElement().getPlayer())
						.addAll(e.getTerrains().stream().filter(t -> t.getNumber() == a)
                                .map(t -> newCard(t.getType())).collect(Collectors.toList())));
		onChangePlayer(currentPlayer.get());
		diceThrown.set(true);
	}


	public static void create(final StackPane root, final Pane value) {
        new CatanModel().initialize(root, value);
    }
}
