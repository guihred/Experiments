package ethical.hacker.ssh;

import java.io.*;
import java.nio.charset.StandardCharsets;
import org.apache.sshd.server.command.AbstractCommandSupport;

/**
 * @author <a href="mailto:dev@mina.apache.org">Apache MINA SSHD Project</a>
 */
abstract class CommandExecutionHelper extends AbstractCommandSupport {
    protected CommandExecutionHelper() {
        this(null);
    }

    private CommandExecutionHelper(String command) {
        super(command, null);
    }

    @Override
    public void run() {
		String command1 = getCommand();
        try {
			if (command1 == null) {
                try (BufferedReader r = new BufferedReader(
                    new InputStreamReader(getInputStream(), StandardCharsets.UTF_8))) {
                    for (;;) {
						command1 = r.readLine();
						if (command1 == null) {
                            return;
                        }

						if (!handleCommandLine(command1)) {
                            return;
                        }
                    }
                }
            }
			handleCommandLine(command1);
        } catch (InterruptedIOException e) {
            log.trace("IGNORED Exception", e);
            // Ignore - signaled end
        } catch (Exception e) {
            log.trace("Exception", e);
			String message = "Failed (" + e.getClass().getSimpleName() + ") to handle '" + command1 + "': "
                + e.getMessage();
            try {
                OutputStream stderr = getErrorStream();
                stderr.write(message.getBytes(StandardCharsets.US_ASCII));
            } catch (IOException ioe) {
                log.trace("Exception", ioe);
                log.warn("Failed ({}) to write error message={}: {}", e.getClass().getSimpleName(), message,
                    ioe.getMessage());
            } finally {
                onExit(-1, message);
            }
        } finally {
            onExit(0);
        }
    }

    /**
	 * @param command1
	 *            The command line
	 * @return {@code true} if continue accepting command
	 */
	protected abstract boolean handleCommandLine(String command1) throws Exception;
}