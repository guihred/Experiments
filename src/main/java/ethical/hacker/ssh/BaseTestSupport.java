package ethical.hacker.ssh;

import static utils.HasLogging.getCurrentClass;

import org.apache.sshd.client.SshClient;
import org.apache.sshd.common.util.net.SshdSocketAddress;
import org.apache.sshd.server.SshServer;
import utils.HasLogging;
import utils.SupplierEx;

public class BaseTestSupport {

    // can be used to override the 'localhost' with an address other than 127.0.0.1
    // in case it is required
    public static final String TEST_LOCALHOST = System.getProperty("org.apache.sshd.test.localhost",
        SshdSocketAddress.LOCALHOST_IPV4);

    protected BaseTestSupport() {
    }

    public static final String getCurrentTestName() {
        return HasLogging.getCurrentClass(0).replaceAll("^.+\\.(\\w+)$", "$1");
    }

    public static SshClient setupTestClient() {
        return CoreTestSupportUtils.setupTestClient();
    }

    public static SshServer setupTestServer() {
		return SupplierEx.get(() -> CoreTestSupportUtils.setupTestServer(Class.forName(getCurrentClass(0))));
    }
}