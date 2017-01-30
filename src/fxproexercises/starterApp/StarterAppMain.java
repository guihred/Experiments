/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fxproexercises.starterApp;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

public class StarterAppMain extends Application {


    @Override
	public void start(Stage stage) throws Exception {
		final GridPane gridPane = new GridPane();
		gridPane.setBackground(new Background(new BackgroundFill(Color.BLUE, CornerRadii.EMPTY, Insets.EMPTY)));
		MazeModel mazeModel = new MazeModel(gridPane);
		final Scene scene = new Scene(gridPane);
        stage.setScene(scene);
		stage.setWidth(MazeModel.MAZE_SIZE * MazeSquare.SQUARE_SIZE + 30);
		stage.setHeight(MazeModel.MAZE_SIZE * MazeSquare.SQUARE_SIZE + 30);
        stage.show();

		final Circle circle = new Circle(MazeSquare.SQUARE_SIZE / 3, Color.RED);
		mazeModel.maze[0][0].setCenter(circle);
		scene.setOnKeyPressed((KeyEvent event) -> {
			final KeyCode code = event.getCode();
			switch (code) {
			case W:
			case UP:
				circle.setCenterX(circle.getCenterX() + 1);
				break;
			case S:
			case DOWN:
				circle.setCenterX(circle.getCenterX() - 1);
				break;
			case D:
			case RIGHT:
				circle.setCenterY(circle.getCenterY() + 1);
				break;
			case A:
			case LEFT:
				circle.setCenterY(circle.getCenterY() - 1);
				break;
			default:

			}
		});

    }

	public static void main(String[] args) {
		launch(args);
    }
}


