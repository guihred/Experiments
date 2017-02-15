/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fxproexercises.ch06;

import java.util.concurrent.atomic.AtomicBoolean;
import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.stage.Stage;

public class TaskProgressApp extends Application {
	public static final AtomicBoolean shouldThrow = new AtomicBoolean(false);

	public static final Worker<String> worker = new Task<String>() {
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
	};

	private TaskProgressView view;

	public TaskProgressApp() {
	}

	private void hookupEvents() {
		view.startButton.setOnAction((ActionEvent actionEvent) -> new Thread((Runnable) worker).start());
		view.cancelButton.setOnAction((ActionEvent actionEvent) -> worker.cancel());
		view.exceptionButton.setOnAction((ActionEvent actionEvent) -> shouldThrow.getAndSet(true));
	}

	@Override
	public void start(Stage stage) throws Exception {
		view = new TaskProgressView();
		hookupEvents();
		stage.setTitle("Worker and Task Example");
		stage.setScene(view.scene);
		stage.show();
	}

	public static void main(String[] args) {
		Application.launch(args);
	}
}