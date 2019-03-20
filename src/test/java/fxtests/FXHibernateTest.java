package fxtests;

import static fxtests.FXTesting.measureTime;

import contest.db.ContestQuestionEditingDisplay;
import election.CrawlerCandidateTask;
import election.CrawlerCitiesTask;
import election.CrawlerCompleteCandidateTask;
import election.ElectionCrawlerApp;
import furigana.FuriganaCrawlerApp;
import fxpro.ch06.TaskProgressApp;
import fxsamples.BackgroundProcesses;
import japstudy.JapaneseLessonApplication;
import japstudy.JapaneseLessonAudioSplitDisplay;
import japstudy.JapaneseLessonDisplay;
import japstudy.JapaneseLessonEditingDisplay;
import japstudy.JapaneseLessonReader;
import japstudy.db.HibernateUtil;
import java.util.Set;
import javafx.scene.Node;
import org.junit.AfterClass;
import org.junit.Test;

public class FXHibernateTest extends AbstractTestExecution {

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
	show(new JapaneseLessonApplication());
	clickAllButtons();
	closeCurrentWindow();
	show(new BackgroundProcesses());
	clickAllButtons();
	show(new FuriganaCrawlerApp());
	clickAllButtons();
	show(new ElectionCrawlerApp(new CrawlerCitiesTask()));
	clickAllButtons();
	show(new ElectionCrawlerApp(new CrawlerCandidateTask()));
	clickAllButtons();
	show(new ElectionCrawlerApp(new CrawlerCompleteCandidateTask()));
	clickAllButtons();
	show(new TaskProgressApp());
	clickAllButtons();
	show(new ContestQuestionEditingDisplay());
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
