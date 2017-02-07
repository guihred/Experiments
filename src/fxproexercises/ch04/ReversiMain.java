/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fxproexercises.ch04;

import static simplebuilder.CommonsFX.newButton;

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
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import simplebuilder.SimpleTextBuilder;
import simplebuilder.SimpleVBoxBuilder;

/**
 *
 * @author Note
 */
public class ReversiMain extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    ReversiModel model = ReversiModel.getInstance();

    @Override
    public void start(Stage primaryStage) throws Exception {
        
        Node game = new BorderPane(new StackPane(createBackground(), tiles()), createTitle(), null, createScoreBoxes(), null);
        Node restart = restart();
        primaryStage.setScene(new Scene(new AnchorPane(game, restart)));

        primaryStage.setWidth(400);
        primaryStage.setHeight(600);
        AnchorPane.setTopAnchor(game, 0d);
        AnchorPane.setBottomAnchor(game, 0d);
        AnchorPane.setLeftAnchor(game, 0d);
        AnchorPane.setRightAnchor(game, 0d);
        AnchorPane.setRightAnchor(restart, 10d);
        AnchorPane.setTopAnchor(restart, 10d);
//        if (Platform.isSupported(ConditionalFeature.SCENE3D)) {
//            primaryStage.getScene().setCamera(PerspectiveCameraBuilder.create()
//                    .fieldOfView(60).build());
//        }
        primaryStage.show();
    }

    private Node restart() {
		return newButton("Restart", (ActionEvent t) -> model.restart());
    }

    private Node createTitle() {
        StackPane left = new StackPane();
        left.setStyle("-fx-background-color: black");

        Text text = new Text("JavaFX");
        text.setFont(Font.font(null, FontWeight.BOLD, 18));
        text.setFill(Color.WHITE);
        StackPane.setAlignment(text, Pos.CENTER_RIGHT);
        left.getChildren().add(text);
        Text right = new Text("Reversi");
        right.setFont(Font.font(null, FontWeight.BOLD, 18));
        TilePane tiles = new TilePane();
        tiles.setSnapToPixel(false);
        TilePane.setAlignment(right, Pos.CENTER_LEFT);
        tiles.getChildren().addAll(left, right);
        tiles.setPrefTileHeight(40);
        tiles.prefTileWidthProperty().bind(Bindings.selectDouble(tiles.parentProperty(),
                "width").divide(2));
        return tiles;
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
//        if (Platform.isSupported(ConditionalFeature.SCENE3D)) {
//            Transform scale = new Scale(.45, .8, 1, 300, 60, 0);
//            Transform translate = new Translate(75, -2, -150);
//            Transform xRot = new Rotate(-40, 300, 150, 0, Rotate.X_AXIS);
//            Transform yRot = new Rotate(-5, 300, 150, 0, Rotate.Y_AXIS);
//            Transform zRot = new Rotate(-6, 300, 150, 0, Rotate.Z_AXIS);
//            board.getTransforms().addAll(scale, translate, xRot, yRot, zRot);
//        }
        return board;
    }
    private Node createBackground() {
        final Region region = new Region();
        region.setStyle("-fx-background-color: radial-gradient(radius 100%, white, gray);");

        return region;
    }

  
    private Node createScoreBoxes() {
		TilePane tiles = new TilePane(createScore(Owner.BLACK), createScore(Owner.WHITE));
		tiles.setSnapToPixel(false);
		tiles.setPrefColumns(2);
        tiles.prefTileWidthProperty().bind(Bindings.selectDouble(tiles.parentProperty(),
                "width").divide(2));
        return tiles;
    }

    private StackPane createScore(Owner owner) {
        Region background;
		Ellipse piece = new Ellipse(32, 20);
		piece.setEffect(new DropShadow(10, Color.DODGERBLUE));
		piece.setFill(owner.getColor());

		Text score = new SimpleTextBuilder().fill(owner.getColor()).font(Font.font(null, FontWeight.BOLD, 100)).build();
		Text remaining = new SimpleTextBuilder().fill(owner.getColor()).font(Font.font(null, FontWeight.BOLD, 12))
				.build();

        background = new Region();
        background.setStyle("-fx-background-color: " + owner.opposite().getColorStyle());
		Node[] children = { piece, remaining };

		FlowPane flowPane = new FlowPane(20, 10, score, new SimpleVBoxBuilder().alignment(Pos.CENTER).spacing(10).children(children).build());
		flowPane.setAlignment(Pos.CENTER);
		StackPane stack = new StackPane(background, flowPane);
		stack.setPrefHeight(40);
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

}
