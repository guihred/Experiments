/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fxpro.ch06;

import java.util.concurrent.atomic.AtomicBoolean;
import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.stage.Stage;
import utils.fx.TaskProgressView;

public class TaskProgressApp extends Application {

	public final AtomicBoolean shouldThrow = new AtomicBoolean(false);

    public final Task<String> worker = new SimpleTask(shouldThrow);
	private TaskProgressView view = new TaskProgressView(worker, shouldThrow);

	@Override
	public void start(Stage stage) {
        stage.setTitle("Simple Task Progress Example");
		stage.setScene(view.getScene());
		stage.show();
	}

	public static void main(String[] args) {
		Application.launch(args);
	}
}