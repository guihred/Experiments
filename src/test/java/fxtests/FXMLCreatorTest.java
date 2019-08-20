package fxtests;

import static utils.PredicateEx.makeTest;

import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javafx.application.Application;
import org.junit.Test;
import org.slf4j.Logger;
import org.testfx.util.WaitForAsyncUtils;
import schema.sngpc.FXMLCreator;
import utils.HasLogging;

@SuppressWarnings("static-method")
public final class FXMLCreatorTest {

    private static final Logger LOG = HasLogging.log();
    @Test
    public void testAllClasses() {
        List<Class<? extends Application>> classes = getClasses(Application.class);
        List<Class<?>> testApplications = FXMLCreator.testApplications(classes);
        WaitForAsyncUtils.waitForFxEvents();
        if (!testApplications.isEmpty()) {
			LOG.error("classes {} /{} got errors", testApplications.size(), classes.size());
            LOG.error("classes {} with errors", classNames(testApplications));
        } else {
            LOG.info("All classes successfull");
        }
    }

    private static String classNames(List<Class<?>> testApplications) {
        return testApplications.stream().map(e -> e.getName() + ".class").collect(Collectors.joining(","));
    }

    @SuppressWarnings("unchecked")
    private static <T> List<Class<? extends T>> getClasses(Class<T> cl) {
        List<Class<? extends T>> appClass = new ArrayList<>();
        try {
            List<String> excludePackages = Arrays.asList("javafx.", "org.", "com.");
            ClassPath.from(FXMLCreatorTest.class.getClassLoader()).getTopLevelClasses().stream()
                .filter(e -> excludePackages.stream().noneMatch(p -> e.getName().contains(p)))
                .filter(makeTest(e -> cl.isAssignableFrom(e.load()))).map(ClassInfo::load)
                .map(e -> (Class<? extends T>) e).forEach(appClass::add);
        } catch (Exception e) {
            LOG.error("", e);
        }
        return appClass;
    }

}
