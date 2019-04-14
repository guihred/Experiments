package fxtests;

import ethical.hacker.EthicalHackApp;
import org.junit.Test;
import utils.ConsoleUtils;

public class FXEngineEthicalHackTest extends AbstractTestExecution {

	@Test
	public void verify() throws Exception {
		show(EthicalHackApp.class);
		tryClickButtons();
		ConsoleUtils.waitAllProcesses();
	}
}
