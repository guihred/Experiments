/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gaming.ex02;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;

/**
 *
 * @author Note
 */
public class MemorySquare extends Region {
    final int i, j;
    Color color;
    ObjectProperty<MemoryImage> memoryImage = new SimpleObjectProperty<>();
    ObjectProperty<State> state = new SimpleObjectProperty<>(State.HIDDEN);
    Shape shape;

    public MemorySquare(int i, int j) {
        this.i = i;
        this.j = j;
        setPadding(new Insets(10));
        setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, new Insets(1))));
        setPrefSize(50, 50);

    }

    Shape getFinalShape() {
        if (shape == null) {
            shape = memoryImage.get().getShape();
            shape.setFill(Color.WHITE);
            shape.fillProperty().bind(Bindings.when(state.isEqualTo(State.HIDDEN)).then(Color.WHITE).otherwise(color));

        }

        return shape;
    }

    public enum State {
        HIDDEN,
        SHOWN,
        FOUND
    }

}
