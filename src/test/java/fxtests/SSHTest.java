package fxtests;

import ethical.hacker.ssh.BaseTestSupport;
import ethical.hacker.ssh.SSHClientUtils;
import java.io.PrintStream;
import org.apache.sshd.server.SshServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import utils.CrawlerTask;

public class SSHTest extends AbstractTestExecution {
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
    public void testMessages() throws Exception {
		String name = BaseTestSupport.getCurrentTestName();
		SSHClientUtils.sendMessage("ipconfig", BaseTestSupport.TEST_LOCALHOST, sshd.getPort(),
				name, name, new PrintStream(System.out));
    }

}
