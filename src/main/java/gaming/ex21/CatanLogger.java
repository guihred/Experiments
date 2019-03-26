package gaming.ex21;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import ml.data.DataframeML;
import org.slf4j.Logger;
import utils.HasLogging;

public class CatanLogger {
    private static final String PLAYER = "Player";
    private static final Logger LOG = HasLogging.log();
    private static final DataframeML DATAFRAME_ML = getDataframe();

    public static void log(final CatanModel model) {
        PlayerColor playerColor = model.currentPlayer.get();
        Map<PlayerColor, List<CatanCard>> cards = model.cards;
        List<CatanCard> list = cards.get(playerColor);
        Map<ResourceType, Long> collect = list.stream().filter(e -> e.getResource() != null)
            .collect(Collectors.groupingBy(e -> e.getResource(), Collectors.counting()));

        ResourceType[] values = ResourceType.getResources();
		DATAFRAME_ML.add(PLAYER, playerColor.toString());
        for (ResourceType r : values) {
			DATAFRAME_ML.add(r.toString(), collect.getOrDefault(r, 0L));
        }
        LOG.info("{}", DATAFRAME_ML);
        
    }
    
    private static DataframeML getDataframe() {
        DataframeML dataframeML = new DataframeML();
		List<String> collect2 = Stream.of(ResourceType.getResources()).map(e -> e.toString())
				.collect(Collectors.toList());
        dataframeML.addCols(PLAYER, String.class);
        dataframeML.addCols(collect2, Long.class);
        return dataframeML;
    }
    
    
}
