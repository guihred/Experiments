package ethical.hacker.ssh;

import org.apache.sshd.client.SshClient;
import org.apache.sshd.common.util.net.SshdSocketAddress;
import org.apache.sshd.server.SshServer;
import org.slf4j.Logger;
import utils.HasLogging;

public class BaseTestSupport {

    private static final Logger LOG = HasLogging.log();

    // can be used to override the 'localhost' with an address other than 127.0.0.1
    // in case it is required
    public static final String TEST_LOCALHOST = System.getProperty("org.apache.sshd.test.localhost",
        SshdSocketAddress.LOCALHOST_IPV4);

    protected BaseTestSupport() {
        super();
    }

    public final static String getCurrentTestName() {
        return HasLogging.getCurrentClass(0).replaceAll("^.+\\.(\\w+)$", "$1");
    }

    public static SshClient setupTestClient() {
        return CoreTestSupportUtils.setupTestClient();
    }

    public static SshServer setupTestServer() {
        try {
            return CoreTestSupportUtils.setupTestServer(Class.forName(HasLogging.getCurrentClass(0)));
        } catch (Exception e) {
            LOG.error("", e);
            return null;
        }
    }
}