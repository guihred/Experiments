package fxtests;

import gaming.ex21.*;
import java.util.*;
import java.util.stream.Collectors;
import javafx.geometry.Pos;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;
import ml.data.DataframeML;
import ml.data.DecisionTree;
import ml.data.DecisionTree.DecisionNode;
import org.junit.Test;
import utils.RunnableEx;

public class FXEngineCatanTest extends AbstractTestExecution {

	private static final int MAX_TRIES = 500;

	private Random random = new Random();

	private CatanAction lastPredict;

	// @Test
	public void testaToolsVerify() throws Exception {
		show(CatanApp.class);
		List<EdgeCatan> allEdge = lookup(EdgeCatan.class::isInstance).queryAllAs(EdgeCatan.class).stream()
				.collect(Collectors.toList());
		Collections.shuffle(allEdge);
		List<Village> allVillages = new ArrayList<>();
		List<City> cities = new ArrayList<>();
		List<SettlePoint> settlePoints = lookup(SettlePoint.class::isInstance).queryAllAs(SettlePoint.class).stream()
				.collect(Collectors.toList());
		List<Road> allRoads = new ArrayList<>();
		Collections.shuffle(settlePoints);
		List<ButtonBase> allButtons = lookup(".button").queryAllAs(ButtonBase.class).stream()
				.collect(Collectors.toList());
		allButtons.addAll(lookup(".toggle-button").queryAllAs(ToggleButton.class));
		Set<ButtonBase> clickedButtons = new HashSet<>();
		List<Terrain> allTerrains = lookup(Terrain.class::isInstance).queryAllAs(Terrain.class).stream()
				.collect(Collectors.toList());
		Collections.shuffle(allTerrains);
		for (int i = 0; i < MAX_TRIES && allButtons.stream().anyMatch(b -> !clickedButtons.contains(b)); i++) {
			clickVillages(allVillages, settlePoints);
			clickCities(cities);
			clickRoads(allEdge, allRoads);
			clickThiefs(allTerrains);
			clickCards();
			clickButton(allButtons, clickedButtons);
			getLogger().trace("{}/{}-{}/{}", i + 1, MAX_TRIES, clickedButtons.size(), allButtons.size());
		}
	}

	@Test
	public void testDecisions() throws Exception {
		CatanApp newInstance = new CatanApp();
		try {
			interactNoWait(RunnableEx.makeRunnable(() -> newInstance.start(currentStage)));
		} catch (Exception e) {
			getLogger().error(String.format("ERRO IN %s", CatanApp.class), e);
		}
		DataframeML build = DataframeML.builder("out/catan_log.txt").build();
		build.removeCol("WINNER");
		List<Object> list = build.list("ACTION");
		list.add(list.remove(0));
		DecisionNode buildTree = DecisionTree.buildTree(build, "ACTION");
		getLogger().info("{}", buildTree);

		List<EdgeCatan> allEdge = lookup(EdgeCatan.class::isInstance).queryAllAs(EdgeCatan.class).stream()
				.collect(Collectors.toList());
		Collections.shuffle(allEdge);
		List<Village> allVillages = new ArrayList<>();
		List<City> cities = new ArrayList<>();
		List<SettlePoint> settlePoints = lookup(SettlePoint.class::isInstance).queryAllAs(SettlePoint.class).stream()
				.collect(Collectors.toList());
		List<Road> allRoads = new ArrayList<>();
		Collections.shuffle(settlePoints);
		List<ButtonBase> allButtons = lookup(".button").queryAllAs(ButtonBase.class).stream()
				.collect(Collectors.toList());
		allButtons.addAll(lookup(".toggle-button").queryAllAs(ToggleButton.class));
		Set<ButtonBase> clickedButtons = new HashSet<>();
		List<Terrain> allTerrains = lookup(Terrain.class::isInstance).queryAllAs(Terrain.class).stream()
				.collect(Collectors.toList());
		Collections.shuffle(allTerrains);
		CatanModel model = newInstance.getModel();
		for (int i = 0; i < MAX_TRIES * 2; i++) {

			// while (model.getCurrentPlayer() == PlayerColor.GREEN) {
			// sleep(1000);
			// }
			if (model.getUserChart().anyPlayerPoints(9, model)) {
				return;
			}
			makeDecision(buildTree, allEdge, allVillages, cities, settlePoints, allRoads, allTerrains, newInstance,
					allButtons, clickedButtons);

			getLogger().info("{}/{}-{}/{}", i + 1, MAX_TRIES * 2, clickedButtons.size(), allButtons.size());
		}

	}

