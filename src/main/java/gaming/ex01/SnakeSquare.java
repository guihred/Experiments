/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gaming.ex01;

import static gaming.ex01.SnakeModel.MAP_SIZE;

import javafx.beans.NamedArg;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.layout.Region;

/**
 *
 * @author Note
 */
public class SnakeSquare extends Region {

    private int i;
    private int j;
    private final ObjectProperty<SnakeState> state = new SimpleObjectProperty<>(SnakeState.NONE);

    public SnakeSquare() {
    }
    public SnakeSquare(@NamedArg("i") int i, @NamedArg("j") int j) {
        setPrefSize(10, 10);
        styleProperty().bind(Bindings.when(state.isEqualTo(SnakeState.FOOD)).then("-fx-background-color:black;")
            .otherwise(Bindings.when(state.isEqualTo(SnakeState.SNAKE)).then("-fx-background-color:green;")
                .otherwise("-fx-background-color:gray;")));
        this.i = i;
        this.j = j;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !getClass().isInstance(obj)) {
            return false;
        }
        return ((SnakeSquare) obj).i == i && ((SnakeSquare) obj).j == j;

    }

    public int getI() {
        return i;
    }

    public int getJ() {
        return j;
    }

    public SnakeState getState() {
        return state.get();
    }

    @Override
    public int hashCode() {
        return i * MAP_SIZE + getJ();
    }

    public void setI(int i) {
        this.i = i;
    }

    public void setJ(int j) {
        this.j = j;
    }

    public void setState(SnakeState state) {
        this.state.set(state);
    }

}
