package fxproexercises.ch06;

import java.util.concurrent.atomic.AtomicBoolean;
import javafx.concurrent.Task;

class SimpleTask extends Task<String> {

	private AtomicBoolean shouldThrow;

	public SimpleTask(AtomicBoolean shouldThrow) {
		this.shouldThrow = shouldThrow;
	}

	@Override
	public String call() throws Exception {
		updateTitle("Example Task");
		updateMessage("Starting...");
		final int total = 250;
		updateProgress(0, total);
		for (int i = 1; i <= total; i++) {
			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
				return "Cancelled at " + System.currentTimeMillis();
			}
			if (shouldThrow.get()) {
				throw new RuntimeException("Exception thrown at " + System.currentTimeMillis());
			}
			updateTitle("Example Task (" + i + ")");
			updateMessage("Processed " + i + " of " + total + " items.");
			updateProgress(i, total);
		}
		return "Completed at " + System.currentTimeMillis();
	}
}