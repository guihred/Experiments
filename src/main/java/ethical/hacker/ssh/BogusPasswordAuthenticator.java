package ethical.hacker.ssh;

import org.apache.sshd.common.util.logging.AbstractLoggingBean;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.session.ServerSession;

final class BogusPasswordAuthenticator extends AbstractLoggingBean implements PasswordAuthenticator {
    public static final BogusPasswordAuthenticator INSTANCE = new BogusPasswordAuthenticator();

    private BogusPasswordAuthenticator() {
        super();
    }

    @Override
    public boolean authenticate(String username, String password, ServerSession session) {
        boolean result = username != null && username.equals(password);
        if (log.isInfoEnabled()) {
            log.info("authenticate({}) {} / {} - success = {}", session, username, password, result);
        }

        return result;
    }
}