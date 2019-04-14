package gaming.ex17;


import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class PuzzleLauncher extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Puzzle");
        Group root = new Group();
        PuzzleModel puzzlePiece = new PuzzleModel();
        root.getChildren().add(puzzlePiece);
        Scene value = new Scene(root);
        primaryStage.setScene(value);
        primaryStage.show();

    }

    public static void main(String[] args) {
        launch(args);
    }
}
