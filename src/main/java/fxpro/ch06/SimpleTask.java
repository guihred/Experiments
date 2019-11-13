package fxpro.ch06;

import java.util.concurrent.atomic.AtomicBoolean;
import javafx.concurrent.Task;
import org.assertj.core.api.exception.RuntimeIOException;
import utils.RunnableEx;

public class SimpleTask extends Task<String> {

    private AtomicBoolean shouldThrow;

	public SimpleTask(AtomicBoolean shouldThrow) {
        this.shouldThrow = shouldThrow;
	}

	@Override
	public String call() {
		updateTitle("Example Task");
		updateMessage("Starting...");
		final int total = 250;
		updateProgress(0, total);
		for (int i = 1; i <= total; i++) {
            RunnableEx.ignore(() -> Thread.sleep(20));

			if (shouldThrow.get()) {
                throw new RuntimeIOException("Exception thrown at " + System.currentTimeMillis());
			}
			updateTitle("Example Task (" + i + ")");
			updateMessage("Processed " + i + " of " + total + " items.");
			updateProgress(i, total);
		}
		return "Completed at " + System.currentTimeMillis();
	}
}