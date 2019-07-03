package ethical.hacker.ssh;

import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.config.hosts.HostConfigEntryResolver;
import org.apache.sshd.client.keyverifier.AcceptAllServerKeyVerifier;
import org.apache.sshd.common.keyprovider.KeyIdentityProvider;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.pubkey.AcceptAllPublickeyAuthenticator;
import org.apache.sshd.server.shell.UnknownCommandFactory;

final class CoreTestSupportUtils {
    private CoreTestSupportUtils() {
        throw new UnsupportedOperationException("No instance");
    }

    public static SshClient setupTestClient(Class<?> anchor) {
        SshClient client = SshClient.setUpDefaultClient();
        client.setServerKeyVerifier(AcceptAllServerKeyVerifier.INSTANCE);
        client.setHostConfigEntryResolver(HostConfigEntryResolver.EMPTY);
        client.setKeyIdentityProvider(KeyIdentityProvider.EMPTY_KEYS_PROVIDER);
        return client;
    }

    public static SshServer setupTestServer(Class<?> anchor) {
        SshServer sshd = SshServer.setUpDefaultServer();
        sshd.setKeyPairProvider(CommonTestSupportUtils.createTestHostKeyProvider(anchor));
        sshd.setPasswordAuthenticator(BogusPasswordAuthenticator.INSTANCE);
        sshd.setPublickeyAuthenticator(AcceptAllPublickeyAuthenticator.INSTANCE);
        sshd.setShellFactory(EchoShellFactory.INSTANCE);
        sshd.setCommandFactory(UnknownCommandFactory.INSTANCE);
        return sshd;
    }
}