package gaming.ex22;

import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;

public class FreeCellCard extends Group {
    private static DoubleProperty cardWidth = new SimpleDoubleProperty(0);
    private final FreeCellNumber number;
    private final FreeCellSuit suit;
    private boolean autoMoved;

    private boolean shown;

    public FreeCellCard(FreeCellNumber number, FreeCellSuit suit) {
        this.number = number;
        this.suit = suit;
        setManaged(false);
        Rectangle rectangle = new Rectangle();
        rectangle.widthProperty().bind(FreeCellCard.cardWidth());
        rectangle.heightProperty().bind(FreeCellCard.cardWidth());
        rectangle.setFill(Color.WHITE);
        rectangle.setStroke(Color.BLACK);
        rectangle.setArcWidth(5);
        rectangle.setArcHeight(5);
        getChildren().add(rectangle);
        Text e = new Text(number.getRepresentation());
        e.layoutXProperty().bind(FreeCellCard.cardWidth().divide(8));
        e.layoutYProperty().bind(FreeCellCard.cardWidth().divide(4));
        SVGPath shape = suit.getShape();
        shape.layoutXProperty().bind(FreeCellCard.cardWidth().divide(3));
        shape.layoutYProperty().bind(FreeCellCard.cardWidth().divide(10));
        getChildren().add(e);
        getChildren().add(shape);

    }

    public FreeCellNumber getNumber() {
        return number;
    }

    public FreeCellSuit getSuit() {
        return suit;
    }

    public boolean isAutoMoved() {
        return autoMoved;
    }

    public boolean isShown() {
        return shown;
    }

    @Override
    public void relocate(double layoutX1, double layoutY1) {
        setLayoutX(layoutX1);
        setLayoutY(layoutY1);
    }

    public void setAutoMoved(boolean autoMoved) {
        this.autoMoved = autoMoved;
    }

    public void setShown(boolean value) {
        shown = value;
    }

    @Override
    public String toString() {
        return getNumber().getRepresentation() + " " + suit;
    }

    public static double getCardWidth() {
        return cardWidth().get();
    }

    public static void setCardWidth(double cardWidth) {
        FreeCellCard.cardWidth.set(cardWidth);
    }

    private static DoubleBinding cardWidth() {
        return cardWidth.multiply(4).divide(5);
    }
}
