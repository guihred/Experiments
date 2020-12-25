package fxtests;

import ethical.hacker.ssh.*;
import java.io.PrintStream;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import org.apache.sshd.server.SshServer;
import org.assertj.core.api.exception.RuntimeIOException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.testfx.util.WaitForAsyncUtils;
import utils.ExtractUtils;

public class FXSSHTest extends AbstractTestExecution {
    private SshServer sshd;

    @Before
    public void setUp() throws Exception {
        ExtractUtils.insertProxyConfig();
        sshd = BaseTestSupport.setupTestServer();
        sshd.start();
    }

    @After
    public void tearDown() throws Exception {
        sshd.stop(true);
    }

    @Test
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
                new PrintTextStream(System.out, true, "UTF-8", new SimpleStringProperty()));
    }

    @Test
    public void verifySSHApp() {
        show(SSHSessionApp.class);
        List<Button> allButtons = lookupList(Button.class);
        clickOn(last(allButtons));
        List<TextField> fields = lookupList(TextField.class);
        clickOn(last(fields));
        type(typeText("git push"));
        tryClickOn(allButtons.get(0));
    }

    @Test
    public void  verifySSHMessages2() throws Exception {
        String name = BaseTestSupport.getCurrentTestName();
        SSHClientUtils.sendMessage("exit", BaseTestSupport.TEST_LOCALHOST, sshd.getPort(), name, name,
            new PrintStream(System.out));
    }

    @Test(expected = RuntimeIOException.class)
    public void verifySSHSessionApp2() {
        show(SSHSessionApp.class);
        List<Button> allButtons = lookupList(Button.class);
        clickOn(last(allButtons));
        WaitForAsyncUtils.waitForFxEvents();
        sleep(5000);
        List<TextField> fields = lookupList(TextField.class);
        clickOn(last(fields));
        type(typeText("ipconfig"));
        tryClickOn(allButtons.get(0));
        sleep(5000);
        WaitForAsyncUtils.waitForFxEvents();
    }

    private static <T> T last(List<T> fields) {
        return fields.get(fields.size() - 1);
    }

}
