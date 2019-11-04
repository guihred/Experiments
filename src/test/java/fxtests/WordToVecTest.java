package fxtests;

import static fxtests.FXTesting.measureTime;
import static java.nio.file.Files.deleteIfExists;

import java.io.File;
import java.io.IOException;
import ml.Word2VecExample;
import ml.WordSuggetionApp;
import org.junit.Test;
import utils.ConsumerEx;

public class WordToVecTest extends AbstractTestExecution {
	@Test
	public void testWord2Vec() throws IOException {
		File file = Word2VecExample.getPathToSave();
		deleteIfExists(file.toPath());
		measureTime("Word2VecExample.createWord2Vec", Word2VecExample::createWord2Vec);
	}

	@Test
	public void verifyWordSuggetion() {
		show(WordSuggetionApp.class);
		lookup(".text-field").queryAll().forEach(ConsumerEx.makeConsumer(t -> {
			clickOn(t);
			write("new york ");
		}));
	}
}
