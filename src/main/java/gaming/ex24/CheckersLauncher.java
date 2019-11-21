package gaming.ex24;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import utils.CommonsFX;

public class CheckersLauncher extends Application {
	@FXML
    private GridPane gridPane;
    private int currentPlayer;
    private List<CheckersPlayer> players = Arrays.asList(CheckersPlayer.WHITE, CheckersPlayer.BLACK);
    private List<CheckersSquare> squares;

    public void initialize() {
        squares = gridPane.getChildren().stream().map(e -> (CheckersSquare) e).collect(Collectors.toList());
    }

    public void onMouseClickedCheckersSquare0(MouseEvent e0) {
        Object target = e0.getSource();
        if (target instanceof CheckersSquare) {
            boolean onClick = CheckersHelper.onClick(players.get(currentPlayer % players.size()), squares,
                (CheckersSquare) target);
            if (onClick) {
                currentPlayer++;
            }
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        CommonsFX.loadFXML("Checkes", "CheckersLauncher.fxml", this, primaryStage);

    }

    public static void main(String[] args) {
        launch(args);
    }
}
