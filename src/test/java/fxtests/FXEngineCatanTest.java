package fxtests;

import gaming.ex21.*;
import java.util.*;
import java.util.stream.Collectors;
import javafx.geometry.Pos;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;
import ml.data.DataframeML;
import ml.data.DecisionTree;
import ml.data.DecisionTree.DecisionNode;
import org.junit.Test;

public class FXEngineCatanTest extends AbstractTestExecution {

    private static final int MAX_TRIES = 400;

    private Random random = new Random();

//    @Test
    public void testaToolsVerify() throws Exception {
        show(CatanApp.class);
        List<EdgeCatan> allEdge = lookup(EdgeCatan.class::isInstance).queryAllAs(EdgeCatan.class).stream()
            .collect(Collectors.toList());
        Collections.shuffle(allEdge);
        List<Village> allVillages = new ArrayList<>();
        List<City> cities = new ArrayList<>();
        List<SettlePoint> settlePoints = lookup(SettlePoint.class::isInstance).queryAllAs(SettlePoint.class).stream()
            .collect(Collectors.toList());
        List<Road> allRoads = new ArrayList<>();
        Collections.shuffle(settlePoints);
        List<ButtonBase> allButtons = lookup(".button").queryAllAs(ButtonBase.class).stream()
            .collect(Collectors.toList());
        allButtons.addAll(lookup(".toggle-button").queryAllAs(ToggleButton.class));
        Set<ButtonBase> clickedButtons = new HashSet<>();
        List<Terrain> allTerrains = lookup(Terrain.class::isInstance).queryAllAs(Terrain.class).stream()
            .collect(Collectors.toList());
        Collections.shuffle(allTerrains);
        for (int i = 0; i < MAX_TRIES && allButtons.stream().anyMatch(b -> !clickedButtons.contains(b)); i++) {
            clickVillages(allVillages, settlePoints);
            clickCities(cities);
            clickRoads(allEdge, allRoads);
            clickThiefs(allTerrains);
            clickCards();
            clickButton(allButtons, clickedButtons);
            getLogger().trace("{}/{}-{}/{}", i + 1, MAX_TRIES, clickedButtons.size(), allButtons.size());
        }
    }

    @Test
    public void testDecisions() throws Exception {
        CatanApp newInstance = show(CatanApp.class);
        DataframeML build = DataframeML.builder("out/catan_log.txt").build();
        build.removeCol("WINNER", "PLAYER");
        List<Object> list = build.list("ACTION");
        list.add(list.remove(0));
        DecisionNode decisionTree = DecisionTree.buildTree(build, "ACTION");
        getLogger().info("{}", decisionTree);

        List<EdgeCatan> allEdge = lookup(EdgeCatan.class::isInstance).queryAllAs(EdgeCatan.class).stream()
            .collect(Collectors.toList());
        Collections.shuffle(allEdge);
        List<Village> allVillages = new ArrayList<>();
        List<City> cities = new ArrayList<>();
        List<SettlePoint> settlePoints = lookup(SettlePoint.class::isInstance).queryAllAs(SettlePoint.class).stream()
            .collect(Collectors.toList());
        List<Road> allRoads = new ArrayList<>();
        Collections.shuffle(settlePoints);
        List<ButtonBase> allButtons = lookup(".button").queryAllAs(ButtonBase.class).stream()
            .collect(Collectors.toList());
        allButtons.addAll(lookup(".toggle-button").queryAllAs(ToggleButton.class));
        Set<ButtonBase> clickedButtons = new HashSet<>();
        List<Terrain> allTerrains = lookup(Terrain.class::isInstance).queryAllAs(Terrain.class).stream()
            .collect(Collectors.toList());
        Collections.shuffle(allTerrains);
        CatanModel model = newInstance.getModel();
        for (int i = 0; i < MAX_TRIES * 2; i++) {
            // while (model.getCurrentPlayer() == PlayerColor.GREEN) {
            // sleep(1000);
            // }
            if (model.getUserChart().anyPlayerPoints(9, model)) {
                return;
            }

            CatanAction action = chooseAction(decisionTree, model);
            boolean makeDecision = makeDecision(action, allEdge, allVillages, cities, settlePoints, allRoads,
                allTerrains, allButtons, clickedButtons);
            if (makeDecision) {
                getLogger().info("{}/{}", i + 1, MAX_TRIES * 2);
            } else {
                getLogger().info("{}", action);
                makeDecision(getRandomAction(), allEdge, allVillages, cities, settlePoints, allRoads, allTerrains, allButtons,
                    clickedButtons);
                i--;
            }
        }
    }

