package gaming.ex14;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class PacmanLauncher extends Application {

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage stage) throws Exception {
		final Group group = new Group();

		final BorderPane borderPane = new BorderPane(group);

		borderPane.setStyle("-fx-background-color:black;");
		final Scene scene = new Scene(borderPane);
		PacmanModel.create(group, scene);
		stage.setScene(scene);

		stage.setWidth(PacmanModel.MAZE_SIZE * 2 * PacmanModel.SQUARE_SIZE + PacmanModel.SQUARE_SIZE);
		stage.setHeight(PacmanModel.MAZE_SIZE * 2 * PacmanModel.SQUARE_SIZE + PacmanModel.SQUARE_SIZE);
		stage.setResizable(false);
		stage.show();
	}
}
