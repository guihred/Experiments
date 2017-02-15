/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fxproexercises.ch06;

import javafx.application.Application;
import javafx.stage.Stage;

public class ThreadInformationApp extends Application {

	private final ThreadInformationModel model = new ThreadInformationModel();
	private ThreadInformationView view = new ThreadInformationView(model);

    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("JavaFX Threads Information");
		stage.setScene(view.getScene());
        stage.setWidth(440);
        stage.setHeight(640);
        stage.show();
    }

    public static void main(String[] args) {
        Application.launch(args);
    }
}