	private void clickButton(List<ButtonBase> allButtons, Set<ButtonBase> clickedButtons) {
		Collections.shuffle(allButtons);
		allButtons.addAll(lookup(".button").queryAllAs(ButtonBase.class).stream().filter(t -> !allButtons.contains(t))
				.collect(Collectors.toList()));
		allButtons.stream().filter(e -> !e.isDisabled()).filter(e -> e.getParent() != null)
				.filter(ButtonBase::isVisible).filter(e -> e.getParent().isVisible())
				.sorted(Comparator.comparing(clickedButtons::contains)).findFirst().ifPresent(t -> {
					clickedButtons.add(t);
					tryToClick(t);
				});
		allButtons.removeIf(e -> e.getParent() == null);
		clickedButtons.removeIf(e -> e.getParent() == null);
	}

	private void clickButton(String btn, List<ButtonBase> allButtons, Set<ButtonBase> clickedButtons) {
		Collections.shuffle(allButtons);
		allButtons.addAll(lookup(".button").queryAllAs(ButtonBase.class).stream().filter(t -> !allButtons.contains(t))
				.collect(Collectors.toList()));
		allButtons.stream().filter(e -> !e.isDisabled()).filter(e -> e.getParent() != null)
				.filter(ButtonBase::isVisible).filter(e -> e.getParent().isVisible())
				.sorted(Comparator
						.comparing((ButtonBase c) -> matches(btn, c))
						.thenComparing(clickedButtons::contains))
				.findFirst().ifPresent(t -> {
					clickedButtons.add(t);
					tryToClick(t);
				});
		allButtons.removeIf(e -> e.getParent() == null);
		clickedButtons.removeIf(e -> e.getParent() == null);
	}

	private void clickCards() {
		clickCards(null);
	}

	private void clickCards(String btn) {
		List<CatanCard> allCards = lookup(CatanCard.class::isInstance).queryAllAs(CatanCard.class).stream()
				.collect(Collectors.toList());
		if (allCards.stream().filter(CatanCard::isSelected).count() > 4) {
			return;
		}
		List<CatanCard> cardsToSelect = new ArrayList<>(allCards);
		Collections.shuffle(cardsToSelect);
		cardsToSelect.sort(Comparator.comparing(
				(CatanCard c) -> matches(btn, c))
				.thenComparing(c -> orderByResources(cardsToSelect, c)));
		cardsToSelect.removeIf(CatanCard::isSelected);
		if (random.nextBoolean() && !cardsToSelect.isEmpty()) {
			targetPos(Pos.TOP_CENTER);
			clickOn(cardsToSelect.remove(0));
			if (random.nextBoolean() && !cardsToSelect.isEmpty()
					&& allCards.stream().filter(CatanCard::isSelected).count() < 4) {
				clickOn(cardsToSelect.remove(0));
			}
			targetPos(Pos.CENTER);
		}
	}

	private void clickCities(final List<City> cities) {
		List<City> notClickedVillages = lookup(City.class::isInstance).queryAllAs(City.class).stream()
				.filter(t -> !cities.contains(t)).collect(Collectors.toList());

		if (notClickedVillages.isEmpty()) {
			return;
		}
		List<SettlePoint> settlePoints = lookup(SettlePoint.class::isInstance).queryAllAs(SettlePoint.class)
				.parallelStream().collect(Collectors.toList());
		City next = notClickedVillages.remove(0);
		drag(next, MouseButton.PRIMARY);
		SettlePoint remove = settlePoints.parallelStream().filter(e -> e.isSuitableForCity(next)).findAny()
				.orElse(settlePoints.get(0));
		moveTo(remove);
		drop();
		cities.add(next);
	}

	private void clickRoads(final List<EdgeCatan> allEdge, final List<Road> allRoads) {
		lookup(Road.class::isInstance).queryAllAs(Road.class).stream().filter(r -> !allRoads.contains(r)).forEach(e -> {
			moveTo(e);
			drag(e, MouseButton.PRIMARY);
			moveTo(getEdge(e, allEdge));
			drop();
			if (!(e.getParent() instanceof StackPane)) {
				allRoads.add(e);
			}
		});
	}

	private void clickThiefs(final List<Terrain> queryAllAs) {
		Collections.shuffle(queryAllAs);
		lookup(Thief.class::isInstance).queryAllAs(Thief.class).stream().filter(e -> e.getParent() instanceof StackPane)
				.forEach(e -> {
					moveTo(e);
					drag(e, MouseButton.PRIMARY);

					queryAllAs.parallelStream().findAny().ifPresent(this::moveTo);
					drop();
				});
	}

