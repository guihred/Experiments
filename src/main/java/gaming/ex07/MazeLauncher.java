package gaming.ex07;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class MazeLauncher extends Application {


    @Override
    public void start(Stage stage) throws Exception {
        final GridPane gridPane = new GridPane();
        gridPane.setBackground(new Background(new BackgroundFill(Color.BLUE, CornerRadii.EMPTY, Insets.EMPTY)));
        final Scene scene = new Scene(gridPane);
		MazeModel.create(gridPane, scene);
        stage.setScene(scene);
        stage.setWidth(MazeSquare.MAZE_SIZE * MazeSquare.SQUARE_SIZE + 30);
        stage.setHeight(MazeSquare.MAZE_SIZE * MazeSquare.SQUARE_SIZE + 60);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
