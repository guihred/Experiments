/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gaming.ex18;

import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;

/**
 *
 * @author Note
 */
public class Square2048 extends Region {

    private final IntegerProperty number = new SimpleIntegerProperty();


    public Square2048() {
        Text text = new Text();
        text.textProperty().bind(Bindings.when(number.isNotEqualTo(0)).then(number.asString()).otherwise(""));
        text.wrappingWidthProperty().bind(widthProperty());
        text.layoutYProperty().bind(heightProperty().divide(2));
        getChildren().add(text);
        setPrefSize(50, 50);
    }

    public int getNumber() {
        return number.get();
    }

    public boolean isEmpty() {
        return number.get() == 0;
    }

    public void setNumber(int value) {
        number.set(value);
    }

}