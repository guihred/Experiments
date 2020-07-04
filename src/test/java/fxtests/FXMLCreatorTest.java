package fxtests;

import ethical.hacker.MalwareInvestigator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javafx.application.Application;
import ml.data.CoverageUtils;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.testfx.util.WaitForAsyncUtils;
import schema.sngpc.FXMLCreatorHelper;
import utils.HasLogging;
import utils.HibernateUtil;

@SuppressWarnings("static-method")
public final class FXMLCreatorTest {

    private static final Logger LOG = HasLogging.log();

    @Test
    public void testAllClasses() {
        HibernateUtil.setShutdownEnabled(false);
        List<Class<? extends Application>> classes = CoverageUtils.getUncoveredApplications();
        FXMLCreatorTest.testApplications(classes);
    }

    @Test
	public  void testClassesNotClose() {
        List<Class<? extends Application>> classes = Arrays.asList(MalwareInvestigator.class);
        FXMLCreatorHelper.testApplications(classes, true);
    }

    private static <T> String classNames(List<Class<? extends T>> testApplications) {
        return testApplications.stream().map(e -> e.getName() + ".class").collect(Collectors.joining(","));
    }

    private static void testApplications(List<Class<? extends Application>> classes) {
        List<Class<? extends Application>> differentTree = new ArrayList<>();
		List<Class<?>> testApplications = FXMLCreatorHelper.testApplications(classes, true, differentTree);
		WaitForAsyncUtils.waitForFxEvents();
        if (testApplications.isEmpty()) {
            LOG.info("All classes successfull");
        } else {
            LOG.error("classes {}/{} got errors", testApplications.size(), classes.size());
            String classNames = classNames(testApplications);
            LOG.error("classes {} with errors", classNames);
            Assert.fail("Exception in " + classNames);
        }
        if (!differentTree.isEmpty()) {
            LOG.error("classes {}/{} with different trees", differentTree.size(), classes.size());
            LOG.error("classes {} with different trees", classNames(differentTree));
        }
    }

}
