/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gaming.ex19;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import simplebuilder.SimpleButtonBuilder;

public class SudokuLauncher extends Application {

    private static final int WIDTH = 400;
    @Override
    public void start(Stage stage) throws Exception {
        final GridPane gridPane = new GridPane();
        gridPane.setBackground(new Background(new BackgroundFill(Color.BLUE, CornerRadii.EMPTY, Insets.EMPTY)));

		final SudokuModel sudokuModel = new SudokuModel();

		for (int i = 0; i < SudokuModel.MAP_N_SQUARED; i++) {
			for (int j = 0; j < SudokuModel.MAP_N_SQUARED; j++) {
				SudokuSquare map1 = sudokuModel.getMapAt(i, j);
				gridPane.add(map1, i, j);
            }
        }

        Region numberBoard = sudokuModel.getNumberBoard();
        final StackPane borderPane = new StackPane(gridPane, numberBoard);
		gridPane.minWidthProperty().bind(borderPane.widthProperty());
        BorderPane root = new BorderPane(borderPane);
        root.setLeft(new VBox(SimpleButtonBuilder.newButton("Blank", e -> sudokuModel.blank()),
                SimpleButtonBuilder.newButton("Reset", e -> sudokuModel.reset()),
                SimpleButtonBuilder.newButton("Solve", e -> sudokuModel.solve())));
        final Scene scene = new Scene(root);
        stage.setResizable(false);
        stage.setScene(scene);
        stage.setWidth(WIDTH);
        stage.setHeight(WIDTH);
        gridPane.prefWidthProperty().bind(scene.widthProperty());
        gridPane.prefHeightProperty().bind(scene.heightProperty());
        gridPane.setOnMousePressed(sudokuModel::handleMousePressed);
        gridPane.setOnMouseDragged(sudokuModel::handleMouseMoved);
        gridPane.setOnMouseReleased(sudokuModel::handleMouseReleased);
        stage.show();
    }
    public static void main(String[] args) {
        launch(args);
    }
}
