package fxtests;

import static language.FXTesting.measureTime;

import contest.db.ContestQuestionEditingDisplay;
import election.CrawlerCandidateTask;
import election.CrawlerCitiesTask;
import election.ElectionCrawlerApp;
import furigana.FuriganaCrawlerApp;
import fxpro.ch06.TaskProgressApp;
import fxsamples.BackgroundProcesses;
import japstudy.*;
import japstudy.db.HibernateUtil;
import java.util.Set;
import javafx.scene.Node;
import javafx.stage.Stage;
import language.FXTesting;
import org.junit.AfterClass;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;
import utils.ResourceFXUtils;
import utils.RunnableEx;

public class FXHibernateTest extends ApplicationTest {

    private Stage currentStage;

    @Override
    public void start(Stage stage) throws Exception {
        ResourceFXUtils.initializeFX();
        currentStage = stage;
    }

    @Override
    public void init() throws Exception {
        super.init();
        HibernateUtil.getSessionFactory();
        HibernateUtil.setShutdownEnabled(false);
    }

    @Test
    public void verify() throws Exception {
        measureTime("JapaneseLessonReader.getLessons", () -> JapaneseLessonReader.getLessons("jaftranscript.docx"));
        currentStage.setHeight(1000);
        interactNoWait(RunnableEx.makeRunnable(() -> new JapaneseLessonApplication().start(currentStage)));
        clickAllButtons();
        closeCurrentWindow();
        interactNoWait(RunnableEx.makeRunnable(() -> new BackgroundProcesses().start(currentStage)));
        clickAllButtons();
        interactNoWait(RunnableEx.makeRunnable(() -> new FuriganaCrawlerApp().start(currentStage)));
        clickAllButtons();
        interactNoWait(RunnableEx.makeRunnable(() -> new ElectionCrawlerApp().start(currentStage)));
        clickAllButtons();
        interactNoWait(
                RunnableEx.makeRunnable(() -> new ElectionCrawlerApp(new CrawlerCitiesTask()).start(currentStage)));
        clickAllButtons();
        interactNoWait(
                RunnableEx.makeRunnable(() -> new ElectionCrawlerApp(new CrawlerCandidateTask()).start(currentStage)));
        clickAllButtons();
        interactNoWait(RunnableEx.makeRunnable(() -> new TaskProgressApp().start(currentStage)));
        clickAllButtons();
        interactNoWait(RunnableEx.makeRunnable(() -> new ContestQuestionEditingDisplay().start(currentStage)));
        clickAllButtons();

        FXTesting.testApps(ElectionCrawlerApp.class, JapaneseLessonApplication.class,
                JapaneseLessonEditingDisplay.class, JapaneseLessonAudioSplitDisplay.class, JapaneseLessonDisplay.class);
    }

    private void clickAllButtons() {
        Set<Node> queryButtons = lookup(".button").queryAll();
        for (Node e : queryButtons) {
            if (e.isVisible()) {
                clickOn(e);
            }
            sleep(1000);
        }
    }

    @AfterClass
    public static void cleanUp() {
        HibernateUtil.setShutdownEnabled(true);
        HibernateUtil.shutdown();
    }

}
