/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fxpro.ch03;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.effect.Light;
import javafx.scene.effect.Lighting;
import javafx.scene.paint.Color;
import org.slf4j.Logger;
import utils.ex.HasLogging;

public class JavaFXBeanController {

	private static final Logger LOG = HasLogging.log();
    private final JavaFXBeanModelExample model;

    private JavaFXBeanController(JavaFXBeanModelExample model) {
        this.model = model;
    }

    private void changeStrPropertyOnModel() {
        String str = model.getStr();
		if ("Hello".equals(str)) {
            model.setStr("World");
        } else {
            model.setStr("Hello");
        }
    }

    private void incrementIPropertyOnModel() {
        model.setI(model.getI() + 1);
    }
    private void switchColorPropertyOnModel() {
        Color color = model.getColor();
        if (Color.BLACK.equals(color)) {
            model.setColor(Color.WHITE);
        } else {
            model.setColor(Color.BLACK);
        }
    }

    public static void main(String[] args) {

        JavaFXBeanController javaFXBeanController = new JavaFXBeanController(new JavaFXBeanModelExample());
        javaFXBeanController.incrementIPropertyOnModel();
        javaFXBeanController.changeStrPropertyOnModel();
        javaFXBeanController.switchColorPropertyOnModel();
        javaFXBeanController.switchColorPropertyOnModel();
        javaFXBeanController.changeStrPropertyOnModel();

        ObjectProperty<Lighting> root = new SimpleObjectProperty<>();
        ObjectBinding<Color> selectBinding = Bindings.select(root, "light", "color");
		selectBinding.addListener((observableValue, oldValue, newValue) -> LOG
            .trace("The color changed:\n\t\told color = {},\n\t\tnew color = {}", oldValue, newValue));
        LOG.trace("firstLight is black.");
        Light firstLight = new Light.Point();
        firstLight.setColor(Color.BLACK);
        LOG.trace("secondLight is white.");
        Light secondLight = new Light.Point();
        secondLight.setColor(Color.WHITE);
        LOG.trace("firstLighting has firstLight.");
        Lighting firstLighting = new Lighting();
        firstLighting.setLight(firstLight);
        LOG.trace("secondLighting has secondLight.");
        Lighting secondLighting = new Lighting();
        secondLighting.setLight(secondLight);
        LOG.trace("Making root observe firstLighting.");
        root.set(firstLighting);
        LOG.trace("Making root observe secondLighting.");
        root.set(secondLighting);
        LOG.trace("Changing secondLighting's light to firstLight");
        secondLighting.setLight(firstLight);
        LOG.trace("Changing firstLight's color to red");
        firstLight.setColor(Color.RED);
    }
}
