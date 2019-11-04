package fxtests;

import static fxtests.FXTesting.measureTime;

import election.*;
import furigana.FuriganaCrawlerApp;
import fxpro.ch06.TaskProgressApp;
import fxsamples.BackgroundProcesses;
import japstudy.*;
import java.util.Set;
import java.util.stream.Collectors;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Cell;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import utils.HibernateUtil;
import utils.ResourceFXUtils;

@SuppressWarnings("static-method")
public class FXHibernateTest extends AbstractTestExecution {

    @After
    public void cleanUp() {
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
    public void verifyBackgroundProcesses() {
        show(BackgroundProcesses.class);
        clickButtonsWait();
    }

    @Test
    public void verifyCandidatoApp() {
        show(CandidatoApp.class);
        targetPos(Pos.TOP_CENTER);
        clickOn(lookupFirst(TreeView.class));
        type(KeyCode.SPACE);
        type(KeyCode.RIGHT);
        type(KeyCode.DOWN, 3);
        lookup(CheckBox.class).forEach(this::clickOn);
        lookup(CheckBox.class).forEach(this::clickOn);
        targetPos(Pos.CENTER);
    }

    @Test
    public void verifyCrawlerCandidates2018Task() {
        show(new ElectionCrawlerApp(new CrawlerCandidates2018Task()));
        clickButtonsWait();
    }





    @Test
    public void verifyCrawlerCandidateTask() {
        show(new ElectionCrawlerApp(new CrawlerCandidateTask()));
        clickButtonsWait();
    }

    @Test
    public void verifyCrawlerCitiesTask() {
        show(new ElectionCrawlerApp(new CrawlerCitiesTask()));
        clickButtonsWait();
    }

    @Test
    public void verifyCrawlerCompleteCandidateTask() {
        show(new ElectionCrawlerApp(new CrawlerCompleteCandidateTask()));
        clickButtonsWait();
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
        clickOn(randomItem(lookup(Cell.class)));
        type(KeyCode.SHIFT);
        clickButtonsWait();
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

    @Test
    public void verifyTaskProgressApp() {
        show(TaskProgressApp.class);
        clickButtonsWait();
    }

}