    private CatanAction chooseAction(DecisionNode buildTree, CatanModel model) {
        Map<String, Object> row = CatanLogger.row(model);
        if (model.getElements().stream().anyMatch(Village.class::isInstance)) {
            return CatanAction.PLACE_VILLAGE;
        }
        if (model.getElements().stream().anyMatch(City.class::isInstance)) {
            return CatanAction.PLACE_CITY;
        }
        if (model.getElements().stream().anyMatch(Thief.class::isInstance)) {
            return CatanAction.PLACE_THIEF;
        }
        if (model.getElements().stream().anyMatch(Road.class::isInstance)) {
            return CatanAction.PLACE_ROAD;
        }
        CatanAction valueOf = CatanAction.valueOf(Objects.toString(buildTree.predict(row)));
        String cardClass = getCardClass(valueOf);
        if (valueOf.name().startsWith("SELECT_") && getCards().stream().noneMatch(e -> !matches(cardClass, e))) {
            return getRandomAction();
        }
        return valueOf;
    }

    private void clickButton(List<ButtonBase> allButtons, Set<ButtonBase> clickedButtons) {
        clickButton(null, allButtons, clickedButtons);
    }

    private boolean clickButton(String btn, List<ButtonBase> allButtons, Set<ButtonBase> clickedButtons) {
        Collections.shuffle(allButtons);
        allButtons.addAll(lookup(".button").queryAllAs(ButtonBase.class).stream().filter(t -> !allButtons.contains(t))
            .collect(Collectors.toList()));
        Optional<ButtonBase> buttonToPress = allButtons.stream().filter(e -> !e.isDisabled())
            .filter(e -> e.getParent() != null).filter(ButtonBase::isVisible).filter(e -> e.getParent().isVisible())
            .sorted(Comparator.comparing((ButtonBase c) -> matches(btn, c)).thenComparing(clickedButtons::contains))
            .findFirst();
        buttonToPress.ifPresent(t -> {
            clickedButtons.add(t);
            tryToClick(t);
        });
        allButtons.removeIf(e -> e.getParent() == null);
        clickedButtons.removeIf(e -> e.getParent() == null);
        return buttonToPress.isPresent();
    }

    private void clickCards() {
        clickCards(null);
    }

    private boolean clickCards(String btn) {

        List<CatanCard> allCards = getCards();
        if (allCards.stream().filter(CatanCard::isSelected).count() > 4) {
            return false;
        }
        List<CatanCard> cardsToSelect = new ArrayList<>(allCards);
        Collections.shuffle(cardsToSelect);
        cardsToSelect.sort(Comparator.comparing((CatanCard c) -> matches(btn, c))
            .thenComparing(c -> orderByResources(cardsToSelect, c)));
        cardsToSelect.removeIf(CatanCard::isSelected);
        if ((random.nextBoolean() || btn != null) && !cardsToSelect.isEmpty()) {
            targetPos(Pos.TOP_CENTER);
            clickOn(cardsToSelect.remove(0));
            if (random.nextBoolean() && !cardsToSelect.isEmpty()
                && allCards.stream().filter(CatanCard::isSelected).count() < 4) {
                clickOn(cardsToSelect.remove(0));
            }
            targetPos(Pos.CENTER);
            return true;
        }
        return false;
    }

    private boolean clickCities(final List<City> cities) {
        List<City> notClickedVillages = lookup(City.class::isInstance).queryAllAs(City.class).stream()
            .filter(t -> !cities.contains(t)).collect(Collectors.toList());
        if (notClickedVillages.isEmpty()) {
            return false;
        }
        List<SettlePoint> settlePoints = lookup(SettlePoint.class::isInstance).queryAllAs(SettlePoint.class)
            .parallelStream().collect(Collectors.toList());
        City next = notClickedVillages.remove(0);
        drag(next, MouseButton.PRIMARY);
        SettlePoint remove = settlePoints.parallelStream().filter(e -> e.isSuitableForCity(next)).findAny()
            .orElse(settlePoints.get(0));
        moveTo(remove);
        drop();
        cities.add(next);
        return true;
    }

    private boolean clickRoads(final List<EdgeCatan> allEdge, final List<Road> allRoads) {
        Road[] filter = lookup(Road.class::isInstance).queryAllAs(Road.class).stream()
            .filter(r -> !allRoads.contains(r)).toArray(Road[]::new);
        for (Road e : filter) {
            moveTo(e);
            drag(e, MouseButton.PRIMARY);
            moveTo(getEdge(e, allEdge));
            drop();
            if (!(e.getParent() instanceof StackPane)) {
                allRoads.add(e);
            }
        }
        return filter.length > 0;
    }

    private boolean clickThiefs(final List<Terrain> queryAllAs) {
        Collections.shuffle(queryAllAs);
        Thief[] filter = lookup(Thief.class::isInstance).queryAllAs(Thief.class).stream()
            .filter(e -> e.getParent() instanceof StackPane).toArray(Thief[]::new);
        for (Thief e : filter) {
            moveTo(e);
            drag(e, MouseButton.PRIMARY);
            queryAllAs.parallelStream().findAny().ifPresent(this::moveTo);
            drop();
        }
        return filter.length > 0;
    }

