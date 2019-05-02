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

    public static void addCount(String string, Collection<EdgeCatan> edges, Class<Road> a) {
		Map<PlayerColor, Long> roadCount = edges.stream().filter(e -> a.isInstance(e.getElement()))
				.map(e -> e.getElement().getPlayer()).collect(Collectors.groupingBy(e -> e, Collectors.counting()));
		for (PlayerColor r : PlayerColor.values()) {
			DATAFRAME_ML.add(string + r.toString(), roadCount.getOrDefault(r, 0L));
		}
	}

    public static <T extends CatanResource> void addCount2(String string, Collection<SettlePoint> edges,
			Class<T> catanResourceType) {
		Map<PlayerColor, Long> roadCount = edges.stream().filter(e -> catanResourceType.isInstance(e.getElement()))
				.collect(Collectors.groupingBy(e -> e.getElement().getPlayer(), Collectors.counting()));
		for (PlayerColor r : PlayerColor.values()) {
			DATAFRAME_ML.add(string + r.toString(), roadCount.getOrDefault(r, 0L));
		}
	}

	public static void log(CatanModel model, CatanAction action) {
        Map<String, Object> row = row(model);
        row.put(ACTION, action);
        DATAFRAME_ML.add(row);
        LOG.trace("{}", row);
        appendLine(row);
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


	public static void log(CatanModel model, ResourceType catanCard) {
	    log(model, actionResource(catanCard));
	}

	public static Map<String, Object> row(CatanModel model) {
		PlayerColor playerColor = model.currentPlayer.get();
		Map<String, Object> currentState = new LinkedHashMap<>();
		List<CatanCard> cards = model.cards.get(playerColor);
		Map<String, Long> resourceCount = cards.stream()
				.map(e -> Objects.toString(e.getResource(), Objects.toString(e.getDevelopment())))
				.collect(Collectors.groupingBy(e -> e, Collectors.counting()));
		currentState.put(PLAYER, playerColor.toString());
        currentState.put(POINTS, model.getUserChart().countPoints(playerColor, model));
		for (ResourceType r : ResourceType.getResources()) {
			currentState.put(r.toString(), resourceCount.getOrDefault(r.toString(), 0L));
		}
		for (DevelopmentType r : DevelopmentType.values()) {
			currentState.put(r.toString(), resourceCount.getOrDefault(r.toString(), 0L));
		}
		PlayerColor currentWinner = PlayerColor.vals().parallelStream().max(Comparator.comparing(
				(PlayerColor e) -> model.getUserChart().countPoints(e, model.settlePoints, model.usedCards, model.edges))
				.thenComparing((PlayerColor e) -> model.cards.get(e).size())).orElse(playerColor);
		currentState.put(WINNER, currentWinner);
		boolean anyMatch = model.deals.stream().anyMatch(m -> !model.isDealUnfeasible(m));
		currentState.put(HAS_DEAL, Objects.toString(anyMatch));
		currentState.put(SELECT_RESOURCE, Objects.toString(model.resourcesToSelect));
		
		for (Class<? extends CatanResource> class1 : HAS_CLASSES) {
			currentState.put("HAS_" + class1.getSimpleName().toUpperCase(),
					Objects.toString(model.elements.stream().anyMatch(class1::isInstance)));
		}
		
		return currentState;
	}

	public static void winner(PlayerColor playerWinner) {
		DATAFRAME_ML.filter(PLAYER, c->c.equals(playerWinner.toString()));
		DATAFRAME_ML.forEachRow(CatanLogger::appendLine);
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

	private static CatanAction actionResource(ResourceType resource) {
        switch (resource) {
            case BRICK:
                return CatanAction.RESOURCE_BRICK;
            case ROCK:
                return CatanAction.RESOURCE_ROCK;
            case SHEEP:
                return CatanAction.RESOURCE_SHEEP;
            case WHEAT:
                return CatanAction.RESOURCE_WHEAT;
            case WOOD:
                return CatanAction.RESOURCE_WOOD;
            default:
                break;
        }
        return null;
    }

	private static void appendLine(Map<String, Object> rowMap) {
		File file = new File(ResourceFXUtils.getOutFile(), CATAN_LOG);
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

        try (FileWriterWithEncoding fw = new FileWriterWithEncoding(file, StandardCharsets.UTF_8, true);) {
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
