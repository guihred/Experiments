/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gaming.ex04;

import static gaming.ex04.TronModel.MAP_SIZE;

import javafx.beans.NamedArg;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.layout.Region;

/**
 *
 * @author Note
 */
public class TronSquare extends Region {

    private int i;
    private int j;

    private final ObjectProperty<TronState> state = new SimpleObjectProperty<>(TronState.NONE);

    public TronSquare() {
    }

    public TronSquare(@NamedArg("i") int i, @NamedArg("j") int j) {
        this.i = i;
        this.j = j;
        setPrefSize(10, 10);
        styleProperty().bind(Bindings.when(state.isEqualTo(TronState.FOOD)).then("-fx-background-color:black;")
            .otherwise(Bindings.when(state.isEqualTo(TronState.SNAKE)).then("-fx-background-color:green;")
                .otherwise("-fx-background-color:gray;")));

    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !getClass().isInstance(obj)) {
            return false;
        }
        return ((TronSquare) obj).i == i && ((TronSquare) obj).j == j;

    }

    public int getI() {
        return i;
    }

    public int getJ() {
        return j;
    }

    public TronState getState() {
        return state.get();
    }

    @Override
    public int hashCode() {
        return getI() * MAP_SIZE + getJ();
    }

    public void setI(int i) {
        this.i = i;
    }

    public void setJ(int j) {
        this.j = j;
    }

    public void setState(TronState value) {
        state.set(value);
    }

}
