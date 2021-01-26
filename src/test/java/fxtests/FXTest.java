package fxtests;

import audio.mp3.PageImage;
import javafx.application.Application;
import ml.data.CoverageUtils;
import org.junit.Test;

@SuppressWarnings("static-method")
public final class FXTest extends AbstractTestExecution {
    public void test() throws Throwable {
        AbstractTestExecution.testApps(CoverageUtils.getClasses(Application.class));
    }



    @Test
    public void testPageImage() {
        measureTime("PageImage.testApps", () -> AbstractTestExecution.testApps(PageImage.class));
    }


}
