package fxtests;

import static fxtests.FXTesting.measureTime;

import contest.ContestApplication;
import contest.ContestQuestionEditingDisplay;
import election.*;
import furigana.FuriganaCrawlerApp;
import fxpro.ch06.TaskProgressApp;
import fxsamples.BackgroundProcesses;
import japstudy.*;
import javafx.scene.Node;
import javafx.scene.control.Button;
import org.junit.AfterClass;
import org.junit.Test;
import utils.HibernateUtil;
import utils.RunnableEx;

public class FXHibernateTest extends AbstractTestExecution {

    private static final int WAIT_TIME = 1000;

    @Override
    public void init() throws Exception {
        super.init();
        HibernateUtil.getSessionFactory();
        HibernateUtil.setShutdownEnabled(false);
    }

    @Test
    public void verifyApplications() throws Exception {
        show(new BackgroundProcesses());
        clickAllButtons();
        show(new FuriganaCrawlerApp());
        clickAllButtons();
        show(new TaskProgressApp());
        clickAllButtons();
        show(new ContestQuestionEditingDisplay());
        clickAllButtons();
        show(new JapaneseLessonEditingDisplay());
        clickAllButtons();
        show(new JapaneseLessonAudioSplitDisplay());
        clickAllButtons();
        show(new JapaneseLessonDisplay());
        clickAllButtons();
        show(new ContestApplication());
    }

    @Test
    public void verifyCandidatoApp() {
        show(new CandidatoApp());
    }

    @Test
    public void verifyElectionApp() {
        show(new ElectionCrawlerApp(new CrawlerCitiesTask()));
        clickAllButtons();
        show(new ElectionCrawlerApp(new CrawlerCandidateTask()));
        clickAllButtons();
        show(new ElectionCrawlerApp(new CrawlerCandidates2018Task()));
        clickAllButtons();
        show(new ElectionCrawlerApp(new CrawlerCompleteCandidateTask()));
        clickAllButtons();
    }

    @Test
    public void verifyJapaneseLessonApplication() throws Exception {
        show(new JapaneseLessonApplication());
        clickAllButtons();
    }

    @Test
    @SuppressWarnings("static-method")
    public void verifyLessons() {
        measureTime("JapaneseLessonReader.getLessons", () -> JapaneseLessonReader.getLessons("jaftranscript.docx"));
    }

    private void clickAllButtons() {
        for (Node e : lookup(Button.class)) {
            RunnableEx.ignore(() -> clickOn(e));
            sleep(WAIT_TIME);
        }
    }

    @AfterClass
    public static void cleanUp() {
        HibernateUtil.setShutdownEnabled(true);
        HibernateUtil.shutdown();
    }

}
