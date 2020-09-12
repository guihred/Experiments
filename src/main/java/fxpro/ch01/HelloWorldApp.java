/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fxpro.ch01;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import utils.ex.HasLogging;

/**
 *
 * @author Note
 */
public class HelloWorldApp extends Application {
    private static final Logger LOGGER = HasLogging.log();
    
    @Override
    public void start(Stage primaryStage) {
        Button btn = new Button();
        btn.setText("Say 'Hello World'");
		btn.setOnAction(e -> LOGGER.info("Hello World!"));
        StackPane root = new StackPane();
        root.getChildren().add(btn);

        Scene scene = new Scene(root, 100, 100);
        
        primaryStage.setTitle("Hello World!");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}
