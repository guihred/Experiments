package ethical.hacker.ssh;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import utils.ConsoleUtils;

class EchoShell extends CommandExecutionHelper {
    public EchoShell() {
        super();
    }

    @Override
    protected boolean handleCommandLine(String command) throws Exception {
        List<String> executeInConsoleInfo = ConsoleUtils.executeInConsoleInfo(command);
        OutputStream out = getOutputStream();
        for (String string : executeInConsoleInfo) {
            out.write((string + "\n").getBytes(StandardCharsets.UTF_8));
        }
        out.flush();
        return !"exit".equals(command);

    }
}