package gaming.ex21;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import ml.data.DataframeML;
import org.apache.commons.io.output.FileWriterWithEncoding;
import org.assertj.core.util.Files;
import org.slf4j.Logger;
import utils.HasLogging;
import utils.ResourceFXUtils;

public final class CatanLogger {
	private static final String CATAN_LOG = "catan_log.txt";
	private static final String SELECT_RESOURCE = "SELECT_RESOURCE";
	private static final String HAS_DEAL = "HAS_DEAL";
	private static final String WINNER = "WINNER";
    private static final String POINTS = "POINTS";
	private static final String PLAYER = "PLAYER";
	private static final String ACTION = "ACTION";
	private static final Logger LOG = HasLogging.log();
    private static final List<Class<? extends CatanResource>> HAS_CLASSES = Arrays.asList(Thief.class, City.class,
			Village.class, Road.class);
	private static final DataframeML DATAFRAME_ML = getDataframe();

	private CatanLogger() {
	}

	public static void log(Map<String, Object> row, CatanAction action) {
		row.put(ACTION, action);
        DATAFRAME_ML.add(row);
        LOG.trace("{}", row);
        appendLine(row);
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

	public static Map<String, Object> row(CatanModel model) {
		PlayerColor playerColor = model.getCurrentPlayer();
		Map<String, Object> currentState = new LinkedHashMap<>();
		List<CatanCard> cards = model.getCards().get(playerColor);
		Map<String, Long> resourceCount = cards.stream()
				.map(e -> Objects.toString(e.getResource(), Objects.toString(e.getDevelopment())))
				.collect(Collectors.groupingBy(e -> e, Collectors.counting()));
		currentState.put(PLAYER, playerColor.toString());
        currentState.put(POINTS,
				model.getUserChart().countPoints(playerColor, model.getSettlePoints(), model.getUsedCards(),
						model.getEdges()));
		for (ResourceType r : ResourceType.getResources()) {
			currentState.put(r.toString(), resourceCount.getOrDefault(r.toString(), 0L));
		}
		for (DevelopmentType r : DevelopmentType.values()) {
			currentState.put(r.toString(), resourceCount.getOrDefault(r.toString(), 0L));
		}
		PlayerColor currentWinner = PlayerColor.vals().parallelStream().max(Comparator.comparing(
				(PlayerColor e) -> model.getUserChart().countPoints(e, model.getSettlePoints(), model.getUsedCards(),
						model.getEdges()))
				.thenComparing((PlayerColor e) -> model.getCards().get(e).size())).orElse(playerColor);
		currentState.put(WINNER, currentWinner);
		boolean anyMatch = model.getDeals().stream().anyMatch(m -> !model.isDealUnfeasible(m));
		currentState.put(HAS_DEAL, Objects.toString(anyMatch));
		currentState.put(SELECT_RESOURCE, Objects.toString(model.getResourcesToSelect()));
		
		for (Class<? extends CatanResource> class1 : HAS_CLASSES) {
			currentState.put("HAS_" + class1.getSimpleName().toUpperCase(),
					Objects.toString(model.getElements().stream().anyMatch(class1::isInstance)));
		}
		return currentState;
	}

	public static void winner(PlayerColor playerWinner) {
		DATAFRAME_ML.filter(PLAYER, c->c.equals(playerWinner.toString()));
		DATAFRAME_ML.forEachRow(CatanLogger::appendLine);
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

	private static void appendLine(Map<String, Object> rowMap) {
        File file = ResourceFXUtils.getOutFile(CATAN_LOG);
		boolean exists = file.exists();
		String collect = DATAFRAME_ML.cols().stream().collect(Collectors.joining(",", "", ""));
		if (exists) {
			try (Scanner scanner = new Scanner(file, StandardCharsets.UTF_8.displayName())) {
				String nextLine = scanner.nextLine();
				if (!collect.equals(nextLine)) {
					exists = false;
				}
			} catch (Exception e) {
				LOG.error("{}", e);
			}
			if (!exists) {
                Files.delete(file);
			}
		}

		try (FileWriterWithEncoding fw = new FileWriterWithEncoding(file, StandardCharsets.UTF_8, true)) {
			if (!exists) {
				fw.append(collect + "\n");
			}
            List<String> cols = DATAFRAME_ML.cols().stream().collect(Collectors.toList());

            fw.append(rowMap.entrySet().stream().sorted(Comparator.comparing(t -> cols.indexOf(t.getKey())))
                .map(Entry<String, Object>::getValue).map(Object::toString).collect(Collectors.joining(",", "", "\n")));
		} catch (Exception e1) {
			LOG.error("{}", e1);
		}
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

		List<String> resources = Stream
				.concat(Stream.of(ResourceType.getResources()).map(ResourceType::toString),
						Stream.of(DevelopmentType.values()).map(DevelopmentType::toString))
				.collect(Collectors.toList());
		dataframeML.addCols(resources, Long.class);

		return dataframeML;
	}

}
