package ethical.hacker.ssh;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import utils.ConsoleUtils;
import utils.StringSigaUtils;

class EchoShell extends CommandExecutionHelper {

    public static final ObservableList<String> COMMANDS = FXCollections.observableArrayList();

    public EchoShell() {
        super();
    }

    @Override
    protected boolean handleCommandLine(String command) throws Exception {
        COMMANDS.add(command);

        List<String> executeInConsoleInfo = ConsoleUtils.executeInConsoleInfo(command);
        OutputStream out = getOutputStream();
        for (String string : executeInConsoleInfo) {
            String string2 = StringSigaUtils.fixEncoding(string + "\n");
            out.write(string2.getBytes(StandardCharsets.UTF_8));
        }
        out.flush();
        return !"exit".equals(command);

    }
}