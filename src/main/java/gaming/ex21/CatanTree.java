package gaming.ex21;

import java.util.*;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javafx.collections.ObservableList;
import utils.SupplierEx;

public class CatanTree {

    private static final int MAX_DEPTH = 20;
    private Map<PlayerColor, List<CatanCard>> cards = PlayerColor.newMapList();
    private int[] terrainsNumbers;
    private CatanResource[] settlePoints;
    private List<EdgeCatan> edges;
    private Map<PlayerColor, List<DevelopmentType>> usedCards = PlayerColor.newMapList();
    private PlayerColor currentPlayer;
    private CatanResource[] elements;
    private boolean diceThrown;
    private SelectResourceType resourcesToSelect;
    private List<Port> ports;
    private int turnCount;
    private ObservableList<Deal> deals;
    private boolean makeDeal;
    private DevelopmentType[] developmentCards;
    private List<ResourceType> terrainsResources;
    private boolean resourceChoices;
    private boolean exchangeDisabled;
    private Map<PlayerColor, Long> playersPoints = new EnumMap<>(PlayerColor.class);
    private Entry<CatanAction, Object> action;
    private int depth;

    List<CatanTree> children;
    private List<Boolean> terrainsAvailable;
    private CatanResource[] roads;

    public CatanTree(CatanVariables var) {
        var.getCards().forEach((k, v) -> cards.get(k).addAll(v));
        terrainsNumbers = var.getTerrains().stream().mapToInt(Terrain::getNumber).toArray();
        terrainsResources = var.getTerrains().stream().map(Terrain::getType).collect(Collectors.toList());
        terrainsAvailable = var.getTerrains().stream().map(e -> e.getThief() == null).collect(Collectors.toList());
        settlePoints = var.getSettlePoints().stream().map(SettlePoint::getElement).toArray(CatanResource[]::new);
        edges = var.getEdges();
        roads = var.getEdges().stream().map(EdgeCatan::getElement).toArray(i -> new CatanResource[i]);
        elements = var.getElements().toArray(new CatanResource[0]);
        currentPlayer = var.getCurrentPlayer();
        diceThrown = var.getDiceThrown().get();
        resourcesToSelect = var.getResourcesToSelect();
        var.getUsedCards().forEach((k, v) -> usedCards.get(k).addAll(v));
        makeDeal = var.makeDeal.isDisabled();
        developmentCards = var.developmentCards.toArray(new DevelopmentType[0]);
        ports = var.getPorts();
        deals = var.getDeals();
        turnCount = var.turnCount;
        resourceChoices = var.resourceChoices.isVisible();
        exchangeDisabled = var.exchangeButton.isDisable();
        var.userChart.getPlayersPoints().forEach((k, v) -> playersPoints.put(k, v.get()));
    }

    public Map.Entry<CatanAction, Object> getAction() {
        return action;
    }

    public CatanTree makeDecision(PlayerColor player) {
        double max = Double.NEGATIVE_INFINITY;
        CatanTree decision = null;
        for (CatanTree e : getChildren()) {
            double maxValue = e.maxValue(player, depth + MAX_DEPTH);
            if (maxValue > max) {
                max = maxValue;
                decision = e;
            }
        }
        return decision;
    }

    private List<Entry<CatanAction, Integer>> actions(PlayerColor player) {
        List<Entry<CatanAction, Integer>> actions = new ArrayList<>();

//        EXCHANGE,
//        MAKE_DEAL,
//        THROW_DICE,
//        SKIP_TURN,
//        ACCEPT_DEAL,
        if (Stream.of(elements).anyMatch(e -> e instanceof Thief)) {
            // PLACE_THIEF,
            List<SimpleEntry<CatanAction, Integer>> collect = IntStream.range(0, terrainsResources.size())
                .filter(e -> terrainsResources.get(e) != ResourceType.DESERT)
                .filter(e -> terrainsAvailable.get(e))
                .mapToObj(e -> new AbstractMap.SimpleEntry<>(CatanAction.PLACE_THIEF, e)).collect(Collectors.toList());
            actions.addAll(collect);
        }
        if (Stream.of(elements).anyMatch(e -> e instanceof Village)) {
//        PLACE_VILLAGE,
//            List<SimpleEntry<CatanAction, Integer>> collect = IntStream.range(0, settlePoints.length)
//                .filter(e -> settlePoints[e] == null&&CatanHelper.isPositioningPhase(turnCount)
//                    ||edges.get(0).edgeAcceptRoad(edge, road)
//                    ).filter(e -> terrainsAvailable.get(e))
//                .mapToObj(e -> new AbstractMap.SimpleEntry<>(CatanAction.PLACE_VILLAGE, e))
//                .collect(Collectors.toList());
//            actions.addAll(collect);
        }
//        PLACE_ROAD,
//        PLACE_CITY,
//
//        BUY_VILLAGE,
//        BUY_ROAD,
//        BUY_CITY,
//        BUY_DEVELOPMENT,
//
//        SELECT_KNIGHT,
//        SELECT_MONOPOLY,
//        SELECT_ROAD_BUILDING,
//        SELECT_UNIVERSITY,
//        SELECT_YEAR_OF_PLENTY,
//
//        SELECT_BRICK,
//        SELECT_ROCK,
//        SELECT_SHEEP,
//        SELECT_WHEAT,
//        SELECT_WOOD,
//
//        RESOURCE_BRICK,
//        RESOURCE_ROCK,
//        RESOURCE_SHEEP,
//        RESOURCE_WHEAT,
//        RESOURCE_WOOD;

        return actions;
    }

    private List<CatanTree> getChildren() {
        return SupplierEx.orElse(children, () -> {
            children = new ArrayList<>();
            PlayerColor player = currentPlayer;
            List<Map.Entry<CatanAction, Integer>> actions = actions(player);
            for (Map.Entry<CatanAction, Integer> j : actions) {
                children.add(result(player, j));
            }
            Collections.shuffle(children);
            return children;
        });
    }

    private double maxValue(PlayerColor player, int i) {

        if (i > depth) {
            return utility(player);
        }
        double v = 0;
        List<CatanTree> children2 = getChildren();
        for (CatanTree a : children2) {
            v += a.maxValue(player, i);
        }
        return v / children2.size();
    }

    private CatanTree result(PlayerColor player, Entry<CatanAction, Integer> j) {
        return null;
    }

    private double utility(PlayerColor player) {
        return playersPoints.entrySet().stream()
            .mapToDouble(e -> (e.getKey() == player ? 1 : -1) * e.getValue().doubleValue()).sum();
    }

}
