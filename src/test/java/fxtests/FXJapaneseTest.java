package fxtests;

import furigana.FuriganaCrawlerApp;
import japstudy.*;
import java.util.Set;
import java.util.stream.Collectors;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Cell;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import utils.HibernateUtil;
import utils.ResourceFXUtils;

@SuppressWarnings("static-method")
public class FXJapaneseTest extends AbstractTestExecution {
    @After
    public void cleanUp() {
        stop();
        HibernateUtil.setShutdownEnabled(true);
        HibernateUtil.shutdown();
    }

    @Before
    public void start() {
        ResourceFXUtils.initializeFX();
        HibernateUtil.getSessionFactory();
        HibernateUtil.setShutdownEnabled(false);
    }

    @Test
    public void testJapaneseFile() {

        measureTime("JapanRefactoring.createDatabaseFile", () -> JapanRefactoring.createDatabaseFile());
        measureTime("JapanRefactoring.refactorJapaneseFile",
            () -> JapanRefactoring.refactorJapaneseFile(JapanRefactoring.TXT_FILE,
                JapanRefactoring.renameFile(JapanRefactoring.TXT_FILE)));
    }

    @Test
    public void verifyFuriganaCrawlerApp() {
        show(FuriganaCrawlerApp.class);
        clickButtonsWait();
    }

    @Test
    public void verifyJapaneseLessonApplication() {
        show(JapaneseLessonApplication.class);
        Set<Button> lookup = lookup(Button.class);
        doubleClickOn(randomItem(lookup(Cell.class)));
        lookupList(Button.class, t -> !lookup.contains(t)).forEach(this::clickOn);
        type(KeyCode.SHIFT);
        tryClickButtons();
        clickOn(lookupFirst(TextField.class));
        tryClickButtons();
        type(KeyCode.ENTER);
        type(KeyCode.SHIFT);
    }

    @Test
    public void verifyJapaneseLessonAudioSplitDisplay() {
        show(JapaneseLessonAudioSplitDisplay.class);
        for (Node e : lookup(Button.class).stream().limit(4).collect(Collectors.toList())) {
            clickOn(e);
            sleep(WAIT_TIME);
        }
        clickButtonsWait();
    }

    @Test
    public void verifyJapaneseLessonDisplay() {
        show(JapaneseLessonDisplay.class);
        clickButtonsWait();
        lookup(CheckBox.class).forEach(this::clickOn);
        clickButtonsWait();
        type(KeyCode.ENTER);
    }

    @Test
    public void verifyJapaneseLessonEditingDisplay() {
        show(JapaneseLessonEditingDisplay.class);
        clickButtonsWait();
    }

    @Test
    public void verifyLessons() {
        measureTime("JapaneseLessonReader.getLessons", () -> JapaneseLessonReader.getLessons("jaftranscript.docx"));
    }

}
