package fxtests;

import javafx.scene.Node;
import ml.WordSuggetionApp;
import org.junit.Test;


public class FXEngineWordSearchTest extends AbstractTestExecution {

	@Test
	public void verify() throws Exception {
		show(WordSuggetionApp.class);
		Node query = lookup(".text-field").query();
		clickOn(query);
		write("new york ");
	}

}
