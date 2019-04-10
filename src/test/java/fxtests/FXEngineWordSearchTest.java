package fxtests;

import ml.WordSuggetionApp;
import org.junit.Test;
import utils.ConsumerEx;


public class FXEngineWordSearchTest extends AbstractTestExecution {

	@Test
	public void verify() throws Exception {
		show(WordSuggetionApp.class);
        lookup(".text-field").queryAll().forEach(ConsumerEx.makeConsumer(t -> {
            clickOn(t);
            write("new york ");
        }));
	}

}
