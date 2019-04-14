package fxtests;

import audio.mp3.MusicOrganizer;
import javafx.scene.control.TableRow;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.junit.Test;
import utils.ConsoleUtils;

public class FXEngineMusicOrganizerTest extends AbstractTestExecution {


	@Test
	public void convertToMp3() throws Exception {

		clickOn("Carregar Vídeos");
		typeIfLinux();
		sleep(1000);
		lookup(e -> e instanceof TableRow).tryQuery().ifPresent(this::doubleClickOn);
		lookup("_Convert to Mp3").queryAll().forEach(this::clickOn);
		ConsoleUtils.waitAllProcesses();
	}

	@Test
	public void splitAudio() {
		clickOn("Carregar Musicas");
		typeIfLinux();
		lookup(e -> e instanceof TableRow).tryQuery().ifPresent(this::doubleClickOn);
		lookup("_Split").queryAll().forEach(this::clickOn);
	}

	@Override
	public void start(Stage stage) throws Exception {
		super.start(stage);
		stage.setMaximized(true);
		show(MusicOrganizer.class);
	}

	private void typeIfLinux() {
		if (isLinux) {
			type(KeyCode.DOWN);
			type(KeyCode.ENTER);
			type(KeyCode.M);
			type(KeyCode.DOWN);
			type(KeyCode.ENTER);
			type(KeyCode.ENTER);
		} else {
			type(KeyCode.M);
			type(KeyCode.U);
			type(KeyCode.DOWN);
			type(KeyCode.TAB);
			type(KeyCode.ENTER);
		}
	}
}
