/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fxproexercises.ch04;

import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.scene.Node;
import javafx.scene.SceneBuilder;
import javafx.scene.layout.HBox;
import javafx.scene.layout.HBoxBuilder;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPaneBuilder;
import javafx.stage.Stage;

public class ReversiPieceTest extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        Node white, black;
        primaryStage.setScene(SceneBuilder.create()
                .root(HBoxBuilder.create()
                        .snapToPixel(false)
                        .children(
                                white = StackPaneBuilder.create()
                                .children(
                                        new ReversiSquare(),
                                        new ReversiPiece(Owner.WHITE)
                                )
                                .build(),
                                black = StackPaneBuilder.create()
                                .children(
                                        new ReversiSquare(),
                                        new ReversiPiece(Owner.BLACK)
                                )
                                .build()
                        )
                        .build())
                .build());
        HBox.setHgrow(white, Priority.ALWAYS);
        HBox.setHgrow(black, Priority.ALWAYS);
        primaryStage.show();
    }
}
