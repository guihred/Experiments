package ethical.hacker.ssh;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.nio.charset.StandardCharsets;
import org.apache.sshd.server.command.AbstractCommandSupport;
import utils.RunnableEx;

/**
 * @author <a href="mailto:dev@mina.apache.org">Apache MINA SSHD Project</a>
 */
abstract class CommandExecutionHelper extends AbstractCommandSupport {
    private String typedCommand;

    protected CommandExecutionHelper() {
        this(null);
    }

    private CommandExecutionHelper(String command) {
        super(command, null);
    }

    @Override
    public void run() {
        RunnableEx.make(() -> {
            typedCommand = getCommand();
            if (typedCommand == null) {
                try (BufferedReader r = new BufferedReader(
                    new InputStreamReader(getInputStream(), StandardCharsets.UTF_8))) {
                    for (;;) {
                        typedCommand = r.readLine();
                        if (typedCommand == null) {
                            return;
                        }
                        if (!handleCommandLine(typedCommand)) {
                            return;
                        }
                    }
                }
            }
            handleCommandLine(typedCommand);
        }, e -> {
            if (e instanceof InterruptedIOException) {
                log.trace("IGNORED Exception", e);
                return;
            }
            log.trace("Exception", e);
            String message = "Failed (" + e.getClass().getSimpleName() + ") to handle '" + typedCommand + "': "
                + e.getMessage();
            RunnableEx.make(() -> getErrorStream().write(message.getBytes(StandardCharsets.US_ASCII)),
                ioe -> log.warn("Failed ({}) to write error message={}: {}", e.getClass().getSimpleName(), message,
                    ioe.getMessage()))
                .run();
            onExit(-1, message);
        }).run();
        onExit(0);
    }

    /**
     * @param command1 The command line
     * @return {@code true} if continue accepting command
     */
    protected abstract boolean handleCommandLine(String command1) throws Exception;
}