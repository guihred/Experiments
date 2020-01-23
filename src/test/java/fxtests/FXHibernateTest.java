package fxtests;

import election.*;
import fxpro.ch06.TaskProgressApp;
import javafx.geometry.Pos;
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
        clickButtonsWait(WAIT_TIME * 3);
    }


    @Test
    public void verifyTaskProgressApp() {
        show(TaskProgressApp.class);
        clickButtonsWait();
    }

}
