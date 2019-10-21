package gaming.ex19;

import java.util.stream.Collectors;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import utils.CommonsFX;

public class SudokuLauncher extends Application {
    @FXML
    private GridPane numberBoard;
    @FXML
    private GridPane gridPane0;
    @FXML
    private StackPane borderPane;

    private SudokuModel sudokuModel;

    public void initialize() {
        sudokuModel = new SudokuModel(numberBoard);
        sudokuModel.getNumberOptions().addAll(
            borderPane.lookupAll(".numberButton").stream().map(NumberButton.class::cast).collect(Collectors.toList()));
        sudokuModel.getSudokuSquares().addAll(
            borderPane.lookupAll(".sudokuSquare").stream().map(SudokuSquare.class::cast).collect(Collectors.toList()));
        sudokuModel.reset();
        gridPane0.minWidthProperty().bind(borderPane.widthProperty());
        gridPane0.sceneProperty().addListener(e -> {
            Scene scene = gridPane0.getScene();
            gridPane0.prefWidthProperty().bind(scene.widthProperty());
            gridPane0.prefHeightProperty().bind(scene.heightProperty());
        });
    }

    public void onActionBlank() {
        sudokuModel.blank();
    }

    public void onActionReset() {
        sudokuModel.reset();
    }

    public void onActionSolve() {
        sudokuModel.solve();
    }

    public void onMouseDraggedGridPane0(MouseEvent e) {
        sudokuModel.handleMouseMoved(e);
    }

    public void onMousePressedGridPane0(MouseEvent e) {
        sudokuModel.handleMousePressed(e);
    }

    public void onMouseReleasedGridPane0() {
        sudokuModel.handleMouseReleased();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        final int width = 400;
        CommonsFX.loadFXML("Sudoku", "SudokuLauncher.fxml", this, primaryStage, width, width);
        primaryStage.setResizable(false);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
