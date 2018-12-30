package fxsamples;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.MenuBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import simplebuilder.SimpleMenuBarBuilder;

public class TheMenuGrid extends Application {
	@Override
	public void start(Stage primaryStage) {
		primaryStage.setTitle("Menus Example");
		BorderPane root = new BorderPane();
        Scene scene = new Scene(root, 500, 500, Color.WHITE);
        MenuBar menuBar = new SimpleMenuBarBuilder()
                .addMenu("File")
                .addMenuItem("New")
                .addMenuItem("Save")
                .addSeparator()
                .addMenuItem("Exit", e -> Platform.exit())
                .addMenu("Cameras")
                .addCheckMenuItem("Show Camera 1")
                .addCheckMenuItem("Show Camera 2")
                .addMenu("Alarm")
                .addRadioMenuItem("Sound Alarm")
                .addRadioMenuItem("Alarm Off")
                .addSeparator()
                .addSubMenu("Contingent Plans")
                .addCheckMenuItem("Self Destruct in T minus 50")
                .addCheckMenuItem("Turn off the coffee machine ")
                .addCheckMenuItem("Run for your lives! ")
                .build();
		// File menu - new, save, exit
		// Cameras menu - camera 1, camera 2
		// Alarm menu
		// sound or turn alarm off
        root.setTop(menuBar);
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}
}
