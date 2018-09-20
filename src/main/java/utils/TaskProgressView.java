package utils;

import java.util.concurrent.atomic.AtomicBoolean;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.concurrent.Worker;
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

public class TaskProgressView {
	private Button cancelButton = new Button("Cancel");
	private Label exception = new Label();
	private Button exceptionButton = new Button("Exception");
	private Label message = new Label();
	private Label progress = new Label();
	private ProgressBar progressBar = new ProgressBar();
	private Label running = new Label();
	private Scene scene;
	private Button startButton = new Button("Start");
	private Label state = new Label();
	private Label title = new Label();
	private Label totalWork = new Label();
	private Label value = new Label();
	private Label workDone = new Label();

    public TaskProgressView(Worker<String> worker) {
        this(worker, new AtomicBoolean(false));
        exceptionButton.setVisible(false);
    }

    public TaskProgressView(Worker<String> worker, AtomicBoolean shouldThrow) {
		progressBar.setMinWidth(250);
		progressBar.progressProperty().bind(worker.progressProperty());
		title.textProperty().bind(worker.titleProperty());
		message.textProperty().bind(worker.messageProperty());
		running.textProperty().bind(Bindings.format("%s", worker.runningProperty()));
		final ReadOnlyObjectProperty<Worker.State> stateProperty = worker.stateProperty();
		state.textProperty().bind(Bindings.format("%s", stateProperty));
		totalWork.textProperty().bind(worker.totalWorkProperty().asString());
		workDone.textProperty().bind(worker.workDoneProperty().asString());
		progress.textProperty().bind(Bindings.format("%5.2f%%", worker.progressProperty().multiply(100)));
		value.textProperty().bind(worker.valueProperty());

		exception.textProperty().bind(Bindings.createStringBinding(() -> {
			final Throwable workerException = worker.getException();
			if (workerException == null) {
				return "";
			}
			return workerException.getMessage();
		}, worker.exceptionProperty()));
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
		hookupEvents(worker, shouldThrow);
	}

	private void hookupEvents(Worker<String> worker, AtomicBoolean shouldThrow) {
		startButton.setOnAction(actionEvent -> new Thread((Runnable) worker).start());
		cancelButton.setOnAction(actionEvent -> worker.cancel());
		exceptionButton.setOnAction(actionEvent -> shouldThrow.getAndSet(true));
	}
	public Scene getScene() {
		return scene;
	}
}