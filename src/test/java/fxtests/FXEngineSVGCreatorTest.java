package fxtests;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import javafx.scene.Node;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;
import paintexp.svgcreator.SVGCreator;
import utils.HasLogging;
import utils.ResourceFXUtils;
import utils.RunnableEx;

public class FXEngineSVGCreatorTest extends ApplicationTest implements HasLogging {

    private Stage currentStage;

    private Random random = new Random();

	@Override
	public void start(final Stage stage) throws Exception {
		ResourceFXUtils.initializeFX();
		currentStage = stage;
	}

    @Test
	public void testaToolsVerify() throws Exception {
        interactNoWait(RunnableEx.makeRunnable(() -> new SVGCreator().start(currentStage)));
        Node stack = lookup(StackPane.class::isInstance).queryAs(StackPane.class);
        testTools(stack);
	}
	protected void testTools(final Node stack) {


        List<Node> queryAll = lookup(ToggleButton.class::isInstance).queryAll().stream().collect(Collectors.toList());
		Collections.shuffle(queryAll);
        int bound = (int) (stack.getBoundsInParent().getWidth() / 4);
		for (Node next : queryAll) {
            clickOn(next);
            moveTo(stack);
			drag(MouseButton.PRIMARY);
            moveBy(randomMove(bound), randomMove(bound));
			drop();

            moveBy(randomMove(bound), randomMove(bound));
            drag(MouseButton.PRIMARY);
            moveBy(randomMove(bound), randomMove(bound));
            drop();
            moveTo(stack);
            moveBy(randomMove(bound), randomMove(bound));
            drag(MouseButton.PRIMARY);
            moveBy(randomMove(bound), randomMove(bound));
            drop();
		}
	}

    private int randomMove(int bound) {
        return random.nextInt(bound) - bound / 2;
    }
}
