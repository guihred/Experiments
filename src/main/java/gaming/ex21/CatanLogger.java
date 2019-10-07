package gaming.ex21;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.collections.ObservableList;
import ml.data.CSVUtils;
import ml.data.DataframeML;
import org.slf4j.Logger;
import utils.HasLogging;
import utils.ResourceFXUtils;

public final class CatanLogger {
    public static final Logger LOGGER = HasLogging.log();
    private static final String CATAN_LOG = "catan_log.txt";
    private static final String SELECT_RESOURCE = "SELECT_RESOURCE";
    private static final String HAS_DEAL = "HAS_DEAL";
    private static final String WINNER = "WINNER";
    private static final String POINTS = "POINTS";
    private static final String PLAYER = "PLAYER";
    private static final String ACTION = "ACTION";
    private static final List<Class<? extends CatanResource>> HAS_CLASSES = Arrays.asList(Thief.class, City.class,
        Village.class, Road.class);
    private static final DataframeML DATAFRAME_ML = getDataframe();

    private CatanLogger() {
    }

    public static void log(Map<String, Object> row, CatanAction action) {
        row.put(ACTION, action);
        DATAFRAME_ML.add(row);
        LOGGER.trace("{}", row);
        CSVUtils.appendLine(ResourceFXUtils.getOutFile(CATAN_LOG), row);
    }

    public static void log(Map<String, Object> model, CatanCard catanCard) {
        if (catanCard.getDevelopment() != null) {
            log(model, action(catanCard.getDevelopment()));
        } else {
            log(model, action(catanCard.getResource()));
        }
    }

    public static void log(Map<String, Object> model, Combination combination) {
        log(model, action(combination));
    }

    public static void log(Map<String, Object> model, ResourceType catanCard) {
        log(model, actionResource(catanCard));
    }

    public static void winner(PlayerColor playerWinner) {
        DATAFRAME_ML.filter(PLAYER, c -> c.equals(playerWinner.toString()));
        File outFile = ResourceFXUtils.getOutFile(CATAN_LOG);
        DATAFRAME_ML.forEachRow(c -> CSVUtils.appendLine(outFile, c));
    }

    static Map<String, Object> row(PlayerColor playerColor, Map<PlayerColor, List<CatanCard>> cards2,
        UserChart userChart, Map<PlayerColor, List<DevelopmentType>> usedCards, List<SettlePoint> settlePoints,
        List<EdgeCatan> edges, ObservableList<Deal> deals, SelectResourceType resourcesToSelect,
        ObservableList<CatanResource> elements) {
        Map<String, Object> currentState = new LinkedHashMap<>();
        List<CatanCard> cards = cards2.get(playerColor);
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
        currentState.put(WINNER, userChart.getWinner(settlePoints, usedCards, edges, cards2));
        boolean anyMatch = deals.stream().anyMatch(m -> !Deal.isDealUnfeasible(m, playerColor, cards2));
        currentState.put(HAS_DEAL, Objects.toString(anyMatch));
        currentState.put(SELECT_RESOURCE, Objects.toString(resourcesToSelect));

        for (Class<? extends CatanResource> class1 : HAS_CLASSES) {
            currentState.put("HAS_" + class1.getSimpleName().toUpperCase(),
                Objects.toString(elements.stream().anyMatch(class1::isInstance)));
        }
        return currentState;
    }

    private static CatanAction action(Combination combination) {
        return CatanAction.getAction("BUY_", combination);
    }

    private static CatanAction action(DevelopmentType development) {
        return CatanAction.getAction("SELECT_", development);
    }

    private static CatanAction action(ResourceType resource) {
        return CatanAction.getAction("SELECT_", resource);
    }

    private static CatanAction actionResource(ResourceType resource) {
        return CatanAction.getAction("RESOURCE_", resource);
    }

    private static DataframeML getDataframe() {
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
