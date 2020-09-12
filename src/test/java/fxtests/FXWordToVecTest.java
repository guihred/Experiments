package fxtests;

import static java.nio.file.Files.deleteIfExists;

import java.io.File;
import java.io.IOException;
import ml.Word2VecExample;
import ml.WordSuggetionApp;
import org.junit.Test;
import utils.ex.ConsumerEx;

public class FXWordToVecTest extends AbstractTestExecution {
    @Test
	public void testWord2Vec() throws IOException {
		File file = Word2VecExample.getPathToSave();
		deleteIfExists(file.toPath());
		measureTime("Word2VecExample.createWord2Vec", Word2VecExample::createWord2Vec);
	}

	@Test
	public void verifyWordSuggetion() {
        showNewStage(WordSuggetionApp.class, () -> {
            lookup(".text-field").queryAll().forEach(ConsumerEx.makeConsumer(t -> {
                clickOn(t);
                write("new york ");
            }));
        });
	}
}
