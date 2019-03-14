package fxtests;

import gaming.ex21.CatanApp;
import gaming.ex21.CatanCard;
import gaming.ex21.EdgeCatan;
import gaming.ex21.Road;
import gaming.ex21.SettlePoint;
import gaming.ex21.Terrain;
import gaming.ex21.Thief;
import gaming.ex21.Village;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;
import utils.HasLogging;
import utils.ResourceFXUtils;
import utils.RunnableEx;

public class FXEngineCatanTest extends ApplicationTest implements HasLogging {

	private static final int MAX_TRIES = 100;
	private Stage currentStage;

	@Override
	public void start(final Stage stage) throws Exception {
		ResourceFXUtils.initializeFX();
		currentStage = stage;
	}

	@Test
	public void testaToolsVerify() throws Exception {
		interactNoWait(RunnableEx.makeRunnable(() -> new CatanApp().start(currentStage)));
		testTools();
	}

	private void clickCards() {
		List<CatanCard> collect = lookup(CatanCard.class::isInstance).queryAllAs(CatanCard.class).stream()
				.collect(Collectors.toList());
		Collections.shuffle(collect);
		if (!collect.isEmpty()) {
			clickOn(collect.get(0));
		}
	}

	private void clickRoads(final List<EdgeCatan> allEdge, final List<Road> allRoads) {
		lookup(Road.class::isInstance).queryAllAs(Road.class).stream().filter(r -> !allRoads.contains(r)).forEach(e -> {
			moveTo(e);
			drag(e, MouseButton.PRIMARY);
			moveTo(getEdge(e, allEdge));
			drop();
			allRoads.add(e);
		});
	}

	private void clickThiefs() {
		lookup(Thief.class::isInstance).queryAllAs(Thief.class).stream().filter(e -> e.getParent() instanceof StackPane)
				.forEach(e -> {
					moveTo(e);
					drag(e, MouseButton.PRIMARY);
					lookup(Terrain.class::isInstance).queryAllAs(Terrain.class).parallelStream().findAny()
							.ifPresent(this::moveTo);
					drop();
				});
	}

	private void clickVillages(final List<Village> allVillages, final List<SettlePoint> settlePoints) {
		List<Village> notClickedVillages = lookup(Village.class::isInstance).queryAllAs(Village.class).stream()
				.filter(v -> !allVillages.contains(v)).collect(Collectors.toList());

		if (notClickedVillages.isEmpty()) {
			return;
		}

		Village next = notClickedVillages.remove(0);
		drag(next, MouseButton.PRIMARY);
		SettlePoint remove = settlePoints.remove(0);
		moveTo(remove);
		drop();
		settlePoints.removeAll(remove.getNeighbors());
		allVillages.add(next);
	}

	private EdgeCatan getEdge(final Road road, final List<EdgeCatan> allEdge) {
		return allEdge.parallelStream().filter(e -> e.edgeAcceptRoad(road)).findFirst().orElse(allEdge.get(0));
	}

	private void testTools() {
		List<EdgeCatan> allEdge = lookup(EdgeCatan.class::isInstance).queryAllAs(EdgeCatan.class).stream()
				.collect(Collectors.toList());
		List<Village> allVillages = new ArrayList<>();
		List<SettlePoint> settlePoints = lookup(SettlePoint.class::isInstance).queryAllAs(SettlePoint.class).stream()
				.collect(Collectors.toList());
		List<Road> allRoads = new ArrayList<>();
		Collections.shuffle(settlePoints);
		List<ButtonBase> allButtons = lookup(".button").queryAllAs(ButtonBase.class).stream()
				.collect(Collectors.toList());
		allButtons.addAll(lookup(".toggle-button").queryAllAs(ToggleButton.class));
		Set<ButtonBase> clickedButtons = new HashSet<>();

		for (int i = 0; i < MAX_TRIES && allButtons.stream().anyMatch(b -> !clickedButtons.contains(b)); i++) {
			clickVillages(allVillages, settlePoints);

			clickRoads(allEdge, allRoads);
			clickThiefs();
			clickCards();
			Collections.shuffle(allButtons);
			allButtons.addAll(lookup(".button").queryAllAs(ButtonBase.class).stream()
					.filter(t -> !allButtons.contains(t)).collect(Collectors.toList()));
			allButtons.stream().filter(e -> !e.isDisabled()).filter(e -> e.getParent() != null)
					.filter(ButtonBase::isVisible).filter(e -> e.getParent().isVisible())
					.sorted(Comparator.comparing(clickedButtons::contains)).findFirst().ifPresent(t -> {
						clickedButtons.add(t);
						tryToClick(t);
					});
			allButtons.removeIf(e -> e.getParent() == null);
			clickedButtons.removeIf(e -> e.getParent() == null);
			getLogger().info("{}/{}-{}/{}", i + 1, MAX_TRIES, clickedButtons.size(), allButtons.size());
		}
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
