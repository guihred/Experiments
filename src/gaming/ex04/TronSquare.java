/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gaming.ex04;

import static gaming.ex01.SnakeModel.MAP_SIZE;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.layout.Region;

/**
 *
 * @author Note
 */
public class TronSquare extends Region {

    final int i, j;

    ObjectProperty<SnakeState> state = new SimpleObjectProperty<>(SnakeState.NONE);

    public TronSquare(int i, int j) {
        setPrefSize(10, 10);
        styleProperty().bind(
                Bindings.when(state.isEqualTo(SnakeState.FOOD)).then("-fx-background-color:black;").otherwise(
                        Bindings.when(state.isEqualTo(SnakeState.SNAKE))
                        .then("-fx-background-color:green;")
                        .otherwise("-fx-background-color:gray;"
                        )));

        this.i = i;
        this.j = j;
    }

    

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj.getClass() != TronSquare.class) {
            return false;
        }
        if (((TronSquare) obj).i == i && ((TronSquare) obj).j == j) {
            return true;
        }
        return false;

    }

    @Override
    public int hashCode() {
        return i * MAP_SIZE + j;
    }

}
