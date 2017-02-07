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

	private final int i, j;

	private final ObjectProperty<SnakeState> state = new SimpleObjectProperty<>(SnakeState.NONE);

    public TronSquare(int i, int j) {
		this.i = i;
		this.j = j;
        setPrefSize(10, 10);
        styleProperty().bind(
                Bindings.when(stateProperty().isEqualTo(SnakeState.FOOD)).then("-fx-background-color:black;").otherwise(
                        Bindings.when(stateProperty().isEqualTo(SnakeState.SNAKE))
                        .then("-fx-background-color:green;")
                        .otherwise("-fx-background-color:gray;"
                        )));

    }

    

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj.getClass() != TronSquare.class) {
            return false;
        }
        if (((TronSquare) obj).getI() == getI() && ((TronSquare) obj).getJ() == getJ()) {
            return true;
        }
        return false;

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



	public ObjectProperty<SnakeState> stateProperty() {
		return state;
	}

	public void setState(SnakeState value) {
		state.set(value);
	}

	public SnakeState getState() {
		return state.get();
	}

}
