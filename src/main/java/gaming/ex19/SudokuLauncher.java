/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gaming.ex19;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class SudokuLauncher extends Application {

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

        final BorderPane borderPane = new BorderPane(gridPane);
		gridPane.minWidthProperty().bind(borderPane.widthProperty());
        final Scene scene = new Scene(borderPane);
        stage.setScene(scene);
        stage.setWidth(400);
        stage.setHeight(400);

		scene.setOnKeyPressed(sudokuModel::handleKeyPressed);
		scene.setOnKeyPressed(sudokuModel::handleKeyPressed);
		scene.setOnKeyPressed(sudokuModel::handleKeyPressed);
        stage.show();
    }
    public static void main(String[] args) {
        launch(args);
    }
}
