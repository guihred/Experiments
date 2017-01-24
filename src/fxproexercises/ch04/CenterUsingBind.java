/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fxproexercises.ch04;

import static others.CommonsFX.newVBox;

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.DropShadowBuilder;
import javafx.scene.effect.InnerShadow;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.FlowPaneBuilder;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.EllipseBuilder;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import others.SimpleTextBuilder;

public class CenterUsingBind extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        Scene scene;
		TilePane tiles = new TilePane(createScore(Owner.BLACK), createScore(Owner.WHITE));
		primaryStage.setScene(scene = new Scene(tiles, 600, 120));
        tiles.prefTileWidthProperty().bind(scene.widthProperty().divide(2));
        tiles.prefTileHeightProperty().bind(scene.heightProperty());
        primaryStage.show();
    }

    private StackPane createScore(Owner owner) {
        Region background;
		Ellipse piece = EllipseBuilder.create()
				.effect(DropShadowBuilder.create().color(Color.DODGERBLUE).spread(0.2).build()).radiusX(32).radiusY(20)
				.fill(owner.getColor()).build();
		Text score = new SimpleTextBuilder().font(Font.font(null, FontWeight.BOLD, 100)).fill(owner.getColor()).build();
		Text remaining = new SimpleTextBuilder().font(Font.font(null, FontWeight.BOLD, 12)).fill(owner.getColor())
				.build();
        ReversiModel model = ReversiModel.getInstance();

        background = new Region();
        background.setStyle("-fx-background-color: " + owner.opposite().getColorStyle());

		FlowPane flowPane = FlowPaneBuilder.create().hgap(20).vgap(10).alignment(Pos.CENTER)
				.children(score, newVBox(Pos.CENTER, 10, piece, remaining)).build();
		StackPane stack = new StackPane(background, flowPane);
		stack.setPrefHeight(1000);
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
