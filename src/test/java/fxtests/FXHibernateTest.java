package fxtests;

import static fxtests.FXTesting.measureTime;

import contest.ContestApplication;
import contest.ContestQuestionEditingDisplay;
import election.*;
import furigana.FuriganaCrawlerApp;
import fxpro.ch06.TaskProgressApp;
import fxsamples.BackgroundProcesses;
import japstudy.*;
import java.util.Set;
import javafx.scene.Node;
import org.junit.AfterClass;
import org.junit.Test;
import utils.CrawlerTask;
import utils.HibernateUtil;
import utils.RunnableEx;


public class FXHibernateTest extends AbstractTestExecution {

    private static final int WAIT_TIME = 5000;

    @Override
    public void init() throws Exception {
        super.init();
        HibernateUtil.getSessionFactory();
        HibernateUtil.setShutdownEnabled(false);
    }
    @Test
    public void verify() throws Exception {
        measureTime("JapaneseLessonReader.getLessons", () -> JapaneseLessonReader.getLessons("jaftranscript.docx"));
        show(new JapaneseLessonApplication());
        clickAllButtons();
        closeCurrentWindow();
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
        CrawlerTask.insertProxyConfig();
        show(new CandidatoApp());
        sleep(WAIT_TIME * 2);
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

    private void clickAllButtons() {
        Set<Node> queryButtons = lookup(".button").queryAll();
        for (Node e : queryButtons) {
			RunnableEx.run(() -> clickOn(e));
            sleep(WAIT_TIME);
        }
    }

    @AfterClass
    public static void cleanUp() {
        HibernateUtil.setShutdownEnabled(true);
        HibernateUtil.shutdown();
    }

}
