package gaming.ex21;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import ml.data.DataframeML;
import org.slf4j.Logger;
import utils.HasLogging;

public class CatanLogger {
    private static final String WINNER = "WINNER";
    private static final String POINTS = "POINTS";
    private static final String PLAYER = "PLAYER";
    private static final Logger LOG = HasLogging.log();
    private static final DataframeML DATAFRAME_ML = getDataframe();

    public static void log(final CatanModel model) {
        PlayerColor playerColor = model.currentPlayer.get();
        List<CatanCard> cards = model.cards.get(playerColor);
        Map<ResourceType, Long> resourceCount = cards.stream().filter(e -> e.getResource() != null)
            .collect(Collectors.groupingBy(e -> e.getResource(), Collectors.counting()));

        DATAFRAME_ML.add(PLAYER, playerColor.toString());
        for (ResourceType r : ResourceType.getResources()) {
            DATAFRAME_ML.add(r.toString(), resourceCount.getOrDefault(r, 0L));
        }
        addCount2("VILLAGE_", model.settlePoints, Village.class);
        addCount2("CITY_", model.settlePoints, City.class);
        addCount("ROAD_", model.edges, Road.class);
        long a = model.userChart.countPoints(playerColor, model.settlePoints, model.usedCards, model.edges);
        DATAFRAME_ML.add(POINTS, a);

        PlayerColor currentWinner = PlayerColor.vals().parallelStream()
            .max(Comparator.comparing(e -> model.userChart.countPoints(e, model.settlePoints, model.usedCards, model.edges)))
            .orElse(playerColor);
        DATAFRAME_ML.add(WINNER, currentWinner);

        LOG.info("{}", DATAFRAME_ML);
        
    }

    private static void addCount(String string, List<EdgeCatan> edges, Class<Road> a) {
        Map<PlayerColor, Long> roadCount = edges.stream().filter(e -> a.isInstance(e.getElement()))
            .map(e -> e.getElement().getPlayer()).collect(Collectors.groupingBy(e -> e, Collectors.counting()));
        for (PlayerColor r : PlayerColor.values()) {
            DATAFRAME_ML.add(string + r.toString(), roadCount.getOrDefault(r, 0L));
        }
    }

    private static <T extends CatanResource> void addCount2(String string, List<SettlePoint> edges, Class<T> a) {
        Map<PlayerColor, Long> roadCount = edges.stream().filter(e -> a.isInstance(e.getElement()))
            .collect(Collectors.groupingBy(e -> e.getElement().getPlayer(), Collectors.counting()));
        for (PlayerColor r : PlayerColor.values()) {
            DATAFRAME_ML.add(string + r.toString(), roadCount.getOrDefault(r, 0L));
        }
    }
    
    private static DataframeML getDataframe() {
        DataframeML dataframeML = new DataframeML();
        List<String> resources = Stream.of(ResourceType.getResources())
            .map(ResourceType::toString)
			.collect(Collectors.toList());
        dataframeML.addCols(PLAYER, String.class);
        dataframeML.addCols(POINTS, Long.class);
        dataframeML.addCols(WINNER, Long.class);
        dataframeML.addCols(resources, Long.class);

        for (PlayerColor r : PlayerColor.values()) {
            dataframeML.addCols("CITY_" + r.toString(), Long.class);
            dataframeML.addCols("VILLAGE_" + r.toString(), Long.class);
            dataframeML.addCols("ROAD_" + r.toString(), Long.class);
        }

        return dataframeML;
    }
    
    
}
