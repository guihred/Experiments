/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fxpro.ch04;

import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class ReversiPieceExample extends Application {

	@Override
	public void start(Stage primaryStage) {
		Node white = new StackPane(new ReversiSquare(), new ReversiPiece(Owner.WHITE));
		Node black = new StackPane(new ReversiSquare(), new ReversiPiece(Owner.BLACK));
		primaryStage.setScene(new Scene(new HBox(white, black)));
		HBox.setHgrow(white, Priority.ALWAYS);
		HBox.setHgrow(black, Priority.ALWAYS);
		primaryStage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}
}
