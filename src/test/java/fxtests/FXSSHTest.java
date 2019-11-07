package fxtests;

import static fxtests.FXTesting.measureTime;
import static utils.RunnableEx.ignore;

import ethical.hacker.ssh.BaseTestSupport;
import ethical.hacker.ssh.CommonTestSupportUtils;
import ethical.hacker.ssh.SSHClientUtils;
import ethical.hacker.ssh.SSHSessionApp;
import java.io.PrintStream;
import java.util.List;
import java.util.stream.Collectors;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import org.apache.sshd.server.SshServer;
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
            new PrintStream(System.out));
    }


    @Test
    public void verifySSHSessionApp() {
        show(SSHSessionApp.class);
        List<Button> collect = lookup(Button.class).stream().collect(Collectors.toList());
        clickOn(collect.get(collect.size() - 1));
        List<TextField> fields = lookup(TextField.class).stream().collect(Collectors.toList());
        clickOn(fields.get(fields.size() - 1));
        type(typeText("git push"));
        ignore(() -> clickOn(collect.get(0)));
    }

    @Test
    public void verifySSHSessionApp2() {
        show(SSHSessionApp.class);
        List<Button> collect = lookup(Button.class).stream().collect(Collectors.toList());
        clickOn(collect.get(collect.size() - 1));
        WaitForAsyncUtils.waitForFxEvents();
        sleep(2000);
        List<TextField> fields = lookup(TextField.class).stream().collect(Collectors.toList());
        clickOn(fields.get(fields.size() - 1));
        type(typeText("ipconfig"));
        ignore(() -> clickOn(collect.get(0)));
        WaitForAsyncUtils.waitForFxEvents();
    }

}
