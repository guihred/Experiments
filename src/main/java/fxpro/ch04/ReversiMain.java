/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fxpro.ch04;

import static utils.CommonsFX.newButton;

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.InnerShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import simplebuilder.SimpleRegionBuilder;
import simplebuilder.SimpleTextBuilder;
import simplebuilder.SimpleVBoxBuilder;
import utils.ClassReflectionUtils;

/**
 *
 * @author Note
 */
public class ReversiMain extends Application {

    private static final int HEIGHT = 600;
    private static final int WIDTH = 400;

    private ReversiModel model = ReversiModel.getInstance();

    @Override
    public void start(Stage primaryStage) throws Exception {
        
        Node game = new BorderPane(new StackPane(createBackground(), tiles()), createTitle(), null, createScoreBoxes(), null);
        Node restart = restart();
        Scene scene = new Scene(new AnchorPane(game, restart));
        primaryStage.setScene(scene);

        primaryStage.setWidth(WIDTH);
        primaryStage.setHeight(HEIGHT);
		AnchorPane.setTopAnchor(game, 0D);
		AnchorPane.setBottomAnchor(game, 0D);
		AnchorPane.setLeftAnchor(game, 0D);
		AnchorPane.setRightAnchor(game, 0D);
		AnchorPane.setRightAnchor(restart, 10D);
		AnchorPane.setTopAnchor(restart, 10D);
        ClassReflectionUtils.displayCSSStyler(scene, "reversi.css");
        primaryStage.show();
    }

    private StackPane createScore(Owner owner) {
        final int radiusX = 32;
        Ellipse piece = new Ellipse(radiusX, 20);
		piece.setEffect(new DropShadow(10, Color.DODGERBLUE));
		piece.setFill(owner.getColor());

        Text score = new SimpleTextBuilder().fill(owner.getColor()).build();
        Text remaining = new SimpleTextBuilder().fill(owner.getColor()).build();

        Region background = new Region();
        background.setStyle("-fx-background-color: " + owner.opposite().getColorStyle());
		Node[] children = { piece, remaining };

        FlowPane flowPane = new FlowPane(20, 10, score,
                new SimpleVBoxBuilder(10, children).alignment(Pos.CENTER).build());
		StackPane stack = new StackPane(background, flowPane);
        final int prefHeight = 40;
        stack.setPrefHeight(prefHeight);
        InnerShadow innerShadow = new InnerShadow(20, Color.DODGERBLUE);
        background.effectProperty().bind(Bindings.when(model.getTurn().isEqualTo(owner))
                .then(innerShadow)
                .otherwise((InnerShadow) null));
        DropShadow dropShadow = new DropShadow(10, Color.DODGERBLUE);
        piece.effectProperty().bind(Bindings.when(model.getTurn().isEqualTo(owner))
                .then(dropShadow)
                .otherwise((DropShadow) null));
        score.textProperty().bind(model.getScore(owner).asString());
        remaining.textProperty().bind(model.getTurnsRemaining(owner).asString().concat(" turns remaining"));
        return stack;
    }

    private Node createScoreBoxes() {
		TilePane tiles = new TilePane(createScore(Owner.BLACK), createScore(Owner.WHITE));
		tiles.setSnapToPixel(false);
		tiles.setPrefColumns(2);
        tiles.prefTileWidthProperty().bind(Bindings.selectDouble(tiles.parentProperty(),
                "width").divide(2));
        tiles.getStyleClass().add("scores");
        return tiles;
    }

    private Node createTitle() {
        StackPane left = new StackPane();
        Text text = new Text("JavaFX");
        StackPane.setAlignment(text, Pos.CENTER_RIGHT);
        left.getChildren().add(text);
        Text right = new Text("Reversi");
        TilePane tiles = new TilePane();
        tiles.setSnapToPixel(false);
        TilePane.setAlignment(right, Pos.CENTER_LEFT);
        tiles.getChildren().addAll(left, right);
        tiles.getStyleClass().add("title");
        final int prefHeight = 40;
        tiles.setPrefTileHeight(prefHeight);
        tiles.prefTileWidthProperty().bind(Bindings.selectDouble(tiles.parentProperty(), "width").divide(2));

        return tiles;
    }
    private Node restart() {
		return newButton("Restart", (ActionEvent t) -> model.restart());
    }
    private Node tiles() {
        GridPane board = new GridPane();
        for (int i = 0; i < ReversiModel.BOARD_SIZE; i++) {
            for (int j = 0; j < ReversiModel.BOARD_SIZE; j++) {
                ReversiSquare square = new ReversiSquare(i, j);
                ReversiPiece piece = new ReversiPiece();
                piece.ownerProperty().bind(model.board[i][j]);
                board.add(new StackPane(square, piece), i, j);
            }
        }
        return board;
    }

  
    public static void main(String[] args) {
        launch(args);
    }

    private static Region createBackground() {
        return SimpleRegionBuilder.create()
                .style("-fx-background-color: radial-gradient(radius 100%, white, gray);")
                .build();
    }

}
