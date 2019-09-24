package fxtests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javafx.application.Application;
import org.junit.Test;
import org.slf4j.Logger;
import org.testfx.util.WaitForAsyncUtils;
import paintexp.PaintMain;
import schema.sngpc.FXMLCreatorHelper;
import utils.HasLogging;

@SuppressWarnings("static-method")
public final class FXMLCreatorTest {

	static final Logger LOG = HasLogging.log();

	// @Test
    public void testAllClasses() {
		List<Class<? extends Application>> classes = FXTesting.getClasses(Application.class);
        testApplications(classes);
    }

	@Test
    public void testErrorClasses() {

		List<Class<? extends Application>> classes = Arrays.asList(PaintMain.class);
        testApplications(classes);
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
            LOG.error("classes {} with errors", classNames(testApplications));
        }
        if (!differentTree.isEmpty()) {
            LOG.error("classes {}/{} with different trees", differentTree.size(), classes.size());
            LOG.error("classes {} with different trees", classNames(differentTree));
        }
    }

}
