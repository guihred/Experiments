package fxtests;

import static java.nio.file.Files.copy;

import audio.mp3.EditSongController;
import audio.mp3.FilesComparator;
import audio.mp3.MusicOrganizer;
import extract.Music;
import extract.MusicReader;
import fxpro.ch08.BasicAudioPlayerWithControlLauncher;
import fxpro.ch08.SimpleAudioPlayerLauncher;
import fxsamples.PlayingAudio;
import fxsamples.PlayingAudio2;
import java.io.File;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.testfx.util.WaitForAsyncUtils;
import utils.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class FXEngineMusicOrganizerTest extends AbstractTestExecution {

    @Test
    public void splitAudio() {
        show(MusicOrganizer.class);
        clickOn("Carregar _Vídeos");
        type(KeyCode.ENTER);
        sleep(1000);
        WaitForAsyncUtils.waitForFxEvents();
        TableRow<?> lookupFirst = lookupFirst(TableRow.class);
        if (lookupFirst != null) {
            doubleClickOn(lookupFirst);
        }
        lookup("_Convert to Mp3").queryAll().forEach(this::clickOn);
        WaitForAsyncUtils.waitForFxEvents();
        ConsoleUtils.waitAllProcesses();
        clickOn("Carregar _Musicas");
        type(KeyCode.ENTER);
        WaitForAsyncUtils.waitForFxEvents();
        doubleClickOn(lookupFirst(TableRow.class));
        lookup("_Play/Pause").queryAll().forEach(this::clickOn);
        lookup("_Split").queryAll().forEach(this::clickOn);
        clickOn("_Consertar Musicas");
        tryClickButtons();
        Set<TextField> lookup = lookup(TextField.class);
        for (TextField textField : lookup) {
            tryClickOn(textField);
            type(typeText(getRandomString()));
        }
    }

    @Test
    public void verifyBasicAudioPlayerWithControlLauncher() {
        show(BasicAudioPlayerWithControlLauncher.class);
        Set<Node> queryAll = lookup(".button").queryAll();
        queryAll.forEach(ConsumerEx.ignore(t -> {
            clickOn(t);
            type(KeyCode.ESCAPE);
        }));
    }

    @Test
    public void verifyEditSong2() {
        FXTesting.measureTime("new EditSongController(song)", () -> {
            Path firstSong = getRandomSong();
            File file = firstSong.toFile();
            File outFile2 = ResourceFXUtils.getOutFile(file.getName());
            copy(firstSong, outFile2.toPath());

            Music readTags = MusicReader.readTags(outFile2);
            show(new EditSongController(readTags));
            List<Node> queryAll = lookup(".button").queryAll().stream().collect(Collectors.toList());
            for (int i = 0; i < queryAll.size(); i++) {
                Node node = queryAll.get(i);
                clickOn(node);
                moveSliders(10);
                sleep(1000);
            }
        });
        FXTesting.measureTime("new EditSongController", () -> {
            show(EditSongController.class);
            List<Node> queryAll = lookup(".button").queryAll().stream().collect(Collectors.toList());
            for (int i = queryAll.size() - 1; i >= 0; i--) {
                Node node = queryAll.get(i);
                clickOn(node);
                moveSliders(10);
                sleep(1000);
            }
        });
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void verifyFileComparator() {
        FilesComparator application = show(FilesComparator.class);
        File[] listFiles = ResourceFXUtils.getUserFolder("Music").listFiles(File::isDirectory);
        int i = 0;
        Set<TableView> lookup = lookup(TableView.class);
        for (TableView<File> query : lookup) {
            application.addSongsToTable(query, listFiles[i++ % listFiles.length]);
            while (application.getProgress() < 1) {
                // DOES NOTHING
            }
        }
        while (application.getProgress() < 1) {
            // DOES NOTHING
        }
        for (TableView<?> tableView : lookup) {
            clickOn(from(tableView).lookup(Cell.class::isInstance).queryLabeled());
        }
        lookup(Button.class).stream().filter(e -> !e.getText().startsWith("File") && !e.getText().equals("X"))
            .forEach(this::tryClickOn);
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    public void verifyPlayingAudio() {
        List<Class<? extends PlayingAudio>> c = Arrays.asList(PlayingAudio.class, PlayingAudio2.class);
        for (Class<? extends PlayingAudio> c2 : c) {
            PlayingAudio show = show(c2);
            interactNoWait(RunnableEx.make(() -> show.playMedia(getRandomSong().toUri().toURL().toExternalForm())));
            tryClickButtons();
            lookup(ToggleButton.class).forEach(this::clickOn);
            lookup(ToggleButton.class).forEach(this::clickOn);
            Group f = lookupFirst(Group.class);
            randomDrag(f, 100);
            lookup(Slider.class).forEach(e -> randomDrag(e, 100));
            interactNoWait(RunnableEx.make(() -> show.playMedia(getRandomSong().toUri().toURL().toExternalForm())));
            tryClickOn(lookup("#closeButton").queryParent());
            interactNoWait(currentStage::close);
        }
    }

    @Test
    public void verifySimpleAudioPlayerLauncher() throws MalformedURLException {
        SimpleAudioPlayerLauncher application = new SimpleAudioPlayerLauncher();
        application.createMedia(getRandomSong().toUri().toURL().toExternalForm());
        show(application);
    }

    private Path getRandomSong() {
        File outFile = ResourceFXUtils.getUserFolder("Music");
        Path randomSong = randomItem(ResourceFXUtils.getPathByExtension(outFile, "mp3"));
        getLogger().info("{} from {}", randomSong, HasLogging.getCurrentLine(1));
        return randomSong;
    }
}
