package fxtests;

import election.*;
import fxpro.ch06.TaskProgressApp;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import utils.CommonsFX;
import utils.HibernateUtil;

@SuppressWarnings("static-method")
public class FXHibernateTest extends AbstractTestExecution {

    @After
    public void cleanUp() {
        stop();
        HibernateUtil.setShutdownEnabled(true);
        HibernateUtil.shutdown();
    }

    @Before
    public void start() {
        CommonsFX.initializeFX();
        HibernateUtil.getSessionFactory();
        HibernateUtil.setShutdownEnabled(false);
    }

    @Test
    public void verifyCandidatoApp() {
        show(CandidatoApp.class);
        TreeView<?> treeView = lookupFirst(TreeView.class);
        TreeItem<?> lookupFirst = treeView.getRoot();
        while (lookupFirst.getChildren().isEmpty()) {
            // DOES NOTHING
            sleep(500);
        }
        targetPos(Pos.TOP_CENTER);
        clickOn(treeView);
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
        clickButtonsWait(WAIT_TIME);
    }

    @Test
    public void verifyCrawlerTask() {
        measureTime("CrawlerCompleteCandidateTask", () -> {
            CrawlerCompleteCandidateTask crawlerTask = new CrawlerCompleteCandidateTask();
            crawlerTask.performTask(0);
        });
    }

    @Test
    public void verifyTaskProgressApp() {
        show(TaskProgressApp.class);
        clickButtonsWait();
    }

}
