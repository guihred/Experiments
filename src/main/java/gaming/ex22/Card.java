package gaming.ex22;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

public class Card {
    protected final DoubleProperty layoutX = new SimpleDoubleProperty(0);
    protected final DoubleProperty layoutY = new SimpleDoubleProperty(0);

    public double getLayoutX() {
		return layoutX.get();
    }

    public double getLayoutY() {
		return layoutY.get();
    }


    public void setLayoutX(double layoutX) {
		this.layoutX.set(layoutX);
    }

    public void setLayoutY(double layoutY) {
		this.layoutY.set(layoutY);
    }


}
