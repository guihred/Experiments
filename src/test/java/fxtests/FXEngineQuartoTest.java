package fxtests;

import gaming.ex06.QuartoLauncher;
import java.util.Random;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyCombination.Modifier;
import javafx.scene.input.MouseButton;
import org.junit.Test;
import utils.RunnableEx;

public class FXEngineQuartoTest extends AbstractTestExecution {

	@Test
	public void verify() throws Exception {
		interactNoWait(RunnableEx.makeRunnable(() -> {
			new QuartoLauncher().start(currentStage);
			currentStage.setMaximized(true);
		}));

		KeyCode[] keycodes = new KeyCode[] { KeyCode.UP, KeyCode.DOWN, KeyCode.LEFT, KeyCode.RIGHT, KeyCode.Z };
		Modifier[] modifiers = new Modifier[] { KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN,
				KeyCombination.CONTROL_DOWN };
		for (int i = 0; i < modifiers.length; i++) {
			for (int j = 0; j < keycodes.length; j++) {
				push(new KeyCodeCombination(keycodes[j], modifiers[i]));
			}
		}
		closeCurrentWindow();
	}

	@Test
	public void verify2() throws Exception {
		interactNoWait(RunnableEx.makeRunnable(() -> {
			new QuartoLauncher().start(currentStage);
			currentStage.setMaximized(true);
		}));
		Random random = new Random();
		for (int i = 0; i < 100; i++) {

			clickOn(random.nextDouble() * currentStage.getWidth() / 2 + currentStage.getWidth() / 4,
					+random.nextDouble() * currentStage.getHeight() / 2 + currentStage.getHeight() / 4,
					MouseButton.PRIMARY);
		}
	}

}
