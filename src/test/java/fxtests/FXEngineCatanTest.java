package fxtests;

import gaming.ex21.*;
import java.util.*;
import java.util.stream.Collectors;
import javafx.geometry.Pos;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;
import org.junit.Test;

public class FXEngineCatanTest extends AbstractTestExecution {

	private static final int MAX_TRIES = 300;

	private Random random = new Random();

	@Test
	public void testaToolsVerify() throws Exception {
		show(CatanApp.class);
		testTools();
	}

	public void testTools() {
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
			getLogger().info("{}/{}-{}/{}", i + 1, MAX_TRIES, clickedButtons.size(), allButtons.size());
		}
	}

	private void clickButton(final List<ButtonBase> allButtons, final Set<ButtonBase> clickedButtons) {
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

	private void clickCards() {
		List<CatanCard> allCards = lookup(CatanCard.class::isInstance).queryAllAs(CatanCard.class).stream()
				.collect(Collectors.toList());
        if (allCards.stream().filter(CatanCard::isSelected).count() > 4) {
			return;
		}
        List<CatanCard> cardsToSelect = new ArrayList<>(allCards);
		Collections.shuffle(cardsToSelect);
		Comparator<CatanCard> comparator = Comparator
				.comparing((final CatanCard c) -> cardsToSelect.stream()
						.filter(e -> e.getResource() == c.getResource()).filter(e -> e.isSelected()).count())
				.reversed();
		cardsToSelect.sort(comparator);
        cardsToSelect.removeIf(CatanCard::isSelected);
		if (random.nextBoolean() && !cardsToSelect.isEmpty()) {
			targetPos(Pos.TOP_LEFT);
			clickOn(cardsToSelect.remove(0));
			if (random.nextBoolean() && !cardsToSelect.isEmpty()
					&& allCards.stream().filter(e -> e.isSelected()).count() < 4) {
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

	private void tryToClick(final ButtonBase t) {
		try {
			clickOn(t);
		} catch (Exception e1) {
			getLogger().trace("{} not clicked", t, e1);
			getLogger().error("{} not clicked", t);
		}
	}
}
