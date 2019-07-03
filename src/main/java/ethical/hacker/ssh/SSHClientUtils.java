package ethical.hacker.ssh;

import static org.junit.Assert.assertFalse;

/**
 * @author <a href="mailto:dev@mina.apache.org">Apache MINA SSHD Project</a>
 */
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.apache.sshd.client.ClientBuilder;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.channel.ClientChannel;
import org.apache.sshd.client.channel.ClientChannelEvent;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.FactoryManager;
import org.apache.sshd.common.PropertyResolverUtils;
import org.apache.sshd.common.channel.Channel;
import org.apache.sshd.common.cipher.BuiltinCiphers;
import org.apache.sshd.common.kex.BuiltinDHFactories;
import org.apache.sshd.server.SshServer;
//import org.apache.sshd.util.test.BaseTestSupport;
import org.junit.After;
import org.junit.Before;
import utils.CrawlerTask;

//@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SSHClientUtils extends BaseTestSupport {

    private SshServer sshd;
    private int port = 22;

    @Before
    public void setUp() throws Exception {
        CrawlerTask.insertProxyConfig();
        sshd = setupTestServer();
        sshd.start();
        port = sshd.getPort();
    }

    @After
    public void tearDown() throws Exception {
        sshd.stop(true);
    }

//    @Test
    public void testLoad() throws Exception {
        test("ipconfig", 1, 4);
    }

    private void runClient(String msg) throws Exception {
        try (SshClient client = setupTestClient()) {
            PropertyResolverUtils.updateProperty(client, FactoryManager.MAX_PACKET_SIZE, 1024 * 16);
            PropertyResolverUtils.updateProperty(client, FactoryManager.WINDOW_SIZE, 1024 * 8);
            client.setKeyExchangeFactories(
                Collections.singletonList(ClientBuilder.DH2KEX.apply(BuiltinDHFactories.dhg1)));
            client.setCipherFactories(Collections.singletonList(BuiltinCiphers.blowfishcbc));
            client.start();
            try (ClientSession session = client.connect(getCurrentTestName(), TEST_LOCALHOST, port)
                .verify(7L, TimeUnit.SECONDS).getSession()) {
                session.addPasswordIdentity(getCurrentTestName());
                session.auth().verify(10L, TimeUnit.SECONDS);

                try (OutputStream out = System.out;
                    ByteArrayOutputStream err = new ByteArrayOutputStream();
                    ClientChannel channel = session.createChannel(Channel.CHANNEL_SHELL)) {
                    channel.setOut(out);
                    channel.setErr(err);

                    try {
                        channel.open().verify(9L, TimeUnit.SECONDS);
                        try (OutputStream pipedIn = channel.getInvertedIn()) {
                            msg += "\nexit\n";
                            pipedIn.write(msg.getBytes(StandardCharsets.UTF_8));
                            pipedIn.flush();
                        }

                        Collection<ClientChannelEvent> result = channel.waitFor(EnumSet.of(ClientChannelEvent.CLOSED),
                            TimeUnit.SECONDS.toMillis(15L));
                        assertFalse("Timeout while waiting for channel closure",
                            result.contains(ClientChannelEvent.TIMEOUT));
                    } finally {
                        channel.close(false);
                    }

//                    assertArrayEquals("Mismatched message data", msg.getBytes(StandardCharsets.UTF_8),
//                        out.toByteArray());
                }
            } finally {
                client.stop();
            }
        }
    }

    private void test(final String msg, final int nbThreads, final int nbSessionsPerThread) throws Exception {
        final List<Throwable> errors = new ArrayList<>();
        final CountDownLatch latch = new CountDownLatch(nbThreads);
        for (int i = 0; i < nbThreads; i++) {
            Runnable r = () -> {
                try {
                    for (int i1 = 0; i1 < nbSessionsPerThread; i1++) {
                        runClient(msg);
                    }
                } catch (Throwable t) {
                    errors.add(t);
                } finally {
                    latch.countDown();
                }
            };
            new Thread(r).start();
        }
        latch.await();
        if (errors.size() > 0) {
            throw new Exception("Errors", errors.get(0));
        }
    }

    public static void main(String[] args) throws Exception {
//        new SSHClientUtils().setUp();
//        while (true) {
//
//        }
        new SSHClientUtils().testLoad();
    }
}
