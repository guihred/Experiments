/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gaming.ex01;

import static gaming.ex01.SnakeModel.MAP_SIZE;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.layout.Region;

/**
 *
 * @author Note
 */
public class SnakeSquare extends Region {

	private final int i, j;

	private final ObjectProperty<SnakeState> state = new SimpleObjectProperty<>(SnakeState.NONE);

    public SnakeSquare(int i, int j) {
        setPrefSize(50, 50);
        styleProperty().bind(
				Bindings.when(stateProperty().isEqualTo(SnakeState.FOOD)).then("-fx-background-color:black;")
						.otherwise(Bindings.when(stateProperty().isEqualTo(SnakeState.SNAKE))
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
        if (obj.getClass() != SnakeSquare.class) {
            return false;
        }
        if (((SnakeSquare) obj).i == i && ((SnakeSquare) obj).getJ() == getJ()) {
            return true;
        }
        return false;

    }

    @Override
    public int hashCode() {
        return i * MAP_SIZE + getJ();
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
	public SnakeState getState() {
		return state.get();
	}

	public void setState(SnakeState state) {
		this.state.set(state);
	}

}
