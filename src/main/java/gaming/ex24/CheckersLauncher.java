package gaming.ex24;

import java.util.Arrays;
import java.util.List;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class CheckersLauncher extends Application {

    private static final int SIZE = 8;
    private GridPane gridPane;
    private int currentPlayer;
    private List<CheckersPlayer> players = Arrays.asList(CheckersPlayer.WHITE, CheckersPlayer.BLACK);
    @Override
    public void start(Stage stage) {
        gridPane = new GridPane();
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                boolean black = (i + j) % 2 == 0;
                CheckersSquare child = new CheckersSquare(black);
                if (black) {
                    if (i < 3) {
                        child.setState(CheckersPlayer.BLACK);
                    }
                    if (i > SIZE - 4) {
                        child.setState(CheckersPlayer.WHITE);
                    }
                }
                gridPane.add(child, j, i);
            }
        }

        Scene scene = new Scene(gridPane);
        stage.setTitle("Checkers");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}