package crypt;

import japstudy.JapaneseLessonApplication;
import japstudy.db.HibernateUtil;
import java.util.Set;
import javafx.scene.Node;
import javafx.stage.Stage;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;
import simplebuilder.ResourceFXUtils;


public class FXEngineJapaneseTest extends ApplicationTest {

    @Override
    public void start(Stage stage) throws Exception {
        HibernateUtil.getSessionFactory();
        ResourceFXUtils.initializeFX();
        stage.setHeight(500);
        stage.setWidth(500);
        new JapaneseLessonApplication().start(stage);
    }

    @Test
    public void verify() throws Exception {
        Set<Node> queryButtons = lookup(".button").queryAll();
        for (Node e : queryButtons) {
            clickOn(e);
        }
        HibernateUtil.shutdown();
    }

}
