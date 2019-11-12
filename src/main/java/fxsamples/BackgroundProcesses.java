package fxsamples;

import static utils.RunnableEx.runIf;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class BackgroundProcesses extends Application {
    private Task<Boolean> copyWorker;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Background Processes");
        // ... Layout and UI controls code here
        VBox root = new VBox(10);
        root.setAlignment(Pos.CENTER);
        final Label label = new Label("Files Transfer:");
        final ProgressBar progressBar = new ProgressBar(0);
        final ProgressIndicator progressIndicator = new ProgressIndicator(0);
        // ... Layout and UI controls code here
        final Button startButton = new Button("Start");
        final Button cancelButton = new Button("Cancel");
        final TextArea textArea = new TextArea();
        // ... Layout and UI controls code here
        // wire up Start button
        textArea.setPrefHeight(100);
        startButton.setOnAction(event -> {
            startButton.setDisable(true);
            progressBar.setProgress(0);
            progressIndicator.setProgress(0);
            textArea.setText("");
            cancelButton.setDisable(false);
            copyWorker = createWorker(10);
            // wire up progress bar
            progressBar.progressProperty().unbind();
            progressBar.progressProperty().bind(copyWorker.progressProperty());
            progressIndicator.progressProperty().unbind();
            progressIndicator.progressProperty().bind(copyWorker.progressProperty());
            // append to text area box
            copyWorker.messageProperty()
                .addListener((observable, oldValue, newValue) -> textArea.appendText(newValue + "\n"));
            new Thread(copyWorker).start();
        });
        // Cancel button will kill worker and reset.
        cancelButton.setOnAction((ActionEvent event) -> {
            startButton.setDisable(false);
            cancelButton.setDisable(true);
            runIf(copyWorker, c -> c.cancel(true));
            // reset
            progressBar.progressProperty().unbind();
            progressBar.setProgress(0);
            progressIndicator.progressProperty().unbind();
            progressIndicator.setProgress(0);
            textArea.appendText("File transfer was cancelled.");
        });

        root.getChildren().addAll(label, progressBar, progressIndicator, startButton, cancelButton, textArea);
        Scene scene = new Scene(root, Color.WHITE);
        primaryStage.setScene(scene);
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }

    private static Task<Boolean> createWorker(final int numFiles) {
        return new SimpleCopyTask(numFiles);
    }
}
