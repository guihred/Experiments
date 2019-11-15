package fxtests;

import java.util.Collections;
import java.util.List;
import javafx.scene.Node;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;
import org.junit.Test;
import paintexp.svgcreator.SVGCreator;

public class FXEngineSVGCreatorTest extends AbstractTestExecution {
    @Test
	public void testSVGCreator() {
        show(SVGCreator.class);
        Node stack = lookupFirst(StackPane.class);
        List<ToggleButton> queryAll = lookupList(ToggleButton.class);
		Collections.shuffle(queryAll);
        int bound = (int) (stack.getBoundsInParent().getWidth() / 4);
		for (Node next : queryAll) {
            clickOn(next);
            moveTo(stack);
            drag(MouseButton.PRIMARY);
            moveRandom(bound);
            drop();

            moveRandom(bound);
            drag(MouseButton.PRIMARY);
            moveRandom(bound);
            drop();

            moveTo(stack);
            moveRandom(bound);
            drag(MouseButton.PRIMARY);
            moveRandom(bound);
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
