package gaming.ex23;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.junit.Assert;
import utils.CommonsFX;

public class TicTacToeLauncher extends Application {

    @FXML
    private GridPane gridPane;
    private int currentPlayer;
    private List<TicTacToePlayer> players = Arrays.asList(TicTacToePlayer.O, TicTacToePlayer.X);
    private List<TicTacToeSquare> squares;

    public void initialize() {
        squares = gridPane.getChildren().stream().map(TicTacToeSquare.class::cast).collect(Collectors.toList());
        gridPane.setBackground(new Background(new BackgroundFill(Color.BLUE, CornerRadii.EMPTY, Insets.EMPTY)));
    }

    public void onMouseClickedTicTacToeSquare0(MouseEvent e) {
        TicTacToeSquare square = (TicTacToeSquare) e.getTarget();
        TicTacToePlayer player = players.get(currentPlayer % players.size());
        if (
//            player == TicTacToePlayer.O && 
        square.getState() == TicTacToePlayer.NONE) {
            square.setState(player);
            currentPlayer++;
            boolean verifyWin = TicTacToeHelper.verifyWin(squares, gridPane);
            if (!verifyWin
//                && player == TicTacToePlayer.O
            ) {
                new Thread(this::runAI).start();
            }
        }
    }

    @Override
    public void start(Stage stage) {
        CommonsFX.loadFXML("Tic-Tac-Toe", "TicTacToeLauncher.fxml", this, stage);
    }

    private void runAI() {
        TicTacToePlayer x = players.get(currentPlayer % players.size());
        List<TicTacToePlayer> states = squares.stream().map(TicTacToeSquare::getState)
            .collect(Collectors.toList());
        TicTacToeTree ticTacToeTree = new TicTacToeTree(states);
        TicTacToeTree makeDecision = ticTacToeTree.makeDecision(x);
        currentPlayer++;
        int action = makeDecision.getAction();
        Assert.assertTrue(action < squares.size());
        Assert.assertTrue(squares.get(action).getState() == TicTacToePlayer.NONE);
        Platform.runLater(() -> {
            squares.get(action).setState(x);
        });
    }

    public static void main(String[] args) {
        launch(args);
    }

}