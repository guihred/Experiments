/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gaming.ex10;

import javafx.beans.NamedArg;
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

	private final int i;

	private final int j;
	private ObjectProperty<MinesweeperImage> minesweeperImage = new SimpleObjectProperty<>(MinesweeperImage.BLANK);
	private int num;
	private Shape shape;
	private Shape flag = MinesweeperImage.FLAG.getShape(0);
	private ObjectProperty<State> state = new SimpleObjectProperty<>(State.HIDDEN);

    public MinesweeperSquare() {
        this(0, 0);
    }

    public MinesweeperSquare(@NamedArg("i") int i, @NamedArg("j") int j) {
		this.i = i;
		this.j = j;
		setPadding(new Insets(10));
		setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, new Insets(1))));
		styleProperty().bind(Bindings.when(state.isEqualTo(State.SHOWN)).then("-fx-background-color: white;")
				.otherwise("-fx-background-color: burlywood;").concat("-fx-border-color: black;-fx-border-width: 1;"));
		flag.visibleProperty().bind(state.isEqualTo(State.FLAGGED));
		setEffect(new InnerShadow());
		setPrefSize(50, 50);
	}

	public Shape getFinalShape() {
		if (shape == null) {
			shape = getMinesweeperImage().getShape(getNum());
			Color color = getColor();

			shape.fillProperty()
			.bind(Bindings.when(state.isEqualTo(State.SHOWN)).then(color).otherwise(Color.TRANSPARENT));

		}

		return shape;
	}

	public Shape getFlag() {
		return flag;
	}

	public int getI() {
		return i;
	}

	public int getJ() {
		return j;
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

	public ObjectProperty<State> stateProperty() {
		return state;
	}

	@Override
	public String toString() {
		return "MinesweeperSquare [i=" + i + ", j=" + j + ", minesweeperImage=" + minesweeperImage + ", num=" + num
				+ ", state=" + state + "]";
	}

	private Color getColor() {
		if (getMinesweeperImage() == MinesweeperImage.BOMB) {
			return Color.RED;
		}
		if (getMinesweeperImage() == MinesweeperImage.NUMBER) {
			return Color.BLUE;
		}
		return Color.WHITE;

	}

	public enum State {
		HIDDEN, SHOWN, FLAGGED
	}

}
