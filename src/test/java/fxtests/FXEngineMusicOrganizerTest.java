package fxtests;

import audio.mp3.MusicOrganizer;
import javafx.scene.control.TableRow;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;
import utils.ConsoleUtils;
import utils.HasLogging;
import utils.ResourceFXUtils;


public class FXEngineMusicOrganizerTest extends ApplicationTest implements HasLogging {

    private MusicOrganizer musicOrganizer;

    @Override
    public void start(Stage stage) throws Exception {
        ResourceFXUtils.initializeFX();
        getLogger().info("STARTING FXEngineMusicOrganizerTest");
        stage.setMaximized(true);
        musicOrganizer = new MusicOrganizer();
        musicOrganizer.start(stage);
        getLogger().info("FXEngineMusicOrganizerTest STARTED");
    }

    @Test
    public void convertToMp3() throws Exception {

        clickOn("Carregar Vídeos");
        type(KeyCode.M);
        type(KeyCode.DOWN);
        type(KeyCode.TAB);
        type(KeyCode.ENTER);
        lookup(e -> e instanceof TableRow).tryQuery().ifPresent(this::doubleClickOn);
        clickOn("_Convert to Mp3");
        ConsoleUtils.waitAllProcesses();
    }

    @Test
    public void splitAudio() {
        clickOn("Carregar Musicas");
        type(KeyCode.M);
        type(KeyCode.DOWN);
        type(KeyCode.TAB);
        type(KeyCode.ENTER);
        lookup(e -> e instanceof TableRow).tryQuery().ifPresent(this::doubleClickOn);
        clickOn("_Split");
    }

    @Test
    public void closeAll() {
        closeCurrentWindow();
    }
}
