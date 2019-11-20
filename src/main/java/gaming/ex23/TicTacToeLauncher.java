package gaming.ex23;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventTarget;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import utils.CommonsFX;
import utils.HasLogging;

public class TicTacToeLauncher extends Application {

    @FXML
    private GridPane gridPane;
    private int currentPlayer;
    private List<TicTacToePlayer> players = Arrays.asList(TicTacToePlayer.O, TicTacToePlayer.X);
    private List<TicTacToeSquare> squares;
    private boolean locked;

    public void initialize() {
        squares = gridPane.getChildren().stream().map(TicTacToeSquare.class::cast).collect(Collectors.toList());
        gridPane.setBackground(new Background(new BackgroundFill(Color.BLUE, CornerRadii.EMPTY, Insets.EMPTY)));
    }

    public void onMouseClickedTicTacToeSquare0(MouseEvent e) {
        EventTarget target = e.getTarget();
        if (!(target instanceof TicTacToeSquare)) {
            return;
        }
        TicTacToeSquare square = (TicTacToeSquare) target;
        TicTacToePlayer player = players.get(currentPlayer % players.size());
        if (!locked && square.getState() == TicTacToePlayer.NONE) {
            square.setState(player);
            currentPlayer++;
            boolean verifyWin = TicTacToeHelper.verifyWin(squares, gridPane);
            if (!verifyWin) {
                Platform.runLater(this::runAI);
            }
        }
    }

    @Override
    public void start(Stage stage) {
        CommonsFX.loadFXML("Tic-Tac-Toe", "TicTacToeLauncher.fxml", this, stage);
    }

    private void runAI() {
        locked = true;
        TicTacToePlayer x = players.get(currentPlayer % players.size());
        List<TicTacToePlayer> states = squares.stream().map(TicTacToeSquare::getState).collect(Collectors.toList());
        TicTacToeTree ticTacToeTree = new TicTacToeTree(states);
        TicTacToeTree makeDecision = ticTacToeTree.makeDecision(x);
        currentPlayer++;
        int action = makeDecision.getAction();
        if (action < squares.size() && squares.get(action).getState() == TicTacToePlayer.NONE) {
            squares.get(action).setState(x);
            TicTacToeHelper.verifyWin(squares, gridPane, () -> Platform.runLater(this::runAI));
        } else {
            HasLogging.log().error("ERROR IN LOGIC");
        }
        locked = false;
    }

    public static void main(String[] args) {
        launch(args);
    }

}