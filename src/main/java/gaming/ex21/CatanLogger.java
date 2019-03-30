package gaming.ex21;

import java.io.File;
import java.io.FileWriter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import ml.data.DataframeML;
import org.slf4j.Logger;
import utils.HasLogging;
import utils.ResourceFXUtils;

public class CatanLogger {
    private static final String ROAD = "ROAD_";
    private static final String VILLAGE = "VILLAGE_";
    private static final String CITY = "CITY_";
    private static final String WINNER = "WINNER";
    private static final String POINTS = "POINTS_";
    private static final String PLAYER = "PLAYER";
    private static final String ACTION = "ACTION";
    private static final Logger LOG = HasLogging.log();
    private static final DataframeML DATAFRAME_ML = getDataframe();

    public static void log(CatanModel model, CatanAction action) {
        PlayerColor playerColor = model.currentPlayer.get();
        List<CatanCard> cards = model.cards.get(playerColor);
        Map<String, Long> resourceCount = cards.stream()
            .map(e -> Objects.toString(e.getResource(), Objects.toString(e.getDevelopment())))
            .collect(Collectors.groupingBy(e -> e, Collectors.counting()));

        DATAFRAME_ML.add(PLAYER, playerColor.toString());
        for (ResourceType r : ResourceType.getResources()) {
            DATAFRAME_ML.add(r.toString(), resourceCount.getOrDefault(r.toString(), 0L));
        }
        for (DevelopmentType r : DevelopmentType.values()) {
            DATAFRAME_ML.add(r.toString(), resourceCount.getOrDefault(r.toString(), 0L));
        }
        addCount2(VILLAGE, model.settlePoints, Village.class);
        addCount2(CITY, model.settlePoints, City.class);
        addCount(ROAD, model.edges, Road.class);
        model.userChart.countPoints(playerColor, model.settlePoints, model.usedCards, model.edges);

        PlayerColor.vals().forEach(color -> DATAFRAME_ML.add(POINTS + color.toString(),
            model.userChart.countPoints(color, model.settlePoints, model.usedCards, model.edges)));

        PlayerColor currentWinner = PlayerColor.vals().parallelStream()
            .max(Comparator
                .comparing((PlayerColor e) 
                    -> model.userChart.countPoints(e, model.settlePoints, model.usedCards, model.edges))
                .thenComparing((PlayerColor e) -> model.cards.get(e).size()))
            .orElse(playerColor);
        DATAFRAME_ML.add(WINNER, currentWinner);
        DATAFRAME_ML.add(ACTION, action);

        Map<String, Object> rowMap = DATAFRAME_ML.rowMap(DATAFRAME_ML.getSize() - 1);
        LOG.info("{}", rowMap);

        appendLine(rowMap);

    }

    public static void log(CatanModel model, CatanCard catanCard) {
        if (catanCard.getDevelopment() != null) {
            log(model, action(catanCard.getDevelopment()));
        } else {
            log(model, action(catanCard.getResource()));
        }
    }

    public static void log(CatanModel model, Combination combination) {
        log(model, action(combination));
    }

    private static CatanAction action(Combination combination) {
        switch (combination) {
            case CITY:
                return CatanAction.BUY_CITY;
            case DEVELOPMENT:
                return CatanAction.BUY_DELEVOPMENT;
            case ROAD:
                return CatanAction.BUY_ROAD;
            case VILLAGE:
                return CatanAction.BUY_VILLAGE;
            default:
                break;
        }
        return null;
    }

    private static CatanAction action(DevelopmentType development) {
        switch (development) {
            case KNIGHT:
                return CatanAction.SELECT_KNIGHT;
            case MONOPOLY:
                return CatanAction.SELECT_MONOPOLY;
            case ROAD_BUILDING:
                return CatanAction.SELECT_ROAD_BUILDING;
            case UNIVERSITY:
                return CatanAction.SELECT_UNIVERSITY;
            case YEAR_OF_PLENTY:
                return CatanAction.SELECT_YEAR_OF_PLENTY;
            default:
                return null;
        }
    }

    private static CatanAction action(ResourceType resource) {
        switch (resource) {
            case BRICK:
                return CatanAction.SELECT_BRICK;
            case ROCK:
                return CatanAction.SELECT_ROCK;
            case SHEEP:
                return CatanAction.SELECT_SHEEP;
            case WHEAT:
                return CatanAction.SELECT_WHEAT;
            case WOOD:
                return CatanAction.SELECT_WOOD;
            default:
                break;
        }
        return null;
    }

    private static void addCount(String string, List<EdgeCatan> edges, Class<Road> a) {
        Map<PlayerColor, Long> roadCount = edges.stream().filter(e -> a.isInstance(e.getElement()))
            .map(e -> e.getElement().getPlayer()).collect(Collectors.groupingBy(e -> e, Collectors.counting()));
        for (PlayerColor r : PlayerColor.values()) {
            DATAFRAME_ML.add(string + r.toString(), roadCount.getOrDefault(r, 0L));
        }
    }

    private static <T extends CatanResource> void addCount2(String string, List<SettlePoint> edges,
        Class<T> catanResourceType) {
        Map<PlayerColor, Long> roadCount = edges.stream().filter(e -> catanResourceType.isInstance(e.getElement()))
            .collect(Collectors.groupingBy(e -> e.getElement().getPlayer(), Collectors.counting()));
        for (PlayerColor r : PlayerColor.values()) {
            DATAFRAME_ML.add(string + r.toString(), roadCount.getOrDefault(r, 0L));
        }
    }

    private static void appendLine(Map<String, Object> rowMap) {
        File file = new File(ResourceFXUtils.getOutFile(), "catan_log.txt");
        boolean exists = file.exists();
        try (FileWriter fw = new FileWriter(file, true);) {
            if (!exists) {
                fw.append(DATAFRAME_ML.cols().stream().collect(Collectors.joining(",", "", "\n")));
            }
            fw.append(rowMap.values().stream().map(Object::toString).collect(Collectors.joining(",", "", "\n")));
        } catch (Exception e1) {
            LOG.info("{}", e1);
        }
    }

    private static DataframeML getDataframe() {
        DataframeML dataframeML = new DataframeML();
        dataframeML.addCols(PLAYER, String.class);
        dataframeML.addCols(ACTION, String.class);
        dataframeML.addCols(WINNER, String.class);

        List<String> resources = Stream.concat(Stream.of(ResourceType.getResources()).map(ResourceType::toString),
            Stream.of(DevelopmentType.values()).map(DevelopmentType::toString)).collect(Collectors.toList());
        dataframeML.addCols(resources, Long.class);

        for (PlayerColor r : PlayerColor.values()) {
            dataframeML.addCols(CITY + r.toString(), Long.class);
            dataframeML.addCols(VILLAGE + r.toString(), Long.class);
            dataframeML.addCols(ROAD + r.toString(), Long.class);
            dataframeML.addCols(POINTS + r.toString(), Long.class);
        }

        return dataframeML;
    }

}
