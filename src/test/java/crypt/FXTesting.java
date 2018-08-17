package crypt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.stage.Stage;
import org.junit.Assert;
import simplebuilder.HasLogging;

public final class FXTesting implements HasLogging {
    JFXPanel jfxPanel = new JFXPanel();
    Class<? extends Application> classError;


    public void testApplications(List<Class<? extends Application>> applicationClasses)
            throws InstantiationException, IllegalAccessException {
        List<Object> testedApps = Collections.synchronizedList(new ArrayList<>());
        long currentTimeMillis = System.currentTimeMillis();
        for (Class<? extends Application> class1 : applicationClasses) {

            Application newInstance = class1.newInstance();
            Platform.runLater(() -> {
                try {
                    getLogger().info("TESTING " + class1.getSimpleName());
                    Stage primaryStage = new Stage();
                    newInstance.start(primaryStage);
                    primaryStage.close();
                    getLogger().info("ENDED " + class1.getSimpleName());
                    testedApps.add(newInstance);
                } catch (Throwable e) {
                    getLogger().error("", e);
                    setClass(class1);
                }
            });
        }
        int size = testedApps.size();
        while (testedApps.size() < applicationClasses.size() && classError == null) {
            if (size != testedApps.size()) {
                getLogger().info("{}/{} done", testedApps.size(), applicationClasses.size());
                size = testedApps.size();
            }
            if (currentTimeMillis - System.currentTimeMillis() > 2 * 60 * 1000) {//2 minutes
                Assert.fail("Test is taking too long");
                break;
            }
        }
        if (classError != null) {
            Assert.fail("Class " + classError.getSimpleName() + " threw an exception");
        }
        Assert.assertTrue("TESTS SUCCESSFULL", true);
    }


    private synchronized void setClass(Class<? extends Application> class1) {
        classError = class1;
    }
}
