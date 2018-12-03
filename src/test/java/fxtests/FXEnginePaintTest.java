package fxtests;

import graphs.entities.ZoomableScrollPane;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import javafx.collections.ObservableList;
import javafx.geometry.VerticalDirection;
import javafx.scene.Node;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;
import paintexp.PaintMain;
import utils.ConsumerEx;
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
	public void testaToolsVerify() throws Exception {
		interactNoWait(RunnableEx.makeRunnable(() -> new PaintMain().start(currentStage)));
		Node stack = lookup(e -> e instanceof ZoomableScrollPane).queryAs(ZoomableScrollPane.class).getContent();
		testTools(stack);
		testMenus(stack);
	}

	protected void testTools(final Node stack) {
		List<Node> queryAll = lookup(e -> e instanceof ToggleButton).queryAll().stream().collect(Collectors.toList());
		Collections.shuffle(queryAll);
		Random random = new Random();
		List<Node> colors = lookup("#colorGrid").queryAllAs(GridPane.class).stream()
				.flatMap(e -> e.getChildren().stream()).collect(Collectors.toList());
		int bound = 200;
		for (Node next : queryAll) {
			if (!colors.isEmpty()) {
				clickOn(colors.get(random.nextInt(colors.size())),
						random.nextInt(5) != 0 ? MouseButton.PRIMARY : MouseButton.SECONDARY);
			}
			clickOn(next);
			moveTo(stack);
			drag(MouseButton.PRIMARY);
			moveBy(random.nextInt(bound) - bound / 2, random.nextInt(bound) - bound / 2);
			drop();
			lookup(".text-area").queryAll().forEach(e -> write("lsadjdnkasjd"));
			moveBy(random.nextInt(bound) - bound / 2, random.nextInt(bound) - bound / 2);
			drag(MouseButton.PRIMARY);
			moveBy(random.nextInt(bound) - bound / 2, random.nextInt(bound) - bound / 2);
			drop();
			Set<Node> queryAll2 = lookup("#tools .toggle-button").queryAll();
			queryAll2.forEach(ConsumerEx.makeConsumer(e -> {
				clickOn(e, MouseButton.PRIMARY);
				scroll(1, VerticalDirection.DOWN);
				moveTo(stack);
				moveBy(random.nextInt(bound) - bound / 2, random.nextInt(bound) - bound / 2);
				drag(MouseButton.PRIMARY);
				moveBy(random.nextInt(bound) - bound / 2, random.nextInt(bound) - bound / 2);
				moveBy(random.nextInt(bound) - bound / 2, random.nextInt(bound) - bound / 2);
				drop();
				drag(MouseButton.PRIMARY);
				moveBy(random.nextInt(bound) - bound / 2, random.nextInt(bound) - bound / 2);
				drop();
				lookup(".text-area").queryAll().forEach(f -> {
					interactNoWait(() -> f.requestFocus());
					write("lsad");
				});
				type(KeyCode.ESCAPE);
			}));
		}
	}

	private void testMenus(final Node stack) {
		List<MenuButton> node = lookup(MenuButton.class::isInstance).queryAllAs(MenuButton.class).stream()
				.collect(Collectors.toList());
		for (int i = 1; i < node.size(); i++) {
			MenuButton e1 = node.get(i);
			ObservableList<MenuItem> items = e1.getItems();
			items.forEach(f -> {
				lookup("#SelectRectTool").queryAll().forEach(this::clickOn);
				moveTo(stack);
				double bound2 = stack.getBoundsInParent().getWidth();
				moveBy(-bound2 / 4, -bound2 / 4);
				drag(MouseButton.PRIMARY);
				moveBy(bound2 / 2, bound2 / 2);
				drop();
				interactNoWait(() -> f.fire());
				lookup(".text-field").queryAll().forEach(e -> {
					clickOn(e);
					eraseText(3);
					write("120");
				});
				lookup("Resize").queryAll().forEach(this::clickOn);
				type(KeyCode.ESCAPE);
			});
		}
	}
}
