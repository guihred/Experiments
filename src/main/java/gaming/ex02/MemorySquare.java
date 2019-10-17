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
	private Color color;
	private final ObjectProperty<MemoryImage> memoryImage = new SimpleObjectProperty<>();
	private final ObjectProperty<State> state = new SimpleObjectProperty<>(State.HIDDEN);
	private Shape shape;

	public MemorySquare() {
        setPadding(new Insets(10));
        setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, new Insets(1))));
        setPrefSize(50, 50);

    }

    public Color getColor() {
		return color;
	}

	public Shape getFinalShape() {
        if (shape == null && memoryImage.get() != null) {
            shape = memoryImage.get().getShape();
            shape.setFill(Color.WHITE);
            shape.fillProperty().bind(Bindings.when(state.isEqualTo(State.HIDDEN)).then(Color.WHITE).otherwise(color));
        }

        return shape;
    }

	public MemoryImage getMemoryImage() {
		return memoryImage.get();
	}

	public State getState() {
		return state.get();
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public void setMemoryImage(MemoryImage image) {
		memoryImage.set(image);
	}

	public void setState(State s) {
		state.set(s);
	}

    public ObjectProperty<State> stateProperty() {
		return state;
	}

}
