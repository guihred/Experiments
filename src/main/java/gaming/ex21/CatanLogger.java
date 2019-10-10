package gaming.ex21;

import java.io.File;
import java.util.*;
import ml.data.CSVUtils;
import ml.data.DataframeML;
import org.slf4j.Logger;
import utils.HasLogging;
import utils.ResourceFXUtils;

public final class CatanLogger {
    private static final Logger LOGGER = HasLogging.log();
    private static final String CATAN_LOG = "catan_log.txt";
    private static final DataframeML DATAFRAME_ML = CatanLogBuilder.getDataframe();

    private CatanLogger() {
    }

    public static void log(Map<String, Object> row, CatanAction action) {
        row.put(CatanLogBuilder.ACTION, action);
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
        DATAFRAME_ML.filter(CatanLogBuilder.PLAYER, c -> c.equals(playerWinner.toString()));
        File outFile = ResourceFXUtils.getOutFile(CATAN_LOG);
        DATAFRAME_ML.forEachRow(c -> CSVUtils.appendLine(outFile, c));
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

}
