package gaming.ex21;

import graphs.entities.Edge;
import java.util.*;
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import simplebuilder.SimpleToggleGroupBuilder;
import utils.CommonsFX;
import utils.ResourceFXUtils;

public class CatanModel {
    private static final String CATAN = "catan/";
    private List<Terrain> terrains = new ArrayList<>();
    private List<SettlePoint> settlePoints = new ArrayList<>();
    private List<EdgeCatan> edges;
    private Map<PlayerColor, List<CatanCard>> cards = newMapList();
    private Map<PlayerColor, List<DevelopmentType>> usedCards = newMapList();
    private ObjectProperty<PlayerColor> currentPlayer = new SimpleObjectProperty<>();
    private ObservableList<Node> elements = FXCollections.observableArrayList();
    private DragContext dragContext = new DragContext();
    private BooleanProperty diceThrown = new SimpleBooleanProperty(false);
    private StackPane center;
    private int resourcesToSelect = 0;
    private Group cardGroup = new Group();
    private Text userPoints = new Text("0");
    private ImageView userImage = new ImageView(
            CatanResource.convertImage(new Image(ResourceFXUtils.toExternalForm("catan/user.png")), Color.BLUE));
    private Dice dice1 = new Dice();
    private Dice dice2 = new Dice();
    private int turnCount;
    private int layX = -Terrain.RADIUS / 2, layY = -Terrain.RADIUS / 2;
    private Button exchangeButton = new Button("Exchange");
    private HBox resourceChoices = createResourceChoices();
    private Thief thief = makeDraggable(new Thief());

    private List<Port> ports = Stream.of(ResourceType.values())
            .flatMap(t -> Stream.generate(() -> t).limit(t == ResourceType.DESERT ? 4 : 1)).map(Port::new)
            .collect(Collectors.toList());

    private Circle circle = new Circle(2);

    protected void onKeyPressed(KeyEvent e, double radius) {
        KeyCode code = e.getCode();
        int x = 0, y = 0;
        switch (code) {
            case UP:
                y--;
                break;
            case DOWN:
                y++;
                break;
            case RIGHT:
                x++;
                break;
            case LEFT:
                x--;
                break;
            default:
                break;
        }
        layX += x;
        layY += y;
        circle.setLayoutX(circle.getLayoutX() + x);
        circle.setLayoutY(circle.getLayoutY() + y);

        relocatePorts(radius);
    }

