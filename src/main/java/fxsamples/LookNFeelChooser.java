package fxsamples;

import static utils.ResourceFXUtils.toExternalForm;

import java.io.IOException;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.MenuBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import simplebuilder.SimpleMenuBarBuilder;
import utils.ResourceFXUtils;

public class LookNFeelChooser extends Application {

	@Override
	public void init() {
        Font.loadFont(toExternalForm("Roboto-Thin.ttf"), 10).getName();
        Font.loadFont(toExternalForm("Roboto-Light.ttf"), 10).getName();
	}

	@Override
    public void start(Stage primaryStage) throws IOException {
		BorderPane root = new BorderPane();
        Parent content = FXMLLoader.load(ResourceFXUtils.toURL("lnf_demo.fxml"));
        Scene scene = new Scene(root);
		root.setCenter(content);
		// Menu bar
        MenuBar menuBar = new SimpleMenuBarBuilder()
                .addMenu("_File")
                .addMenuItem("Exit", "Ctrl+F4", ae -> Platform.exit())
                .addMenu("_Look 'N' Feel")
                .addMenuItem("Caspian", ae -> {
                            scene.getStylesheets().clear();
                            setUserAgentStylesheet(STYLESHEET_CASPIAN);
                        })
                .addMenuItem("Modena", ae -> {
                            scene.getStylesheets().clear();
                            setUserAgentStylesheet(STYLESHEET_MODENA);
                        })
                .build();

		// Look and feel menu
        root.setTop(menuBar);
		primaryStage.setTitle("Look N Feel Chooser");
		primaryStage.setScene(scene);
        primaryStage.show();

	}

	public static void main(String[] args) {
		launch(args);
	}
}
