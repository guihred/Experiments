package fxtests;

import static fxtests.FXTesting.measureTime;
import static japstudy.JapanRefactoring.TXT_FILE;
import static japstudy.JapanRefactoring.refactorJapaneseFile;
import static japstudy.JapanRefactoring.renameFile;

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
import org.junit.Test;

public class FXJapaneseTest extends AbstractTestExecution {
    @SuppressWarnings("static-method")
    @Test
    public void testJapaneseFile() {

        measureTime("JapanRefactoring.createDatabaseFile", () -> JapanRefactoring.createDatabaseFile());
        measureTime("JapanRefactoring.refactorJapaneseFile",
            () -> refactorJapaneseFile(TXT_FILE, renameFile(TXT_FILE)));
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
        lookup(Button.class).stream().filter(t -> !lookup.contains(t)).forEach(this::clickOn);
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
    @SuppressWarnings("static-method")
    public void verifyLessons() {
        measureTime("JapaneseLessonReader.getLessons", () -> JapaneseLessonReader.getLessons("jaftranscript.docx"));
    }

}
