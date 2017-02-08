/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fxproexercises.ch06;

import java.util.concurrent.atomic.AtomicBoolean;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
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

class TaskProgressView {
	public Button cancelButton;
	public Label exception;
	public Button exceptionButton;
	public Label message;
	public Label progress;
	public ProgressBar progressBar;
	public Label running;
	public Scene scene;
	public Button startButton;
	public Label state;
	public Label title;
	public Label totalWork;
	public Label value;
	public Label workDone;

	TaskProgressView() {
		progressBar = new ProgressBar();
		progressBar.setMinWidth(250);
		title = new Label();
		message = new Label();
		running = new Label();
		state = new Label();
		totalWork = new Label();
		workDone = new Label();
		progress = new Label();
		value = new Label();
		exception = new Label();
		startButton = new Button("Start");
		cancelButton = new Button("Cancel");
		exceptionButton = new Button("Exception");

		progressBar.progressProperty().bind(TaskProgressApp.worker.progressProperty());
		title.textProperty().bind(TaskProgressApp.worker.titleProperty());
		message.textProperty().bind(TaskProgressApp.worker.messageProperty());
		running.textProperty().bind(Bindings.format("%s", TaskProgressApp.worker.runningProperty()));
		final ReadOnlyObjectProperty<Worker.State> stateProperty = TaskProgressApp.worker.stateProperty();
		state.textProperty().bind(Bindings.format("%s", stateProperty));
		totalWork.textProperty().bind(TaskProgressApp.worker.totalWorkProperty().asString());
		workDone.textProperty().bind(TaskProgressApp.worker.workDoneProperty().asString());
		progress.textProperty()
				.bind(Bindings.format("%5.2f%%", TaskProgressApp.worker.progressProperty().multiply(100)));
		value.textProperty().bind(TaskProgressApp.worker.valueProperty());

		exception.textProperty().bind(Bindings.createStringBinding(() -> {
			final Throwable workerException = TaskProgressApp.worker.getException();
			if (workerException == null) {
				return "";
			}
			return workerException.getMessage();
		}, TaskProgressApp.worker.exceptionProperty()));
		startButton.disableProperty().bind(stateProperty.isNotEqualTo(Worker.State.READY));
		cancelButton.disableProperty().bind(stateProperty.isNotEqualTo(Worker.State.RUNNING));
		exceptionButton.disableProperty().bind(stateProperty.isNotEqualTo(Worker.State.RUNNING));

		final HBox topPane = new HBox(10, progressBar);
		topPane.setPadding(new Insets(10, 10, 10, 10));
		topPane.setAlignment(Pos.CENTER);

		final ColumnConstraints rightColumn = new ColumnConstraints();
		rightColumn.setHalignment(HPos.RIGHT);
		rightColumn.setMinWidth(65);

		final ColumnConstraints leftColumn = new ColumnConstraints();
		leftColumn.setHalignment(HPos.LEFT);
		leftColumn.setMinWidth(200);

		final GridPane centerPane = new GridPane();
		centerPane.setHgap(10);
		centerPane.setVgap(10);
		centerPane.setPadding(new Insets(10, 10, 10, 10));
		centerPane.getColumnConstraints().addAll(rightColumn, leftColumn);

		centerPane.add(new Label("Title:"), 0, 0);
		centerPane.add(new Label("Message:"), 0, 1);
		centerPane.add(new Label("Running:"), 0, 2);
		centerPane.add(new Label("State:"), 0, 3);
		centerPane.add(new Label("Total Work:"), 0, 4);
		centerPane.add(new Label("Work Done:"), 0, 5);
		centerPane.add(new Label("Progress:"), 0, 6);
		centerPane.add(new Label("Value:"), 0, 7);
		centerPane.add(new Label("Exception:"), 0, 8);
		centerPane.add(title, 1, 0);
		centerPane.add(message, 1, 1);
		centerPane.add(running, 1, 2);
		centerPane.add(state, 1, 3);
		centerPane.add(totalWork, 1, 4);
		centerPane.add(workDone, 1, 5);
		centerPane.add(progress, 1, 6);
		centerPane.add(value, 1, 7);
		centerPane.add(exception, 1, 8);
		final HBox buttonPane = new HBox(startButton, cancelButton, exceptionButton);
		buttonPane.setPadding(new Insets(10, 10, 10, 10));
		buttonPane.setSpacing(10);
		buttonPane.setAlignment(Pos.CENTER);
		scene = new Scene(new BorderPane(centerPane, topPane, null, buttonPane, null));
	}
}