    private boolean clickVillages(final List<Village> allVillages, final List<SettlePoint> settlePoints) {
        List<Village> notClickedVillages = lookup(Village.class::isInstance).queryAllAs(Village.class).stream()
            .filter(v -> !allVillages.contains(v)).collect(Collectors.toList());
        Collections.shuffle(notClickedVillages);
        if (notClickedVillages.isEmpty()) {
            return false;
        }

        Village next = notClickedVillages.remove(0);
        drag(next, MouseButton.PRIMARY);
        Collections.shuffle(settlePoints);
        SettlePoint remove = settlePoints.parallelStream().filter(e -> e.pointAcceptVillage(next)).findAny()
            .orElse(settlePoints.get(0));
        moveTo(remove);
        drop();
        if (next.getParent() instanceof StackPane) {
            return false;
        }
        settlePoints.remove(remove);
        settlePoints.removeAll(remove.getNeighbors());
        allVillages.add(next);
        return true;
    }

    private String getCardClass(CatanAction predict) {
        return predict.toString().replaceAll("SELECT_", "").replaceAll("_", " ").toLowerCase();
    }

    private List<CatanCard> getCards() {
        return lookup(CatanCard.class::isInstance).queryAllAs(CatanCard.class).stream()
            .collect(Collectors.toList());
    }

    private EdgeCatan getEdge(final Road road, final List<EdgeCatan> allEdge) {
        return allEdge.parallelStream().filter(e -> e.edgeAcceptRoad(road)).findFirst().orElse(allEdge.get(0));
    }

    private CatanAction getRandomAction() {
        CatanAction[] values = CatanAction.values();
        return values[random.nextInt(values.length)];
    }

    private boolean makeDecision(CatanAction predict, List<EdgeCatan> allEdge, List<Village> allVillages,
        List<City> cities, List<SettlePoint> settlePoints, List<Road> allRoads, List<Terrain> allTerrains,
        List<ButtonBase> allButtons, Set<ButtonBase> clickedButtons) {

        switch (predict) {
            case PLACE_CITY:
                return clickCities(cities);
            case PLACE_ROAD:
                return clickRoads(allEdge, allRoads);
            case PLACE_THIEF:
                return clickThiefs(allTerrains);
            case PLACE_VILLAGE:
                return clickVillages(allVillages, settlePoints);
            case ACCEPT_DEAL:
            case BUY_CITY:
            case BUY_DELEVOPMENT:
            case BUY_ROAD:
            case BUY_VILLAGE:
            case SKIP_TURN:
            case THROW_DICE:
            case EXCHANGE:
            case MAKE_DEAL:
            case RESOURCE_BRICK:
            case RESOURCE_ROCK:
            case RESOURCE_SHEEP:
            case RESOURCE_WHEAT:
            case RESOURCE_WOOD:
                return clickButton(predict.toString().replaceAll("BUY_", "").replaceAll("RESOURCE_", "")
                    .replaceAll("_", " ").toLowerCase(), allButtons, clickedButtons);
            case SELECT_BRICK:
            case SELECT_ROCK:
            case SELECT_SHEEP:
            case SELECT_WHEAT:
            case SELECT_WOOD:
            case SELECT_KNIGHT:
            case SELECT_MONOPOLY:
            case SELECT_ROAD_BUILDING:
            case SELECT_UNIVERSITY:
            case SELECT_YEAR_OF_PLENTY:
                return clickCards(getCardClass(predict));
            default:
                break;

        }
        return false;
    }

    private boolean matches(String btn, ButtonBase c) {
        if (btn == null) {
            return false;
        }
        return !Objects.toString(c.getStyleClass()).toLowerCase().contains(btn)
            && !Objects.toString(c.getStyleClass()).toLowerCase().contains(btn.replaceAll(" ", "-"))
            && !Objects.toString(c.getId()).toLowerCase().contains(btn)
            && !Objects.toString(c.getText(), "").toLowerCase().contains(btn);
    }

    private boolean matches(String btn, CatanCard c) {
        if (btn == null) {
            return false;
        }
        String s = btn.replaceAll(" ", "-") + "-card";
        return !Objects.toString(c.getStyleClass(), "").contains(s) && !Objects.toString(c.getId(), "").contains(s);
    }

    private Long orderByResources(List<CatanCard> cardsToSelect, final CatanCard c) {
        return -cardsToSelect.stream().filter(e -> e.getResource() == c.getResource()).filter(CatanCard::isSelected)
            .count();
    }

    private void tryToClick(final ButtonBase t) {
        try {
            clickOn(t);
        } catch (Exception e1) {
            getLogger().trace("{} not clicked", t, e1);
            getLogger().error("{} not clicked", t);
        }
    }
}
