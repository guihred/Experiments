/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fxproexercises.ch03;

import org.slf4j.Logger;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.effect.Light;
import javafx.scene.effect.Lighting;
import javafx.scene.paint.Color;
import simplebuilder.HasLogging;

public class JavaFXBeanController {

    private JavaFXBeanModelExample model;
    public JavaFXBeanController(JavaFXBeanModelExample model) {
        this.model = model;
    }

    public void incrementIPropertyOnModel() {
        model.setI(model.getI() + 1);
    }

    public void changeStrPropertyOnModel() {
        final String str = model.getStr();
		if ("Hello".equals(str)) {
            model.setStr("World");
        } else {
            model.setStr("Hello");
        }
    }
    public void switchColorPropertyOnModel() {
        final Color color = model.getColor();
        if (color.equals(Color.BLACK)) {
            model.setColor(Color.WHITE);
        } else {
            model.setColor(Color.BLACK);
        }
    }

    public static void main(String[] args) {

        Logger log = HasLogging.log();

        ObjectProperty<Lighting> root = new SimpleObjectProperty<>();
        final ObjectBinding<Color> selectBinding = Bindings.select(root, "light", "color");
        selectBinding.addListener((observableValue, oldValue, newValue) -> log
                .info("The color changed:\n\t\told color = {},\n\t\tnew color = {}", oldValue, newValue));
        log.info("firstLight is black.");
        Light firstLight = new Light.Point();
        firstLight.setColor(Color.BLACK);
        log.info("secondLight is white.");
        Light secondLight = new Light.Point();
        secondLight.setColor(Color.WHITE);
        log.info("firstLighting has firstLight.");
        Lighting firstLighting = new Lighting();
        firstLighting.setLight(firstLight);
        log.info("secondLighting has secondLight.");
        Lighting secondLighting = new Lighting();
        secondLighting.setLight(secondLight);
        log.info("Making root observe firstLighting.");
        root.set(firstLighting);
        log.info("Making root observe secondLighting.");
        root.set(secondLighting);
        log.info("Changing secondLighting's light to firstLight");
        secondLighting.setLight(firstLight);
        log.info("Changing firstLight's color to red");
        firstLight.setColor(Color.RED);
    }
}
