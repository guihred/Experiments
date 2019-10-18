package gaming.ex22;

import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;

public class FreeCellCard extends Card {

    private final FreeCellNumber number;
    private final FreeCellSuit suit;
    private final SVGPath drawable;
    private boolean autoMoved;
    private Rectangle bounds;
    private Rectangle boundsF;

    FreeCellCard(FreeCellNumber number, FreeCellSuit suit) {
        this.number = number;
        this.suit = suit;
//        paint.setStyle(Paint.Style.STROKE);
        drawable = suit.getShape();
    }

    public void draw(Canvas canvas, double layoutX, double layoutY) {
        Rectangle Rectangle = getBoundsF();
        double w = Rectangle.getWidth();
        double h = Rectangle.getHeight();
//        paint.setStyle(Paint.Style.FILL);
//        paint.setColor(Color.WHITE);
        canvas.getGraphicsContext2D().setFill(Color.WHITE);
        canvas.getGraphicsContext2D().fillRoundRect(layoutX, layoutY, w, h, 5, 5);
        canvas.getGraphicsContext2D().setStroke(Color.BLACK);
        canvas.getGraphicsContext2D().strokeRoundRect(layoutX, layoutY, w, h, 5, 5);
        if (!shown) {
            return;
        }
//        paint.setTextSize(getCardWidth() / 4F);
        int left = (int) (layoutX + this.layoutX) + getCardWidth() / 3;
        int top = (int) (layoutY + this.layoutY) + getCardWidth() / 10 / 2;
//        drawable.setBounds(left, top, left + getCardWidth() / 4, top + getCardWidth() / 4);
//        drawable.draw(canvas);
//        canvas.getGraphicsContext2D().

        canvas.getGraphicsContext2D().strokeText(number.getRepresentation(), left - getCardWidth() / 4.,
            top + getCardWidth() / 4.);
    }




    public Rectangle getBounds() {
        if (bounds == null) {
            bounds = new Rectangle();
        }

        boundsF.setX(layoutX);
        boundsF.setY(layoutY);
        boundsF.setWidth(getCardWidth());
        boundsF.setHeight(getCardWidth());
        return bounds;
    }

    public FreeCellNumber getNumber() {
        return number;
    }

    @Override
    public String toString() {
        return getNumber().getRepresentation() + " " + suit;
    }

    Rectangle getBoundsF() {
        if (boundsF == null) {
            boundsF = new Rectangle();
        }
        boundsF.setX(layoutX);
        boundsF.setY(layoutY);
        boundsF.setWidth(getCardWidth());
        boundsF.setHeight(getCardWidth());
        return boundsF;
    }

    FreeCellSuit getSuit() {
        return suit;
    }

    boolean isAutoMoved() {
        return autoMoved;
    }

    void relocate(double layoutX1, double layoutY1) {
        setLayoutX(layoutX1);
        setLayoutY(layoutY1);
    }

    void setAutoMoved(boolean autoMoved) {
        this.autoMoved = autoMoved;
    }
}
