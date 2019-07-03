package ethical.hacker.ssh;

import org.apache.sshd.client.SshClient;
import org.apache.sshd.common.io.DefaultIoServiceFactoryFactory;
import org.apache.sshd.common.io.IoServiceFactoryFactory;
import org.apache.sshd.common.util.net.SshdSocketAddress;
import org.apache.sshd.server.SshServer;
import org.junit.Rule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.Logger;
import utils.HasLogging;

public class BaseTestSupport {

    private static final Logger LOG = HasLogging.log();

    // can be used to override the 'localhost' with an address other than 127.0.0.1
    // in case it is required
    public static final String TEST_LOCALHOST = System.getProperty("org.apache.sshd.test.localhost",
        SshdSocketAddress.LOCALHOST_IPV4);

    @Rule
    public TestWatcher rule = new TestWatcher() {

        private long startTime;

        @Override
        protected void finished(Description description) {
            long duration = System.currentTimeMillis() - startTime;
            LOG.info("\nFinished {}:{} in {} ms\n", description.getClassName(), description.getMethodName(), duration);
        }

        @Override
        protected void starting(Description description) {
            LOG.info("\nStarting {}:{}...", description.getClassName(), description.getMethodName());
            try {
                IoServiceFactoryFactory ioProvider = getIoServiceProvider();
                LOG.info("Using default provider: {}", ioProvider.getClass().getName());
            } catch (Exception t) {
                // Ignore
            }
            LOG.info("");
            startTime = System.currentTimeMillis();
        }
    };

    protected BaseTestSupport() {
        super();
    }

    public final String getCurrentTestName() {
        return getClass().getSimpleName();
    }

    protected SshClient setupTestClient() {
        return CoreTestSupportUtils.setupTestClient(getClass());
    }

    protected SshServer setupTestServer() {
        return CoreTestSupportUtils.setupTestServer(getClass());
    }

    private static IoServiceFactoryFactory getIoServiceProvider() {
        DefaultIoServiceFactoryFactory factory = DefaultIoServiceFactoryFactory
            .getDefaultIoServiceFactoryFactoryInstance();
        return factory.getIoServiceProvider();
    }
}