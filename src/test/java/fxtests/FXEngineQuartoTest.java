package fxtests;

import gaming.ex06.QuartoLauncher;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyCombination.Modifier;
import javafx.scene.input.MouseButton;
import org.junit.Test;
import utils.RunnableEx;

public class FXEngineQuartoTest extends AbstractTestExecution {

	public double randAngle(int  r) {
		return random.nextInt(r) * Math.PI * 2 / r;
	}

	@Test
	public void verify() throws Exception {
		interactNoWait(RunnableEx.make(() -> {
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
		interactNoWait(RunnableEx.make(() -> {
			new QuartoLauncher().start(currentStage);
			currentStage.setMaximized(true);
		}));

		press(KeyCode.SHIFT, KeyCode.CONTROL, KeyCode.ALT);
		for (int i = 0; i < 10; i++) {
			type(KeyCode.UP);
		}
		for (int i = 0; i < 20; i++) {
			double height = currentStage.getHeight();
			double width = currentStage.getWidth();
			double ratio = .21;
			clickOn(width * ratio + randDistance(width), height / 2, MouseButton.PRIMARY);
			randButton(width, height);
			clickOn(width / 2, height * ratio + randDistance(height), MouseButton.PRIMARY);
			randButton(width, height);
			clickOn(width / 2, height * (1 - ratio) + randDistance(width), MouseButton.PRIMARY);
			randButton(width, height);
			clickOn(width * (1 - ratio) + randDistance(height), height / 2, MouseButton.PRIMARY);
			randButton(width, height);
			if (tryClickButtons()) {
				break;
			}
		}
	}

	private void randButton(double width, double height) {

		boolean bigRadius = random.nextBoolean();
		double angle = randAngle(bigRadius ? 12 : 4);

		double x = Math.cos(angle) * width / 7 / (bigRadius ? 1 : 2) + width / 2;
		double y = Math.sin(angle) * height / 7 / (bigRadius ? 1 : 2) + height / 2;
		clickOn(x, y, MouseButton.PRIMARY);
	}

    private static double randDistance(double height) {
		return (Math.random() - .5) * height / 40;
	}

}
