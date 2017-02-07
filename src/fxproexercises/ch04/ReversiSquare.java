/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fxproexercises.ch04;

import javafx.animation.FadeTransition;
import javafx.beans.binding.Bindings;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.effect.Light;
import javafx.scene.effect.Lighting;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.util.Duration;
import simplebuilder.SimpleFadeTransitionBuilder;
import simplebuilder.SimpleRegionBuilder;

public class ReversiSquare extends Region {
    private static ReversiModel model = ReversiModel.getInstance();
	private Region highlight = SimpleRegionBuilder.create()
            .opacity(0)
            .style("-fx-border-width: 3; -fx-border-color: dodgerblue")
            .build();
	private FadeTransition highlightTransition = new SimpleFadeTransitionBuilder()
            .node(highlight)
            .duration(Duration.millis(200))
            .fromValue(0)
            .toValue(1)
            .build();
    public ReversiSquare() {
        setStyle("-fx-background-color: burlywood");
        Light.Distant light = new Light.Distant();
        light.setAzimuth(-135);
        light.setElevation(30);
        setEffect(new Lighting(light));
        setPrefSize(200, 200);
    }

    public ReversiSquare(final int x, final int y) {
        styleProperty().bind(Bindings.when(model.legalMove(x, y))
                .then("-fx-background-color: derive(dodgerblue, -60%)")
                .otherwise("-fx-background-color: burlywood"));
        Light.Distant light = new Light.Distant();
        light.setAzimuth(-135);
        light.setElevation(30);
        setEffect(new Lighting(light));
        setPrefSize(200, 200);
        getChildren().add(highlight);

        addEventHandler(MouseEvent.MOUSE_ENTERED_TARGET, (MouseEvent t) -> {
            if (model.legalMove(x, y).get()) {
                highlightTransition.setRate(1);
                highlightTransition.play();
            }
        });
        addEventHandler(MouseEvent.MOUSE_EXITED_TARGET, (MouseEvent t) -> {
            highlightTransition.setRate(-1);
            highlightTransition.play();
        });
        addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent t) -> {
            model.play(x, y);
            highlightTransition.setRate(1);
            highlightTransition.play();
        });
    }

    @Override
    protected void layoutChildren() {
        layoutInArea(highlight, 0, 0, getWidth(), getHeight(), getBaselineOffset(), HPos.CENTER,
                VPos.CENTER);
    }


}
