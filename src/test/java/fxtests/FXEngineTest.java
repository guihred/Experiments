package fxtests;

import fxpro.ch02.PongLauncher;
import gaming.ex01.SnakeLauncher;
import gaming.ex11.DotsLauncher;
import gaming.ex11.DotsSquare;
import gaming.ex18.Square2048Launcher;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.shape.Rectangle;
import org.junit.Test;
import utils.ConsumerEx;

public class FXEngineTest extends AbstractTestExecution {

	@Test
	public void verify() throws Exception {
		show(SnakeLauncher.class);
		type(KeyCode.UP, KeyCode.LEFT, KeyCode.DOWN, KeyCode.RIGHT);
		show(Square2048Launcher.class);
		type(KeyCode.UP, KeyCode.LEFT, KeyCode.DOWN, KeyCode.RIGHT);
		show(DotsLauncher.class);
		Set<Node> queryAll = lookup(e -> e instanceof DotsSquare).queryAll().stream().limit(20).collect(Collectors.toSet());
		Random random = new Random();
		for (Node next : queryAll) {
			drag(next, MouseButton.PRIMARY);
			if (random.nextBoolean()) {
				moveBy(DotsSquare.SQUARE_SIZE, 0);
			} else {
				moveBy(0, DotsSquare.SQUARE_SIZE);
			}
			drop();
		}
		show(PongLauncher.class);
		lookup(".button").queryAll().stream().forEach(ConsumerEx.makeConsumer(t -> clickOn(t, MouseButton.PRIMARY)));
		for (Node next : lookup(e -> e instanceof Rectangle && e.isVisible()).queryAll()) {
			drag(next, MouseButton.PRIMARY);
			moveBy(0, DotsSquare.SQUARE_SIZE);
			moveBy(0, -DotsSquare.SQUARE_SIZE);
			drop();
		}
	}

}
