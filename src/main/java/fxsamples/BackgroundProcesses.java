package fxsamples;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;
import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.apache.poi.util.IOUtils;

public class BackgroundProcesses extends Application {
	private Task<Boolean> copyWorker;

    @Override
	public void start(Stage primaryStage) {
		primaryStage
                .setTitle("Background Processes");
		// ... Layout and UI controls code here
		VBox root = new VBox();
		Scene scene = new Scene(root, 380, 150, Color.WHITE);
		final Label label = new Label("Files Transfer:");
		final ProgressBar progressBar = new ProgressBar(0);
		final ProgressIndicator progressIndicator = new ProgressIndicator(0);
		// ... Layout and UI controls code here
		final Button startButton = new Button("Start");
		final Button cancelButton = new Button("Cancel");
		final TextArea textArea = new TextArea();
		// ... Layout and UI controls code here
		// wire up Start button
		startButton.setOnAction(event -> {
			startButton.setDisable(true);
			progressBar.setProgress(0);
			progressIndicator.setProgress(0);
			textArea.setText("");
			cancelButton.setDisable(false);
			copyWorker = createWorker(10);
			// wire up progress bar
				progressBar.progressProperty().unbind();
				progressBar.progressProperty().bind(
						copyWorker.progressProperty());
				progressIndicator.progressProperty().unbind();
				progressIndicator.progressProperty().bind(
						copyWorker.progressProperty());
				// append to text area box
				copyWorker.messageProperty().addListener(
					(observable, oldValue, newValue) -> textArea.appendText(newValue + "\n"));
				new Thread(copyWorker).start();
			});
		// Cancel button will kill worker and reset.
		cancelButton.setOnAction((ActionEvent event) -> {
			startButton.setDisable(false);
			cancelButton.setDisable(true);
			copyWorker.cancel(true);
			// reset
				progressBar.progressProperty().unbind();
				progressBar.setProgress(0);
				progressIndicator.progressProperty().unbind();
				progressIndicator.setProgress(0);
				textArea.appendText("File transfer was cancelled.");
			});

		root.getChildren().addAll(label, progressBar, progressIndicator,
				startButton, cancelButton, textArea);
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	private Task<Boolean> createWorker(final int numFiles) {
		return new Task<Boolean>() {
			@Override
			public Boolean call() throws Exception {
                for (long i = 0; i < numFiles; i++) {
					long elapsedTime = System.currentTimeMillis();
					copyFile("some file", "some dest file");
					elapsedTime = System.currentTimeMillis() - elapsedTime;
					String status = elapsedTime + " milliseconds";
					// queue up status
					updateMessage(status);
					updateProgress(i + 1, numFiles);
				}
				return true;
			}
		};
	}

	public static void main(String[] args) {
		launch(args);
	}

	private static void copyFile(String src, String dest) throws InterruptedException, IOException {
		// simulate a long time
        IOUtils.copy(new FileInputStream(src), new FileOutputStream(dest));

        Random rnd = new Random(System.currentTimeMillis());
		long millis = rnd.nextInt(1000);
		Thread.sleep(millis);
	}
}
