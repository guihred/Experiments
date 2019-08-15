package fxtests;

import static utils.PredicateEx.makeTest;

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;
import gaming.ex01.SnakeLauncher;
import gaming.ex04.TronLauncher;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.application.Application;
import org.junit.Test;
import org.slf4j.Logger;
import org.testfx.util.WaitForAsyncUtils;
import schema.sngpc.FXMLCreator;
import utils.HasLogging;

public final class FXEngineFXMLTest {

    private static final Logger LOG = HasLogging.log();

//    @Test
    public void test() {
        List<Class<? extends Application>> classes = Arrays.asList(
            cubesystem.SphereSystemApp.class,
            paintexp.PaintMain.class
        );
        List<Class<?>> testApplications = FXMLCreator.testApplications(classes, false);
        WaitForAsyncUtils.waitForFxEvents();
        if (!testApplications.isEmpty()) {
            LOG.error("classes {} with errors", testApplications);
        } else {
            LOG.info("All classes successfull");
        }
    }

    @Test
    public void testAllClasses() {
        List<Class<? extends Application>> classes = getClasses();
        classes.remove(SnakeLauncher.class);
        classes.remove(TronLauncher.class);
        List<Class<?>> testApplications = FXMLCreator.testApplications(classes);
        WaitForAsyncUtils.waitForFxEvents();
        if (!testApplications.isEmpty()) {
            LOG.error("classes {} with errors", testApplications);
        } else {
            LOG.info("All classes successfull");
        }
    }

    @SuppressWarnings("unchecked")
    private static List<Class<? extends Application>> getClasses() {
        List<Class<? extends Application>> appClass = new ArrayList<>();
        try {
            List<String> asList = Arrays.asList("javafx.", "org.", "com.");
            ClassPath from = ClassPath.from(FXEngineFXMLTest.class.getClassLoader());
            ImmutableSet<ClassInfo> topLevelClasses = from.getTopLevelClasses();
            topLevelClasses.stream()
                .filter(e -> asList.stream().noneMatch(p -> e.getName().contains(p)))
                .filter(makeTest(e -> Application.class.isAssignableFrom(e.load())))
                .map(ClassInfo::load)
                .map(e -> (Class<? extends Application>) e)
                .forEach(appClass::add);
        } catch (Exception e) {
            LOG.error("", e);
        }
        return appClass;
    }

}
