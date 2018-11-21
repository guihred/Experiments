package fxtests;

import graphs.entities.ZoomableScrollPane;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import javafx.scene.Node;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;
import paintexp.PaintMain;
import utils.ResourceFXUtils;
import utils.RunnableEx;

public class FXEnginePaintTest extends ApplicationTest {

    private Stage currentStage;

    @Override
    public void start(final Stage stage) throws Exception {
        ResourceFXUtils.initializeFX();
        currentStage = stage;
    }

    @Test
    public void verify() throws Exception {
		interactNoWait(RunnableEx.makeRunnable(() -> new PaintMain().start(currentStage)));
		List<Node> queryAll = lookup(e -> e instanceof ToggleButton).queryAll().stream().collect(Collectors.toList());
		Set<Node> stack = lookup(e -> e instanceof ZoomableScrollPane).queryAllAs(ZoomableScrollPane.class)
				.stream()
				.map(ZoomableScrollPane::getContent)
				.collect(Collectors.toSet());
		Collections.shuffle(queryAll);
        Random random = new Random();
		List<Node> colors = lookup("#colorGrid").queryAllAs(GridPane.class).stream()
				.flatMap(e -> e.getChildren().stream()).collect(Collectors.toList());
        for (Node next : queryAll) {
			if (!colors.isEmpty()) {
				clickOn(colors.get(random.nextInt(colors.size())),
						random.nextBoolean() ? MouseButton.PRIMARY : MouseButton.SECONDARY);
			}
			clickOn(next);
			stack.forEach(this::moveTo);
			int bound = 200;
			drag(MouseButton.PRIMARY);
			moveBy(random.nextInt(bound) - bound / 2, random.nextInt(bound) - bound / 2);
            drop();
			lookup(".text-area").queryAll().forEach(e -> write("lsadjdnkasjd"));
			moveBy(random.nextInt(bound) - bound / 2, random.nextInt(bound) - bound / 2);
			drag(MouseButton.PRIMARY);
			moveBy(random.nextInt(bound) - bound / 2, random.nextInt(bound) - bound / 2);
			drop();
			Set<Node> queryAll2 = lookup("#tools .toggle-button").queryAll();
			queryAll2.forEach(e -> {
				clickOn(e);
				stack.forEach(this::moveTo);
				drag(MouseButton.PRIMARY);
				moveBy(random.nextInt(bound) - bound / 2, random.nextInt(bound) - bound / 2);
				drop();
				lookup(".text-area").queryAll().forEach(f -> write("lsadjdnkasjd"));
			});


        }
    }
}
