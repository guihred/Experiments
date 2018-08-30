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

    private final int i;
    private final int j;

	private final ObjectProperty<SnakeState> state = new SimpleObjectProperty<>(SnakeState.NONE);

    public TronSquare(int i, int j) {
		this.i = i;
		this.j = j;
        setPrefSize(10, 10);
        styleProperty().bind(
                Bindings.when(state.isEqualTo(SnakeState.FOOD)).then("-fx-background-color:black;").otherwise(
                        Bindings.when(state.isEqualTo(SnakeState.SNAKE))
                        .then("-fx-background-color:green;")
                        .otherwise("-fx-background-color:gray;"
                        )));

    }

    

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !getClass().isInstance(obj)) {
            return false;
        }
        return ((TronSquare) obj).i == i && ((TronSquare) obj).j == j;

    }

    @Override
    public int hashCode() {
        return getI() * MAP_SIZE + getJ();
    }



	public int getI() {
		return i;
	}



	public int getJ() {
		return j;
	}



	public void setState(SnakeState value) {
		state.set(value);
	}

	public SnakeState getState() {
		return state.get();
	}

}