	private void clickVillages(final List<Village> allVillages, final List<SettlePoint> settlePoints) {
		List<Village> notClickedVillages = lookup(Village.class::isInstance).queryAllAs(Village.class).stream()
				.filter(v -> !allVillages.contains(v)).collect(Collectors.toList());
		Collections.shuffle(notClickedVillages);
		if (notClickedVillages.isEmpty()) {
			return;
		}

		Village next = notClickedVillages.remove(0);
		drag(next, MouseButton.PRIMARY);
		Collections.shuffle(settlePoints);
		SettlePoint remove = settlePoints.parallelStream().filter(e -> e.pointAcceptVillage(next)).findAny()
				.orElse(settlePoints.get(0));
		moveTo(remove);
		drop();
		if (!(next.getParent() instanceof StackPane)) {
			settlePoints.remove(remove);
			settlePoints.removeAll(remove.getNeighbors());
			allVillages.add(next);
		}
	}

	private EdgeCatan getEdge(final Road road, final List<EdgeCatan> allEdge) {
		return allEdge.parallelStream().filter(e -> e.edgeAcceptRoad(road)).findFirst().orElse(allEdge.get(0));
	}

	private void makeDecision(DecisionNode buildTree, List<EdgeCatan> allEdge, List<Village> allVillages,
			List<City> cities, List<SettlePoint> settlePoints, List<Road> allRoads, List<Terrain> allTerrains, CatanApp newInstance, List<ButtonBase> allButtons, Set<ButtonBase> clickedButtons
			) {
		CatanModel model = newInstance.getModel();
		Map<String, Object> row = CatanLogger.row(model);
		CatanAction predict = CatanAction.valueOf(Objects.toString(buildTree.predict(row)));
		if (lastPredict == predict) {
			CatanAction[] values = CatanAction.values();
			predict = values[random.nextInt(values.length)];
			if (model.getElements().stream().anyMatch(City.class::isInstance)) {
				predict = CatanAction.PLACE_CITY;
			}
		}
		lastPredict = predict;
		switch (predict) {
			case PLACE_CITY:
				clickCities(cities);
				break;
			case PLACE_ROAD:
				clickRoads(allEdge, allRoads);
				break;
			case PLACE_THIEF:
				clickThiefs(allTerrains);
				break;
			case PLACE_VILLAGE:
				clickVillages(allVillages, settlePoints);
				break;
			case ACCEPT_DEAL:
			case BUY_CITY:
			case BUY_DELEVOPMENT:
			case BUY_ROAD:
			case BUY_VILLAGE:
			case SKIP_TURN:
			case THROW_DICE:
			case EXCHANGE:
			case MAKE_DEAL:
				clickButton(predict.toString().replaceAll("BUY_", "").replaceAll("_", " ").toLowerCase(), allButtons,
						clickedButtons);
				break;
			case SELECT_BRICK:
			case SELECT_KNIGHT:
			case SELECT_MONOPOLY:
			case SELECT_ROAD_BUILDING:
			case SELECT_ROCK:
			case SELECT_SHEEP:
			case SELECT_UNIVERSITY:
			case SELECT_WHEAT:
			case SELECT_WOOD:
			case SELECT_YEAR_OF_PLENTY:
				clickCards(predict.toString().replaceAll("SELECT_", "").replaceAll("_", " ").toLowerCase());
				break;
			default:
				break;

		}
	}

	private boolean matches(String btn, ButtonBase c) {
		return !Objects.toString(c.getStyleClass()).contains(btn)
				&& !Objects.toString(c.getId()).contains(btn)
				&& !Objects.toString(c.getText(), "").toLowerCase().contains(btn);
	}

	private boolean matches(String btn, CatanCard c) {
		return !Objects.toString(c.getStyleClass(), "").contains(btn + "-card")
				&& !Objects.toString(c.getId()).contains(btn + "-card");
	}

	private Long orderByResources(List<CatanCard> cardsToSelect, final CatanCard c) {
		return -cardsToSelect.stream().filter(e -> e.getResource() == c.getResource()).filter(CatanCard::isSelected)
				.count();
	}

	private void tryToClick(final ButtonBase t) {
		try {
			clickOn(t);
		} catch (Exception e1) {
			getLogger().trace("{} not clicked", t, e1);
			getLogger().error("{} not clicked", t);
		}
	}
}
