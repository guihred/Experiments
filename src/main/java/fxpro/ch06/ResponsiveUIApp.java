/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fxpro.ch06;

/**
 *
 * @author Note
 */
import javafx.application.Application;
import javafx.stage.Stage;

public class ResponsiveUIApp extends Application {

	private final ResponsiveUIModel model = new ResponsiveUIModel();

	private ResponsiveUIView view = new ResponsiveUIView(model);


    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("Unresponsive UI Example");
        stage.setScene(view.getScene());
        stage.show();
    }

    public static void main(String[] args) {
        Application.launch(args);
    }

}
