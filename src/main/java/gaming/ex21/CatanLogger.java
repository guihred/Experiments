package gaming.ex21;

import java.io.File;
import java.util.Map;
import ml.data.DataframeML;
import org.slf4j.Logger;
import utils.CSVUtils;
import utils.ResourceFXUtils;
import utils.ex.HasLogging;

public final class CatanLogger {
    private static final Logger LOGGER = HasLogging.log();
    public static final String CATAN_LOG = "txt/catan_log.txt";
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
            log(model, CatanAction.action(catanCard.getDevelopment()));
        } else {
            log(model, CatanAction.action(catanCard.getResource()));
        }
    }

    public static void log(Map<String, Object> model, Combination combination) {
        log(model, CatanAction.action(combination));
    }

    public static void log(Map<String, Object> model, ResourceType catanCard) {
        log(model, CatanAction.actionResource(catanCard));
    }

    public static void winner(PlayerColor playerWinner) {
        DATAFRAME_ML.filter(CatanLogBuilder.PLAYER, c -> c.equals(playerWinner.toString()));
        File outFile = ResourceFXUtils.getOutFile(CATAN_LOG);
        DATAFRAME_ML.forEachRow(c -> CSVUtils.appendLine(outFile, c));
    }

}
