package fxtests;

import static java.nio.file.Files.copy;

import audio.mp3.EditSongController;
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
import javafx.scene.control.Slider;
import javafx.scene.control.TableRow;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.ImageView;
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
        MusicOrganizer organizer = show(MusicOrganizer.class);
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
        type(KeyCode.ENTER);
        while (organizer.getProgress() < 1) {
            // DOES NOTHING
            sleep(10);
        }
        sleep(100);
        doubleClickOn(lookupFirst(TableRow.class));
        lookup("_Play/Pause").queryAll().forEach(this::clickOn);
        lookup("_Split").queryAll().forEach(this::clickOn);
        clickOn("_Consertar Musicas");
        clickOn("_Fix");
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
            moveSliders(10);
            List<Node> queryAll = lookup(".button").queryAll().stream().collect(Collectors.toList());
            for (int i = 0; i < queryAll.size(); i++) {
                Node node = queryAll.get(i);
                clickOn(node);
                sleep(1000);
            }
            lookup(ImageView.class).stream().anyMatch(PredicateEx.makeTest(e -> {
                doubleClickOn(e);
                return true;
            }));
        });
        FXTesting.measureTime("new EditSongController", () -> {
            show(EditSongController.class);
            List<Node> queryAll = lookup(".button").queryAll().stream().collect(Collectors.toList());
            runReversed(queryAll, node -> {
                clickOn(node);
                moveSliders(10);
            });
        });
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
