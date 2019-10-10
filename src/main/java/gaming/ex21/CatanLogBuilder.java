package gaming.ex21;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.collections.ObservableList;
import ml.data.DataframeML;

public class CatanLogBuilder {
    private static final String SELECT_RESOURCE = "SELECT_RESOURCE";
    private static final String HAS_DEAL = "HAS_DEAL";
    private static final String WINNER = "WINNER";
    private static final String POINTS = "POINTS";
    public static final String PLAYER = "PLAYER";
    public static final String ACTION = "ACTION";
    private static final List<Class<? extends CatanResource>> HAS_CLASSES = Arrays.asList(Thief.class, City.class,
        Village.class, Road.class);

    private Map<String, Object> currentState = new LinkedHashMap<>();

    private PlayerColor playerColor;
    private Map<PlayerColor, List<CatanCard>> allCards;
    private UserChart userChart;
    private Map<PlayerColor, List<DevelopmentType>> usedCards;
    private List<SettlePoint> settlePoints;
    private List<EdgeCatan> edges;
    private ObservableList<Deal> deals;
    private SelectResourceType resourcesToSelect;
    private ObservableList<CatanResource> elements;

    public CatanLogBuilder allCards(Map<PlayerColor, List<CatanCard>> cards2) {
        allCards = cards2;
        return this;
    }

    public Map<String, Object> build() {
        List<CatanCard> cards = allCards.get(playerColor);
        Map<String, Long> resourceCount = cards.stream()
            .map(e -> Objects.toString(e.getResource(), Objects.toString(e.getDevelopment())))
            .collect(Collectors.groupingBy(e -> e, Collectors.counting()));
        currentState.put(PLAYER, playerColor.toString());
        currentState.put(POINTS, userChart.countPoints(playerColor, settlePoints, usedCards, edges));
        for (ResourceType r : ResourceType.getResources()) {
            currentState.put(r.toString(), resourceCount.getOrDefault(r.toString(), 0L));
        }
        for (DevelopmentType r : DevelopmentType.values()) {
            currentState.put(r.toString(), resourceCount.getOrDefault(r.toString(), 0L));
        }
        currentState.put(WINNER, userChart.getWinner(settlePoints, usedCards, edges, allCards));
        boolean anyMatch = deals.stream().anyMatch(m -> !Deal.isDealUnfeasible(m, playerColor, allCards));
        currentState.put(HAS_DEAL, Objects.toString(anyMatch));
        currentState.put(SELECT_RESOURCE, Objects.toString(resourcesToSelect));

        for (Class<? extends CatanResource> class1 : HAS_CLASSES) {
            currentState.put("HAS_" + class1.getSimpleName().toUpperCase(),
                Objects.toString(elements.stream().anyMatch(class1::isInstance)));
        }
        return currentState;
    }

    public CatanLogBuilder deals(ObservableList<Deal> deals1) {
        deals = deals1;
        return this;
    }

    public CatanLogBuilder edges(List<EdgeCatan> edges1) {
        edges = edges1;
        return this;
    }

    public CatanLogBuilder elements(ObservableList<CatanResource> elements1) {
        elements = elements1;
        return this;
    }

    public CatanLogBuilder playerColor(PlayerColor playerColor1) {
        playerColor = playerColor1;
        return this;
    }

    public CatanLogBuilder resourcesToSelect(SelectResourceType resourcesToSelect1) {
        resourcesToSelect = resourcesToSelect1;
        return this;
    }

    public CatanLogBuilder settlePoints(List<SettlePoint> settlePoints) {
        this.settlePoints = settlePoints;
        return this;
    }

    public CatanLogBuilder usedCards(Map<PlayerColor, List<DevelopmentType>> usedCards1) {
        usedCards = usedCards1;
        return this;
    }

    public CatanLogBuilder userChart(UserChart userChart1) {
        userChart = userChart1;
        return this;
    }

    public static DataframeML getDataframe() {
        DataframeML dataframeML = new DataframeML();
        dataframeML.addCols(PLAYER, String.class);
        dataframeML.addCols(ACTION, String.class);
        dataframeML.addCols(WINNER, String.class);
        dataframeML.addCols(SELECT_RESOURCE, String.class);
        dataframeML.addCols(HAS_DEAL, String.class);
        dataframeML.addCols(POINTS, Long.class);
        for (Class<? extends CatanResource> class1 : HAS_CLASSES) {
            dataframeML.addCols("HAS_" + class1.getSimpleName().toUpperCase(), String.class);
        }

        List<String> resources = Stream.concat(Stream.of(ResourceType.getResources()).map(ResourceType::toString),
            Stream.of(DevelopmentType.values()).map(DevelopmentType::toString)).collect(Collectors.toList());
        dataframeML.addCols(resources, Long.class);

        return dataframeML;
    }

}