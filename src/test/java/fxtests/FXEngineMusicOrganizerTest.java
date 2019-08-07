package fxtests;

import audio.mp3.MusicOrganizer;
import javafx.scene.control.TableRow;
import javafx.scene.input.KeyCode;
import org.junit.Test;
import utils.ConsoleUtils;

public class FXEngineMusicOrganizerTest extends AbstractTestExecution {

	@Test
	public void splitAudio() {
        show(MusicOrganizer.class);
        clickOn("Carregar _Vídeos");
        typeIfLinux();
        sleep(1000);
        lookup(e -> e instanceof TableRow).tryQuery().ifPresent(this::doubleClickOn);
        lookup("_Convert to Mp3").queryAll().forEach(this::clickOn);
        ConsoleUtils.waitAllProcesses();

        clickOn("Carregar _Musicas");
		typeIfLinux();
		lookup(e -> e instanceof TableRow).tryQuery().ifPresent(this::doubleClickOn);
        lookup("_Play/Pause").queryAll().forEach(this::clickOn);
		lookup("_Split").queryAll().forEach(this::clickOn);
	}

	private void typeIfLinux() {
        type(KeyCode.M);
        type(KeyCode.U);
        type(KeyCode.DOWN);
        type(KeyCode.TAB);
        type(KeyCode.ENTER);
	}
}
