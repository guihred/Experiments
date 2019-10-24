package fxtests;

import audio.mp3.EditSongController;
import audio.mp3.FilesComparator;
import audio.mp3.MusicOrganizer;
import fxpro.ch08.BasicAudioPlayerWithControlLauncher;
import fxsamples.PlayingAudio;
import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javafx.scene.Node;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyCode;
import org.junit.Test;
import org.testfx.util.WaitForAsyncUtils;
import utils.ConsoleUtils;
import utils.ConsumerEx;
import utils.ResourceFXUtils;
import utils.RunnableEx;

public class FXEngineMusicOrganizerTest extends AbstractTestExecution {

	@Test
	public void splitAudio() {
        show(MusicOrganizer.class);
        clickOn("Carregar _Vídeos");
        typeIfLinux();
        sleep(1000);
        doubleClickOn(lookupFirst(TableRow.class));
		WaitForAsyncUtils.waitForFxEvents();
        lookup("_Convert to Mp3").queryAll().forEach(this::clickOn);
		WaitForAsyncUtils.waitForFxEvents();
        ConsoleUtils.waitAllProcesses();

        clickOn("Carregar _Musicas");
		typeIfLinux();
        doubleClickOn(lookupFirst(TableRow.class));
        lookup("_Play/Pause").queryAll().forEach(this::clickOn);
		lookup("_Split").queryAll().forEach(this::clickOn);
        clickOn("_Consertar Musicas");
	}

	@Test
    public void verifyBasicAudioPlayerWithControlLauncher() throws Exception {
         show(BasicAudioPlayerWithControlLauncher.class);
        Set<Node> queryAll = lookup(".button").queryAll();
        queryAll.forEach(ConsumerEx.ignore(t -> {
            clickOn(t);
            type(KeyCode.ESCAPE);
        }));
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
    @SuppressWarnings("unchecked")
    public void verifyFileComparator() throws Exception {
        FilesComparator application = show(FilesComparator.class);
        TableView<File> query = lookupFirst(TableView.class);
        File[] listFiles = ResourceFXUtils.getUserFolder("Music").listFiles(File::isDirectory);
        application.addSongsToTable(query, listFiles[0]);
    }

    @Test
	    public void verifyPlayingAudio() throws Exception {
	        PlayingAudio show = show(PlayingAudio.class);
	        interactNoWait(() -> show.playMedia(ResourceFXUtils.toExternalForm("TeenTitans.mp3")));
	        tryClickButtons();
	    }

    private void typeIfLinux() {
		// type(KeyCode.M);
		// type(KeyCode.U);
		// type(KeyCode.DOWN);
		// type(KeyCode.TAB);
        type(KeyCode.ENTER);
	}
}
