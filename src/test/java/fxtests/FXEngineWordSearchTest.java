package fxtests;

import ml.WordSuggetionApp;
import org.junit.Test;


public class FXEngineWordSearchTest extends AbstractTestExecution {

	@Test
	public void verify() throws Exception {
		show(WordSuggetionApp.class);
        lookup(".text-field").queryAll().forEach(t -> {
            clickOn(t);
            write("new york ");
        });
	}

}
