package fxtests;

import audio.mp3.EditSongController;
import audio.mp3.FilesComparator;
import audio.mp3.MusicOrganizer;
import java.io.File;
import java.util.List;
import java.util.stream.Collectors;
import javafx.scene.Node;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyCode;
import org.junit.Test;
import org.testfx.util.WaitForAsyncUtils;
import utils.ConsoleUtils;
import utils.ResourceFXUtils;
import utils.RunnableEx;

public class FXEngineMusicOrganizerTest extends AbstractTestExecution {

	@Test
	public void splitAudio() {
        show(MusicOrganizer.class);
        clickOn("Carregar _Vídeos");
        typeIfLinux();
        sleep(1000);
        lookup(e -> e instanceof TableRow).tryQuery().ifPresent(this::doubleClickOn);
		WaitForAsyncUtils.waitForFxEvents();
        lookup("_Convert to Mp3").queryAll().forEach(this::clickOn);
		WaitForAsyncUtils.waitForFxEvents();
        ConsoleUtils.waitAllProcesses();

        clickOn("Carregar _Musicas");
		typeIfLinux();
		lookup(e -> e instanceof TableRow).tryQuery().ifPresent(this::doubleClickOn);
        lookup("_Play/Pause").queryAll().forEach(this::clickOn);
		lookup("_Split").queryAll().forEach(this::clickOn);
	}

	@Test
    public void verifyEditSong() throws Exception {
        EditSongController show = show(EditSongController.class);
        show.setClose(false);
        List<Node> queryAll = lookup(".button").queryAll().stream().collect(Collectors.toList());
        for (int i = 0; i < queryAll.size(); i++) {
            Node node = queryAll.get(i);
            RunnableEx.ignore(() -> clickOn(node));
            RunnableEx.ignore(() -> sleep(1000));
        }
    }

    @Test
    public void verifyFileComparator() throws Exception {
        FilesComparator application = show(FilesComparator.class);
        TableView<File> query = lookup(e -> e instanceof TableView).query();
        File[] listFiles = ResourceFXUtils.getUserFolder("Music").listFiles(File::isDirectory);
        application.addSongsToTable(query, listFiles[0]);
    }

    private void typeIfLinux() {
		// type(KeyCode.M);
		// type(KeyCode.U);
		// type(KeyCode.DOWN);
		// type(KeyCode.TAB);
        type(KeyCode.ENTER);
	}
}
