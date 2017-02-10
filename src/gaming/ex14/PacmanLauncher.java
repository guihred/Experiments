package gaming.ex14;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class PacmanLauncher extends Application {

	@Override
	public void start(Stage stage) throws Exception {
		final Pane group = new Pane();
		final BorderPane borderPane = new BorderPane(group);
		borderPane.setStyle("-fx-background-color:black;");
		final Scene scene = new Scene(borderPane);
		PacmanModel.create(group, scene);
		stage.setScene(scene);
		stage.setWidth(600);
		stage.setHeight(500);
		stage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}
}