    private Node addCombinations() {
        GridPane value = new GridPane();
        Combination[] combinations = Combination.values();
        for (int i = 0; i < combinations.length; i++) {
            Combination combination = combinations[i];
            List<ResourceType> resources = combination.getResources();
            ImageView el = new ImageView(ResourceFXUtils.toExternalForm(CATAN + combination.getElement()));
            el.setFitWidth(30);
            el.setFitHeight(30);
            el.setPreserveRatio(true);
            Button button = CommonsFX.newButton(el, "" + combination, e -> onCombinationClicked(combination));
            button.disableProperty().bind(
                    Bindings.createBooleanBinding(() -> disableCombination(combination), currentPlayer, diceThrown));
            value.addRow(i, button);
            for (ResourceType resourceType : resources) {
                ImageView e2 = new ImageView(ResourceFXUtils.toExternalForm(CATAN + resourceType.getPure()));
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
        root.getChildren().addAll(ports);
        root.getChildren().addAll(edges);
        root.getChildren().addAll(settlePoints);
//        root.getScene().addEventFilter(KeyEvent.KEY_PRESSED, e -> onKeyPressed(e, radius));

    }

    private boolean containsEnough(final Combination combination, final List<CatanCard> list) {
        List<ResourceType> resources = list.stream().map(CatanCard::getResource).filter(Objects::nonNull)
                .collect(Collectors.toList());

        List<ResourceType> resourcesNecessary = combination.getResources().stream().collect(Collectors.toList());
        for (int i = 0; i < resourcesNecessary.size(); i++) {
            if (!resources.remove(resourcesNecessary.get(i))) {
                return false;
            }
        }
        return true;
    }

    private String countPoints(final PlayerColor newV) {
        long count = settlePoints.stream().filter(s -> s.getElement() instanceof Village)
                .filter(e -> e.getElement().getPlayer() == newV).count();
        count += settlePoints.stream().filter(s -> s.getElement() instanceof City)
                .filter(e -> e.getElement().getPlayer() == newV).count() * 2;
        count += usedCards.get(newV).stream().filter(e -> e == DevelopmentType.UNIVERSITY).count();
        return "" + count;
    }

    private HBox createResourceChoices() {
        SimpleToggleGroupBuilder group = new SimpleToggleGroupBuilder();
        for (ResourceType type : ResourceType.values()) {
            if (type.getPure() != null) {
                ImageView node = new ImageView(ResourceFXUtils.toExternalForm(CATAN + type.getPure()));
                node.setFitWidth(20);
                node.setPreserveRatio(true);
                group.addToggle(node, type);
            }
        }
        HBox res = new HBox(group.getTogglesAs(Node.class).toArray(new Node[0]));
        res.setVisible(false);
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
        List<CatanCard> list = cards.get(currentPlayer.get());
        if (list == null) {
            return true;
        }
        return !containsEnough(combination, list);
    }

    private boolean edgeAcceptRoad(final EdgeCatan edge, final Road road) {
        return edge.getElement() == null
                && (edge.matchColor(road.getPlayer()) || edge.getPoints().stream().anyMatch(p -> p.getEdges().stream()
                        .anyMatch(e -> e.getElement() != null && e.getElement().getPlayer() == road.getPlayer())));
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
                settlePoints.stream().filter(e -> isSuitableForCity(event, e)).findFirst()
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

    private void initialize(final StackPane center1, final Pane right) {
        center = center1;
        addTerrains(center1);
        elements.addListener(this::onElementsChange);
        currentPlayer.addListener((ob, old, newV) -> onChangePlayer(newV));
        right.getChildren().add(new HBox(userImage, userPoints));
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

    private void invalidateDice() {
        diceThrown.set(!diceThrown.get());
        diceThrown.set(!diceThrown.get());
    }

    private boolean isPositioningPhase() {
        return turnCount <= 8;
    }

    private boolean isSuitableForCity(final MouseEvent event, final SettlePoint e) {
        return inArea(event, e) && e.getElement() != null && e.getElement().getPlayer() == currentPlayer.get();
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
        userImage.setFitWidth(100);
        userImage.setPreserveRatio(true);
        userImage.setImage(CatanResource.convertImage(new Image(ResourceFXUtils.toExternalForm("catan/user.png")),
                newV.getColor()));
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
                    list.add(newCard(DevelopmentType.randomDevelopment()));
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

    private void onReleaseCity(final MouseEvent event, final City element) {
        Optional<SettlePoint> findFirst = settlePoints.stream().filter(e -> inArea(event, e))
                .filter(t -> !t.isPointDisabled()).filter(e -> isSuitableForCity(event, e)).findFirst();
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
            edgeHovered.get().setThief(thief);
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
        long c = cards.get(currentPlayer.get()).stream().filter(CatanCard::isSelected)
                .filter(e -> e.getResource() != null).map(CatanCard::getResource).distinct().count();
        exchangeButton.setDisable(count != 4 || c != 1);
        if (catanCard.isSelected() && catanCard.getDevelopment() != null) {
            cards.get(currentPlayer.get()).remove(catanCard);
            usedCards.get(currentPlayer.get()).add(catanCard.getDevelopment());
            switch (catanCard.getDevelopment()) {
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

    }

    private void onSelectResource(final SimpleToggleGroupBuilder group, final Toggle n) {
        if (n == null) {
            return;
        }
        ResourceType selectedType = (ResourceType) n.getUserData();
        if (resourcesToSelect == 3) {
            List<CatanCard> collect = new ArrayList<>();
            PlayerColor[] values = PlayerColor.values();
            for (PlayerColor color : values) {
                List<CatanCard> collect2 = cards.get(color).stream().filter(e -> e.getResource() == selectedType)
                        .collect(Collectors.toList());
                cards.get(color).removeAll(collect2);
                collect.addAll(collect2);
            }
            cards.get(currentPlayer.get()).addAll(collect);
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

    }

    private boolean pointAcceptVillage(final SettlePoint point, final Village village) {

        return point.getElement() == null && point.getEdges().stream()
                .anyMatch(e -> e.getElement() != null && e.getElement().getPlayer() == village.getPlayer());
    }

    private void relocatePorts(double radius) {
        
        List<SettlePoint> s = settlePoints.stream().collect(Collectors.toList());
        List<List<SettlePoint>> collect = s.stream()
                .flatMap(p0 -> s.stream().filter(p1 -> !Objects.equals(p1, p0))
                        .filter(p1 -> p1.getNeighbors().contains(p0)).map(p1 -> new HashSet<>(Arrays.asList(p0, p1))))
                .filter(p0 -> p0.stream().anyMatch(p -> p.getNeighbors().size() == 2)).distinct().map(ArrayList::new)
                .collect(Collectors.toList());
        Collections.shuffle(s);
        for (int j = 0; j < ports.size() && j < collect.size(); j++) {
            Port port = ports.get(j);
            List<SettlePoint> list = collect.get(j);
            Optional<SettlePoint> first = list.stream().filter(l -> l.getNeighbors().size() == 3).findFirst();
            collect.removeIf(p -> p.contains(list.get(0)));
            if (first.isPresent()) {
                ObservableList<SettlePoint> neighbors = first.get().getNeighbors()
                        .filtered(p -> p.getNeighbors().size() == 2);
                double x = neighbors.stream().mapToDouble(SettlePoint::getLayoutX).average().orElse(0);
                double y = neighbors.stream().mapToDouble(SettlePoint::getLayoutY).average().orElse(0);
                port.relocate(x + layX, y + layY);
            } else {
                double x = list.stream().mapToDouble(SettlePoint::getLayoutX).average().orElse(0);
                double y = list.stream().mapToDouble(SettlePoint::getLayoutY).average().orElse(0);
                double angulo = Edge.getAngulo(circle.getLayoutX(), circle.getLayoutY(), x, y);
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
        elements.add(thief);
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
        userPoints.setText(countPoints(newV));
    }

    public static void create(final StackPane root, final Pane value) {
        new CatanModel().initialize(root, value);
    }

    private static <T> ObservableMap<PlayerColor, List<T>> newMapList() {
        ObservableMap<PlayerColor, List<T>> observableHashMap = FXCollections.observableHashMap();

        for (PlayerColor playerColor : PlayerColor.values()) {
            observableHashMap.put(playerColor, new ArrayList<>());
        }
        return observableHashMap;
    }
}
