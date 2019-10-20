package gaming.ex22;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

public class Card {
	protected DoubleProperty layoutX = new SimpleDoubleProperty(0);
	protected DoubleProperty layoutY = new SimpleDoubleProperty(0);
    protected boolean shown;

    public double getLayoutX() {
		return layoutX.get();
    }

    public double getLayoutY() {
		return layoutY.get();
    }

    public boolean isShown() {
        return shown;
    }

    public void setLayoutX(double layoutX) {
		this.layoutX.set(layoutX);
    }

    public void setLayoutY(double layoutY) {
		this.layoutY.set(layoutY);
    }

    public void setShown(boolean value) {
        shown = value;
    }

}
