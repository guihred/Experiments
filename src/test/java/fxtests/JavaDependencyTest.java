package fxtests;

import static fxtests.FXTesting.measureTime;

import graphs.app.JavaFileDependency;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Test;
import org.slf4j.Logger;
import utils.HasLogging;

@SuppressWarnings("static-method")
public class JavaDependencyTest {
    private static final Logger LOG = HasLogging.log();

    @Test
    public void testJavaDependency() {

        measureTime("JavaFileDependency.displayTestsToBeRun", () -> {
            Set<String> displayTestsToBeRun = JavaFileDependency
                .displayTestsToBeRun(Arrays.asList("Chapter4", "ExcelService", "JavaFileDependency", "TextTool",
                    "LeitorArquivos", "SimpleComboBoxBuilder", "CommonsFX", "SSHSessionApp"));
            String tests = displayTestsToBeRun.stream().collect(Collectors.joining(",*"));
            LOG.info("TestsToBeRun ={}", tests);
        });
    }

}
