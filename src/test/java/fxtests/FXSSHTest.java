package fxtests;

import static fxtests.FXTesting.measureTime;

import ethical.hacker.ssh.*;
import java.io.PrintStream;
import java.util.List;
import java.util.stream.Collectors;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import org.apache.sshd.server.SshServer;
import org.assertj.core.api.exception.RuntimeIOException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.testfx.util.WaitForAsyncUtils;
import utils.CrawlerTask;

public class FXSSHTest extends AbstractTestExecution {
    private SshServer sshd;

    @Before
    public void setUp() throws Exception {
        CrawlerTask.insertProxyConfig();
        sshd = BaseTestSupport.setupTestServer();
        sshd.start();
    }

    @After
    public void tearDown() throws Exception {
        sshd.stop(true);
    }

    @Test
    @SuppressWarnings("static-method")
    public void testCommonTestSupportUtils() {
        measureTime("CommonTestSupportUtils.getClassContainerLocationURL",
            () -> CommonTestSupportUtils.getClassContainerLocationURL(Node.class));
        measureTime("CommonTestSupportUtils.getClassContainerLocationURL",
            () -> CommonTestSupportUtils.getClassContainerLocationURL(Integer.class));
    }

    @Test
    public void testMessages() throws Exception {
        String name = BaseTestSupport.getCurrentTestName();
        SSHClientUtils.sendMessage("ipconfig", BaseTestSupport.TEST_LOCALHOST, sshd.getPort(), name, name,
            new PrintTextStream(System.out, true, "UTF-8", new Text()));
    }

    @Test
    public void testMessages2() throws Exception {
        String name = BaseTestSupport.getCurrentTestName();
        SSHClientUtils.sendMessage("exit", BaseTestSupport.TEST_LOCALHOST, sshd.getPort(), name, name,
            new PrintStream(System.out));
    }


    @Test
    public void verifySSHSessionApp() {
        show(SSHSessionApp.class);
        List<Button> collect = lookup(Button.class).stream().collect(Collectors.toList());
        clickOn(last(collect));
        List<TextField> fields = lookup(TextField.class).stream().collect(Collectors.toList());
        clickOn(last(fields));
        type(typeText("git push"));
        tryClickOn(collect.get(0));
    }

    @Test(expected = RuntimeIOException.class)
    public void verifySSHSessionApp2() {
        show(SSHSessionApp.class);
        List<Button> collect = lookup(Button.class).stream().collect(Collectors.toList());
        clickOn(last(collect));
        WaitForAsyncUtils.waitForFxEvents();
        sleep(5000);
        List<TextField> fields = lookup(TextField.class).stream().collect(Collectors.toList());
        clickOn(last(fields));
        type(typeText("ipconfig"));
        tryClickOn(collect.get(0));
        sleep(5000);
        WaitForAsyncUtils.waitForFxEvents();
    }

    private static <T> T last(List<T> fields) {
        return fields.get(fields.size() - 1);
    }

}
