package fxtests;

import ethical.hacker.EthicalHackApp;
import java.util.Set;
import javafx.scene.Node;
import org.junit.Test;
import utils.ConsoleUtils;


public class FXEngineEthicalHackTest extends AbstractTestExecution {

	@Test
	public void verify() throws Exception {
		show(EthicalHackApp.class);
		Set<Node> queryButtons = lookup(".button").queryAll();
		for (Node e : queryButtons) {
			clickOn(e);
		}
		ConsoleUtils.waitAllProcesses();
	}
}
