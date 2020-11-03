package ethical.hacker.ssh;

import static org.junit.Assert.assertFalse;

/**
 * @author <a href="mailto:dev@mina.apache.org">Apache MINA SSHD Project</a>
 */
import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
import org.assertj.core.api.exception.RuntimeIOException;
import utils.ex.RunnableEx;

public class SSHClientUtils extends BaseTestSupport {

    public static void sendMessage(String msg, String host, int port, String username, String password,
        OutputStream out) throws InterruptedException {
        final List<Throwable> errors = new ArrayList<>();
        final CountDownLatch latch = new CountDownLatch(1);
        RunnableEx.runNewThread(() -> {
            try {
                runClient(msg, host, port, username, password, out);
            } catch (Exception t) {
                errors.add(t);
            } finally {
                latch.countDown();
            }
        });
        latch.await();
        if (!errors.isEmpty()) {
            throw new RuntimeIOException("Errors", errors.get(0));
        }
    }

	private static void runClient(String message, String host, int port, String username, String password,
			OutputStream out)
        throws IOException {

		String msg = message;
        final int windowSize = 1024 * 8;
        int maxPacketSize = 2 * windowSize;
        try (SshClient client = setupTestClient()) {
            PropertyResolverUtils.updateProperty(client, FactoryManager.MAX_PACKET_SIZE, maxPacketSize);
            PropertyResolverUtils.updateProperty(client, FactoryManager.WINDOW_SIZE, windowSize);
            client.setKeyExchangeFactories(
                Collections.singletonList(ClientBuilder.DH2KEX.apply(BuiltinDHFactories.dhg1)));
            client.setCipherFactories(Collections.singletonList(BuiltinCiphers.blowfishcbc));
            client.start();
            try (ClientSession session = client.connect(username, host, port).verify(7L, TimeUnit.SECONDS)
                .getSession()) {
                session.addPasswordIdentity(password);
                session.auth().verify(10L, TimeUnit.SECONDS);


                try (OutputStream out2 = out;
                    ByteArrayOutputStream err = new ByteArrayOutputStream();
                    ClientChannel channel = session.createChannel(Channel.CHANNEL_SHELL)) {
                    channel.setOut(out2);
                    channel.setErr(err);

                    try {
                        channel.open().verify(9L, TimeUnit.SECONDS);
                        try (OutputStream pipedIn = channel.getInvertedIn()) {
                            msg += "\n";
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

                }
            } finally {
                client.stop();
            }
        }
    }
}
