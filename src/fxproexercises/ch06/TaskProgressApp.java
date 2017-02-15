/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fxproexercises.ch06;

import java.util.concurrent.atomic.AtomicBoolean;
import javafx.application.Application;
import javafx.concurrent.Worker;
import javafx.stage.Stage;

public class TaskProgressApp extends Application {

	public final AtomicBoolean shouldThrow = new AtomicBoolean(false);

	public final Worker<String> worker = new SimpleTask(shouldThrow);
	private TaskProgressView view = new TaskProgressView(worker, shouldThrow);

	@Override
	public void start(Stage stage) throws Exception {
		stage.setTitle("Worker and Task Example");
		stage.setScene(view.getScene());
		stage.show();
	}

	public static void main(String[] args) {
		Application.launch(args);
	}
}