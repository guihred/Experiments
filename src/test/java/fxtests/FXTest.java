package fxtests;

import ethical.hacker.ssh.SSHSessionApp;
import ex.j8.Chapter4;
import java.io.File;
import java.util.List;
import java.util.stream.Collectors;
import javafx.application.Application;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import org.junit.Test;
import utils.StageHelper;

@SuppressWarnings("static-method")
public final class FXTest extends AbstractTestExecution {
    public void test() throws Throwable {
        FXTesting.testApps(FXTesting.getClasses(Application.class));
    }

    @Test
    public void testChapter4() throws Throwable {
        FXTesting.testApps(Chapter4.Ex1.class, Chapter4.Ex4.class, Chapter4.Ex10.class, Chapter4.Ex5.class,
            Chapter4.Ex6.class, Chapter4.Ex7.class, Chapter4.Ex9.class);
    }

    @Test
    public void testDisplayCSSStyler() throws Throwable {
        showNewStage(randomItem(FXTesting.getClasses(Application.class)));
        String[] list = new File("src/main/resources/").list((d, f) -> f.endsWith(".css"));
        interactNoWait(() -> StageHelper.displayCSSStyler(lookup(".root").query().getScene(), list[0]));
        tryClickButtons();
    }

    @Test
    public void verifySSHSessionApp() {
        showNewStage(SSHSessionApp.class);
        List<Button> collect = lookup(Button.class).stream().collect(Collectors.toList());
        clickOn(collect.get(collect.size() - 1));
        List<TextField> fields = lookup(TextField.class).stream().collect(Collectors.toList());
        clickOn(fields.get(fields.size() - 1));
        type(typeText("ipconfig"));
        clickOn(collect.get(0));
    }
}
