package fxtests;

import ethical.hacker.CoverageUtils;
import javafx.application.Application;
import org.junit.Test;
import paintexp.PaintMain;
import physics.Physics;
import rosario.RosarioComparadorArquivos;

@SuppressWarnings("static-method")
public final class FXTest extends AbstractTestExecution {
    @Test
    public void test() throws Throwable {
        AbstractTestExecution.testApps(CoverageUtils.getClasses(Application.class));
    }



    @Test
    public void testPageImage() {
        measureTime("PageImage.testApps",
                () -> AbstractTestExecution.testApps(PaintMain.class, Physics.class, RosarioComparadorArquivos.class));
    }


}
