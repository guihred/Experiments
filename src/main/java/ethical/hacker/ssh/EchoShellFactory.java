package ethical.hacker.ssh;

import org.apache.sshd.server.command.Command;
import org.apache.sshd.server.shell.ShellFactory;

class EchoShellFactory implements ShellFactory {
    public static final EchoShellFactory INSTANCE = new EchoShellFactory();

    private EchoShellFactory() {
        super();
    }

    @Override
    public Command create() {
        return new EchoShell();
    }
}