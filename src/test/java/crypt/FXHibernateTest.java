package crypt;

import static crypt.FXTesting.measureTime;

import election.experiment.ElectionCrawlerApp;
import exercise.java8.RunnableEx;
import furigana.experiment.FuriganaCrawlerApp;
import fxproexercises.ch06.TaskProgressApp;
import japstudy.*;
import japstudy.db.HibernateUtil;
import java.util.Set;
import javafx.scene.Node;
import javafx.stage.Stage;
import org.junit.AfterClass;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;
import simplebuilder.ResourceFXUtils;

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
        interactNoWait(RunnableEx.makeRunnable(() -> new FuriganaCrawlerApp().start(currentStage)));
        clickAllButtons();
        interactNoWait(RunnableEx.makeRunnable(() -> new ElectionCrawlerApp().start(currentStage)));
        clickAllButtons();
        interactNoWait(RunnableEx.makeRunnable(() -> new TaskProgressApp().start(currentStage)));
        clickAllButtons();

        FXTesting.testApps(ElectionCrawlerApp.class, JapaneseLessonApplication.class,
                JapaneseLessonEditingDisplay.class, JapaneseLessonAudioSplitDisplay.class, JapaneseLessonDisplay.class);
    }

    private void clickAllButtons() {
        Set<Node> queryButtons = lookup(".button").queryAll();
        for (Node e : queryButtons) {
            clickOn(e);
        }
    }

    @AfterClass
    public static void cleanUp() {
        HibernateUtil.setShutdownEnabled(true);
        HibernateUtil.shutdown();
    }

}
