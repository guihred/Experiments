package gaming.ex15;

import javafx.beans.NamedArg;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.Group;

public class Numbers3D extends Group {

	private IntegerProperty num = new SimpleIntegerProperty(0);

    public Numbers3D() {
        this(0);
    }
    public Numbers3D(@NamedArg("number") int number) {
		num.set(number);
		Number3D number0 = new Number3D(0);
		Number3D number1 = new Number3D(0);
		number1.translateXProperty().bind(number1.sizeProperty().divide(-2).subtract(5));
		number0.translateXProperty().bind(number1.sizeProperty().divide(2).add(5));
		number0.numProperty().bind(num);
		number1.numProperty().bind(num.divide(10));
		getChildren().addAll(number1, number0);
	}

	public int getNumber() {
	    return num.get();
	}

    public IntegerProperty numProperty() {
		return num;
	}

}