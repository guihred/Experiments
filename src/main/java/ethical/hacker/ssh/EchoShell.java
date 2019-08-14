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
	protected boolean handleCommandLine(String command1) throws Exception {
		COMMANDS.add(command1);

		List<String> executeInConsoleInfo = ConsoleUtils.executeInConsoleInfo(command1);
		OutputStream out1 = getOutputStream();
        for (String string : executeInConsoleInfo) {
            String string2 = StringSigaUtils.fixEncoding(string + "\n");
			out1.write(string2.getBytes(StandardCharsets.UTF_8));
        }
		out1.flush();
		return !"exit".equals(command1);

    }
}