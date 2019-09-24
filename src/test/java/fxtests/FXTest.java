package fxtests;

import javafx.application.Application;
import org.junit.Test;

@SuppressWarnings("static-method")
public final class FXTest {
    @Test
    public void test() throws Throwable {
		FXTesting.testApps(FXTesting.getClasses(Application.class));
    }


}
