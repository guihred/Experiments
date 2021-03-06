
package fxtests;

import gaming.ex21.*;
import java.util.*;
import java.util.stream.Collectors;
import javafx.geometry.Pos;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;
import ml.data.DataframeBuilder;
import ml.data.DataframeML;
import ml.data.DecisionNode;
import ml.data.DecisionTree;
import org.junit.Test;
import utils.ResourceFXUtils;

public class FXEngineCatanTest extends AbstractTestExecution {

    private static final int MAX_TRIES = 100;

    @Test
    public void testAllOptions() {
        show(CatanAppMain.class);
        List<EdgeCatan> allEdge = lookupList(EdgeCatan.class);
        Collections.shuffle(allEdge);
        List<Village> allVillages = new ArrayList<>();
        List<City> cities = new ArrayList<>();
        List<SettlePoint> settlePoints = lookupList(SettlePoint.class);
        List<Road> allRoads = new ArrayList<>();
        Collections.shuffle(settlePoints);
        List<ButtonBase> allButtons =
                lookup(".button").queryAllAs(ButtonBase.class).stream().collect(Collectors.toList());
        allButtons.addAll(lookup(".toggle-button").queryAllAs(ToggleButton.class));
        Set<ButtonBase> clickedButtons = new HashSet<>();
        List<Terrain> allTerrains = lookupList(Terrain.class);
        Collections.shuffle(allTerrains);
        for (int i = 0; i < MAX_TRIES && allButtons.stream().anyMatch(b -> !clickedButtons.contains(b)); i++) {
            clickVillages(allVillages, settlePoints);
            clickCities(cities);
            clickRoads(allEdge, allRoads);
            clickThiefs(allTerrains);
            clickCards();
            clickButton(allButtons, clickedButtons);
            if (i % 10 == 0) {
                getLogger().info("{}/{}-{}/{}", i + 1, MAX_TRIES, clickedButtons.size(), allButtons.size());
            }
        }
    }

    @Test
    public void testDecisionTree() {
        CatanAppMain newInstance = show(CatanAppMain.class);
        DataframeML build = DataframeBuilder.builder(ResourceFXUtils.getOutFile(CatanLogger.CATAN_LOG)).build();
        build.removeCol("WINNER", "PLAYER");
        List<Object> list = build.list("ACTION");
        list.add(list.remove(0));
        DecisionNode decisionTree = DecisionTree.buildTree(build, "ACTION", 0.0005);

        List<EdgeCatan> allEdge = lookupList(EdgeCatan.class);
        Collections.shuffle(allEdge);
        List<Village> allVillages = new ArrayList<>();
        List<City> cities = new ArrayList<>();
        List<SettlePoint> settlePoints = lookupList(SettlePoint.class);
        List<Road> allRoads = new ArrayList<>();
        Collections.shuffle(settlePoints);
        List<ButtonBase> allButtons =
                lookup(".button").queryAllAs(ButtonBase.class).stream().collect(Collectors.toList());
        allButtons.addAll(lookup(".toggle-button").queryAllAs(ToggleButton.class));
        Set<ButtonBase> clickedButtons = new HashSet<>();
        List<Terrain> allTerrains = lookupList(Terrain.class);
        Collections.shuffle(allTerrains);
        CatanModel model = newInstance.getModel();
        int j = MAX_TRIES;
        for (int i = 0; i < j; i++) {
            // while (model.getCurrentPlayer() == PlayerColor.GREEN) {
            // sleep(1000);
            // }
            if (model.anyPlayerPoints(9)) {
                break;
            }

            CatanAction action = chooseAction(decisionTree, model);
            boolean makeDecision = makeDecision(action, allEdge, allVillages, cities, settlePoints, allRoads,
                    allTerrains, allButtons, clickedButtons);
            if (makeDecision) {
                if (i % 10 == 0) {
                    getLogger().info("{}/{}", i + 1, j);
                }
            } else {
                CatanAction randomAction = getRandomAction();
                boolean decision = makeDecision(randomAction, allEdge, allVillages, cities, settlePoints, allRoads,
                        allTerrains, allButtons, clickedButtons);
                if (decision) {
                    getLogger().info("{}/{} {}", i + 1, j, randomAction);
                } else {
                    i--;
                }
            }
        }
        
        PlayerColor playerWinner = model.getPlayerWinner();
        CatanLogger.winner(playerWinner);
        getLogger().info("{}", decisionTree);

    }

    @Test
    public void testDevelopment() {
        CatanAppMain show = show(CatanAppMain.class);
        List<EdgeCatan> allEdge = lookupList(EdgeCatan.class);
        Collections.shuffle(allEdge);
        List<Village> allVillages = new ArrayList<>();
        List<City> cities = new ArrayList<>();
        List<SettlePoint> settlePoints = lookupList(SettlePoint.class);
        List<Road> allRoads = new ArrayList<>();
        Collections.shuffle(settlePoints);
        List<ButtonBase> allButtons =
                lookup(".button").queryAllAs(ButtonBase.class).stream().collect(Collectors.toList());
        allButtons.addAll(lookup(".toggle-button").queryAllAs(ToggleButton.class));
        Set<ButtonBase> clickedButtons = new HashSet<>();
        List<Terrain> allTerrains = lookupList(Terrain.class);
        Collections.shuffle(allTerrains);
        List<DevelopmentType> developments = new ArrayList<>(Arrays.asList(DevelopmentType.values()));
        for (int i = 0; i < MAX_TRIES && allButtons.stream().anyMatch(b -> !clickedButtons.contains(b)); i++) {
            clickVillages(allVillages, settlePoints);
            clickCities(cities);
            clickRoads(allEdge, allRoads);
            clickThiefs(allTerrains);
            clickCards();
            clickButton(allButtons, clickedButtons);
            if (developments.isEmpty()) {
                break;
            }
            chooseDevelopment(show, developments);
            
            if (i % 10 == 0) {
                getLogger().info("{}/{}-{}/{}", i + 1, MAX_TRIES, clickedButtons.size(), allButtons.size());
            }
        }
    }

