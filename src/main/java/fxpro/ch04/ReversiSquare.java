/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fxpro.ch04;

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
    private static final int PREFWIDTH = 200;
    private static ReversiModel model = ReversiModel.getInstance();
    private static final int AZIMUTH = -135;
	private Region highlight = SimpleRegionBuilder.create()
            .opacity(0)
            .style("-fx-border-width: 3; -fx-border-color: dodgerblue")
            .build();
	private FadeTransition highlightTransition = new SimpleFadeTransitionBuilder()
            .node(highlight)
            .duration(Duration.millis(PREFWIDTH))
            .fromValue(0)
            .toValue(1)
            .build();
    public ReversiSquare() {
        setStyle("-fx-background-color: burlywood");
        Light.Distant light = new Light.Distant();
        light.setAzimuth(AZIMUTH);
        light.setElevation(30);
        setEffect(new Lighting(light));
        setPrefSize(PREFWIDTH, PREFWIDTH);
    }

    public ReversiSquare(final int x, final int y) {
        styleProperty().bind(Bindings.when(model.legalMove(x, y))
                .then("-fx-background-color: derive(dodgerblue, -60%)")
                .otherwise("-fx-background-color: burlywood"));
        Light.Distant light = new Light.Distant();
        light.setAzimuth(AZIMUTH);
        light.setElevation(30);
        setEffect(new Lighting(light));
        setPrefSize(PREFWIDTH, PREFWIDTH);
        getChildren().add(highlight);

        addEventHandler(MouseEvent.MOUSE_ENTERED_TARGET, t -> {
            if (model.legalMove(x, y).get()) {
                highlightTransition.setRate(1);
                highlightTransition.play();
            }
        });
        addEventHandler(MouseEvent.MOUSE_EXITED_TARGET, t -> {
            highlightTransition.setRate(-1);
            highlightTransition.play();
        });
        addEventHandler(MouseEvent.MOUSE_CLICKED, t -> {
            model.play(x, y);
            highlightTransition.setRate(1);
            highlightTransition.play();
        });
    }

    @Override
	public void layoutChildren() {
        layoutInArea(highlight, 0, 0, getWidth(), getHeight(), getBaselineOffset(), HPos.CENTER,
                VPos.CENTER);
    }


}
