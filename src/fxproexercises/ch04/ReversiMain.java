/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fxproexercises.ch04;

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ButtonBuilder;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.DropShadowBuilder;
import javafx.scene.effect.InnerShadow;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPaneBuilder;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.StackPaneBuilder;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.TilePaneBuilder;
import javafx.scene.layout.VBoxBuilder;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.EllipseBuilder;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextBuilder;
import javafx.stage.Stage;

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
        return ButtonBuilder.create().text("Restart").onAction((ActionEvent t) -> {
            model.restart();
        }).build();
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
        TilePane tiles = TilePaneBuilder.create()
                .snapToPixel(false)
                .prefColumns(2)
                .children(
                        createScore(Owner.BLACK),
                        createScore(Owner.WHITE))
                .build();
        tiles.prefTileWidthProperty().bind(Bindings.selectDouble(tiles.parentProperty(),
                "width").divide(2));
        return tiles;
    }

    private StackPane createScore(Owner owner) {
        Region background;
        Ellipse piece;
        Text score;
        Text remaining;

        background = new Region();
        background.setStyle(("-fx-background-color: " + owner.opposite().getColorStyle()));

        StackPane stack = StackPaneBuilder.create()
                .prefHeight(40)
                .children(
                        background,
                        FlowPaneBuilder.create()
                        .hgap(20)
                        .vgap(10)
                        .alignment(Pos.CENTER)
                        .children(
                                score = TextBuilder.create()
                                .font(Font.font(null, FontWeight.BOLD, 100))
                                .fill(owner.getColor())
                                .build(),
                                VBoxBuilder.create()
                                .alignment(Pos.CENTER)
                                .spacing(10)
                                .children(
                                        piece = EllipseBuilder.create()
                                        .effect(DropShadowBuilder.create().color(Color.DODGERBLUE).spread(0.2).build())
                                        .radiusX(32)
                                        .radiusY(20)
                                        .fill(owner.getColor())
                                        .build(),
                                        remaining = TextBuilder.create()
                                        .font(Font.font(null, FontWeight.BOLD, 12))
                                        .fill(owner.getColor())
                                        .build()
                                )
                                .build()
                        )
                        .build()
                )
                .build();
        InnerShadow innerShadow = new InnerShadow(20, Color.DODGERBLUE);
        background.effectProperty().bind(Bindings.when(model.turn.isEqualTo(owner))
                .then(innerShadow)
                .otherwise((InnerShadow) null));
        DropShadow dropShadow = new DropShadow(10, Color.DODGERBLUE);
        piece.effectProperty().bind(Bindings.when(model.turn.isEqualTo(owner))
                .then(dropShadow)
                .otherwise((DropShadow) null));
        score.textProperty().bind(model.getScore(owner).asString());
        remaining.textProperty().bind(model.getTurnsRemaining(owner).asString().concat(" turns remaining"));
        return stack;
    }

}