    @Test
    @SuppressWarnings("static-method")
    public void testExtraPoint() {
        ExtraPoint extraPoint = new ExtraPoint("largestarmy.png");
        extraPoint.getUrl();
        extraPoint.setRecord(5);
        extraPoint.getRecord();
    }

    private void chooseDevelopment(CatanAppMain show, List<DevelopmentType> asList) {
        Set<CatanCard> allCards = lookup(CatanCard.class);
        if(!allCards.isEmpty()) {
            DevelopmentType randomItem = randomRemoveItem(asList);
            interact(() -> show.getModel().onSelectDevelopment(randomItem(allCards), randomItem));
        }
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

        Set<CatanCard> allCards = lookup(CatanCard.class);
        if (allCards.stream().filter(CatanCard::isSelected).count() > 4) {
            return false;
        }
        List<CatanCard> cardsToSelect = new ArrayList<>(allCards);
        Collections.shuffle(cardsToSelect);
        cardsToSelect.sort(Comparator.comparing((CatanCard c) -> matches(btn, c))
                .thenComparing(c -> orderByResources(cardsToSelect, c)));
        cardsToSelect.removeIf(CatanCard::isSelected);
        if ((nextBoolean() || btn != null) && !cardsToSelect.isEmpty()) {
            targetPos(Pos.TOP_CENTER);
            clickOn(cardsToSelect.remove(0));
            if (nextBoolean() && !cardsToSelect.isEmpty()
                    && allCards.stream().filter(CatanCard::isSelected).count() < 4) {
                clickOn(cardsToSelect.remove(0));
            }
            targetPos(Pos.CENTER);
            return true;
        }
        return false;
    }


    private boolean clickCities(final List<City> cities) {
        List<City> notClickedVillages = lookupList(City.class, t -> !cities.contains(t));
        if (notClickedVillages.isEmpty()) {
            return false;
        }
        List<SettlePoint> settlePoints = lookupList(SettlePoint.class);
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
        List<Road> filter = lookupList(Road.class, r -> !allRoads.contains(r));
        for (Road e : filter) {
            moveTo(e);
            drag(e, MouseButton.PRIMARY);
            moveTo(getEdge(e, allEdge));
            drop();
            if (!(e.getParent() instanceof StackPane)) {
                allRoads.add(e);
            }
        }
        return !filter.isEmpty();
    }

    private boolean clickThiefs(final List<Terrain> queryAllAs) {
        Collections.shuffle(queryAllAs);
        List<Thief> filter = lookupList(Thief.class, e -> e.getParent() instanceof StackPane);
        for (Thief e : filter) {
            moveTo(e);
            drag(e, MouseButton.PRIMARY);
            queryAllAs.parallelStream().findAny().ifPresent(this::moveTo);
            drop();
        }
        return !filter.isEmpty();
    }

    private boolean clickVillages(final List<Village> allVillages, final List<SettlePoint> settlePoints) {
        List<Village> notClickedVillages = lookupList(Village.class, v -> !allVillages.contains(v));
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

    private CatanAction getRandomAction() {
        return randomEnum(CatanAction.class);
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
            case BUY_DEVELOPMENT:
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

    private void tryToClick(final ButtonBase t) {
        try {
            clickOn(t);
        } catch (Exception e1) {
            getLogger().trace("{} not clicked", t, e1);
            getLogger().error("{} not clicked", t);
        }
    }

    private static CatanAction chooseAction(DecisionNode buildTree, CatanModel model) {
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
        return CatanAction.valueOf(Objects.toString(buildTree.predict(model.row())));
    }

    private static String getCardClass(CatanAction predict) {
        return predict.toString().replaceAll("SELECT_", "").replaceAll("_", " ").toLowerCase();
    }

    private static EdgeCatan getEdge(final Road road, final List<EdgeCatan> allEdge) {
        return allEdge.parallelStream().filter(e -> e.edgeAcceptRoad(road)).findFirst().orElse(allEdge.get(0));
    }

    private static boolean matches(String btn, ButtonBase c) {
        if (btn == null) {
            return false;
        }
        return !Objects.toString(c.getStyleClass()).toLowerCase().contains(btn)
                && !Objects.toString(c.getStyleClass()).toLowerCase().contains(btn.replaceAll(" ", "-"))
                && !Objects.toString(c.getId()).toLowerCase().contains(btn)
                && !Objects.toString(c.getText(), "").toLowerCase().contains(btn);
    }

    private static boolean matches(String btn, CatanCard c) {
        if (btn == null) {
            return false;
        }
        String s = btn.replaceAll(" ", "-") + "-card";
        return !Objects.toString(c.getStyleClass(), "").contains(s) && !Objects.toString(c.getId(), "").contains(s);
    }

    private static Long orderByResources(List<CatanCard> cardsToSelect, final CatanCard c) {
        return -cardsToSelect.stream().filter(e -> e.getResource() == c.getResource()).filter(CatanCard::isSelected)
                .count();
    }
}
