package fxtests;

import gaming.ex21.CatanApp;
import gaming.ex21.Road;
import gaming.ex21.SettlePoint;
import gaming.ex21.Thief;
import gaming.ex21.Village;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;
import utils.HasLogging;
import utils.ResourceFXUtils;
import utils.RunnableEx;

public class FXEngineCatanTest extends ApplicationTest implements HasLogging {

	private Stage currentStage;

	private Random random = new Random();

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

	private void testTools() {
		List<Village> queryAllAs = lookup(Village.class::isInstance).queryAllAs(Village.class).stream()
				.collect(Collectors.toList());
		List<SettlePoint> settlePoints = lookup(SettlePoint.class::isInstance).queryAllAs(SettlePoint.class).stream()
				.collect(Collectors.toList());
		List<Road> roads = new ArrayList<>();
		Collections.shuffle(settlePoints);
		for (int i = 0; i < queryAllAs.size() && !settlePoints.isEmpty(); i++) {
			Node next = queryAllAs.get(i);
			drag(next, MouseButton.PRIMARY);
			SettlePoint remove = settlePoints.remove(0);
			moveTo(remove);
			drop();
			settlePoints.removeAll(remove.getNeighbors());
			lookup(Road.class::isInstance).queryAllAs(Road.class).stream().filter(r -> !roads.contains(r))
					.forEach(e -> {
						moveTo(e);
						drag(e, MouseButton.PRIMARY);
						moveTo(remove.getEdges().get(0));
						drop();
						roads.add(e);
					});
			lookup(Thief.class::isInstance).queryAllAs(Thief.class).stream().forEach(e -> {
				moveTo(e);
				drag(e, MouseButton.PRIMARY);
				moveTo("Terrain");
				drop();
			});

			lookup(".button").queryAllAs(Button.class).stream().filter(e -> !e.isDisabled()).filter(e -> e.isVisible())
					.forEach(this::clickOn);
			queryAllAs.addAll(lookup(Village.class::isInstance).queryAllAs(Village.class).stream()
					.filter(v -> !queryAllAs.contains(v)).collect(Collectors.toList()));

		}
	}
}
