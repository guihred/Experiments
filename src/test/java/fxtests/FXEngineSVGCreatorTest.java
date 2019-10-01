package fxtests;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import javafx.scene.Node;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;
import org.junit.Test;
import paintexp.svgcreator.SVGCreator;

public class FXEngineSVGCreatorTest extends AbstractTestExecution {

    private Random random = new Random();

    @Test
	public void testaToolsVerify() throws Exception {
        show(SVGCreator.class);
        Node stack = lookup(StackPane.class::isInstance).queryAs(StackPane.class);
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
		tryClickButtons();
		type(typeText("fb.jpg"));
		type(KeyCode.ENTER);
		lookup(".slider").queryAll().forEach(m -> {
			drag(m, MouseButton.PRIMARY);
			moveBy(randomMove(10), 0);
			drop();
		});
	}

    private int randomMove(final int bound) {
        return random.nextInt(bound) - bound / 2;
    }
}
