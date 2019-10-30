package gaming.ex14;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class PacmanLauncher extends Application {

	@Override
	public void start(Stage stage) {
		final Group group = new Group();

		final BorderPane borderPane = new BorderPane(group);
		final Scene scene = new Scene(borderPane);
		Text text = new Text();
		text.setFill(Color.WHITE);
		PacmanModel create = PacmanModel.create(group, scene);
		text.textProperty().bind(create.getPoints().asString());
		borderPane.setTop(text);
		borderPane.setStyle("-fx-background-color:black;");
		stage.setScene(scene);
		stage.setWidth(PacmanBall.MAZE_SIZE * 2 * PacmanBall.SQUARE_SIZE + PacmanBall.SQUARE_SIZE);
		stage.setHeight(PacmanBall.MAZE_SIZE * 2 * PacmanBall.SQUARE_SIZE + PacmanBall.SQUARE_SIZE);
		stage.setResizable(false);
		stage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}
}
