package fxtests;

import audio.mp3.MusicOrganizer;
import javafx.scene.Node;
import javafx.scene.control.TableRow;
import javafx.scene.input.KeyCode;
import org.junit.Test;
import utils.ConsoleUtils;

public class FXEngineMusicOrganizerTest extends AbstractTestExecution {



	@Test
	public void splitAudio() {
        show(MusicOrganizer.class);
        clickOn("Carregar Vídeos");
        typeIfLinux();
        sleep(1000);
        lookup(e -> e instanceof TableRow).tryQuery().ifPresent(this::doubleClickOn);
        lookup("_Convert to Mp3").queryAll().forEach(this::clickOn);
        ConsoleUtils.waitAllProcesses();

		clickOn("Carregar Musicas");
		typeIfLinux();
		lookup(e -> e instanceof TableRow).tryQuery().ifPresent(this::doubleClickOn);
		lookup("_Split").queryAll().forEach(this::clickOn);

        lookup(e -> e instanceof TableRow).tryQuery().ifPresent(this::doubleClickOn);
        lookup("_Find Image").queryAll().forEach(this::clickOn);

        for (int i = 0; lookup(".wiki").queryAll().isEmpty() && i < 20; i++) {
            sleep(500);
        }
        Node queryAs = lookup(".wiki").queryAs(Node.class);
        if (queryAs != null) {
            doubleClickOn(queryAs);
        }
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
