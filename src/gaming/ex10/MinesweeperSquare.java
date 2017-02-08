/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gaming.ex10;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.scene.effect.InnerShadow;
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
public class MinesweeperSquare extends Region {

    public enum State {
        HIDDEN,
        SHOWN
    }

	private final int i, j;
	private ObjectProperty<MinesweeperImage> minesweeperImage = new SimpleObjectProperty<>(MinesweeperImage.BLANK);
	private int num;
    private Shape shape;

    private ObjectProperty<State> state = new SimpleObjectProperty<>(State.HIDDEN);


	public MinesweeperSquare(int i, int j) {
        this.i = i;
        this.j = j;
        setPadding(new Insets(10));
        setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, new Insets(1))));
		styleProperty().bind(

				Bindings.when(state.isEqualTo(State.HIDDEN))
                .then("-fx-background-color: burlywood;")
						.otherwise("-fx-background-color: white;")
						.concat("-fx-border-color: black;-fx-border-width: 1;"));

		setEffect(new InnerShadow());
        setPrefSize(50, 50);
    }

	public Shape getFinalShape() {
		if (shape == null) {
			shape = getMinesweeperImage().getShape(getNum());
			Color color = getMinesweeperImage().equals(MinesweeperImage.BOMB) ? Color.RED
					: getMinesweeperImage().equals(MinesweeperImage.NUMBER) ? Color.BLUE : Color.WHITE;

			shape.fillProperty()
					.bind(Bindings.when(state.isEqualTo(State.HIDDEN)).then(Color.TRANSPARENT).otherwise(color));

		}

		return shape;
	}

    public final MinesweeperImage getMinesweeperImage() {
		return minesweeperImage.get();
	}

	public int getNum() {
		return num;
	}

	public final State getState() {
		return state.get();
	}

	public void setFinalShape(Shape shape) {
		this.shape = shape;
    }

	public final void setMinesweeperImage(final MinesweeperImage minesweeperImage) {
		this.minesweeperImage.set(minesweeperImage);
	}


	public void setNum(int num) {
		this.num = num;
	}


	public final void setState(final State state) {
		this.state.set(state);
	}

	public int getJ() {
		return j;
	}

	public int getI() {
		return i;
	}

}
