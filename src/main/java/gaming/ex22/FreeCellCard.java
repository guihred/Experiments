package gaming.ex22;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Group;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class FreeCellCard extends Group {
    protected static DoubleProperty cardWidth = new SimpleDoubleProperty(0);
    private final FreeCellNumber number;
    private final FreeCellSuit suit;
    private boolean autoMoved;
    private Rectangle bounds;

    private boolean shown;

    public FreeCellCard(FreeCellNumber number, FreeCellSuit suit) {
        this.number = number;
        this.suit = suit;
    }

    public void draw(GraphicsContext gc, double layoutX1, double layoutY1) {
        double w = FreeCellCard.getCardWidth();
        double h = FreeCellCard.getCardWidth();
        gc.setFill(Color.WHITE);
        gc.fillRoundRect(layoutX1 + getLayoutX(), layoutY1 + getLayoutY(), w, h, 5, 5);
        gc.setStroke(Color.BLACK);
        gc.strokeRoundRect(layoutX1 + getLayoutX(), layoutY1 + getLayoutY(), w, h, 5, 5);
        double left = layoutX1 + getLayoutX() + FreeCellCard.getCardWidth() / 3;
        double top = layoutY1 + getLayoutY() + FreeCellCard.getCardWidth() / 10 / 2;
        gc.beginPath();
        gc.moveTo(left, top);
        gc.appendSVGPath(suit.getResource());
        gc.setFill(suit.getColor());
        gc.fill();
        getBounds().setX(getLayoutX() + layoutX1);
        getBounds().setY(getLayoutY() + layoutY1);
        gc.closePath();
        gc.strokeText(number.getRepresentation(), left - FreeCellCard.getCardWidth() / 4.,
            top + FreeCellCard.getCardWidth() / 4.);
    }

    public Rectangle getBounds() {
        if (bounds == null) {
            bounds = new Rectangle();
        }
        bounds.setWidth(FreeCellCard.getCardWidth());
        bounds.setHeight(FreeCellCard.getCardWidth());
        return bounds;
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
        return cardWidth.get() * 4 / 5;
    }

    public static void setCardWidth(double cardWidth) {
        FreeCellCard.cardWidth.set(cardWidth);
    }
}
