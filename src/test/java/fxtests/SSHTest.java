package fxtests;

import ethical.hacker.ssh.SSHClientUtils;
import org.apache.sshd.server.SshServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import utils.CrawlerTask;

public class SSHTest {
    private SshServer sshd;

    @Before
    public void setUp() throws Exception {
        CrawlerTask.insertProxyConfig();
        sshd = SSHClientUtils.setupTestServer();
        sshd.start();
    }

    @After
    public void tearDown() throws Exception {
        sshd.stop(true);
    }

    @Test
    public void testMessages() throws Exception {
        String name = SSHClientUtils.getCurrentTestName();
        SSHClientUtils.sendMessage("ipconfig", SSHClientUtils.TEST_LOCALHOST, sshd.getPort(),
            name, name, System.out);
    }

}
