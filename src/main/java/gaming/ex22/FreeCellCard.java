package gaming.ex22;

import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;

public class FreeCellCard extends Card {

    protected static int cardWidth;
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

	public void draw(Canvas canvas, double layoutX1, double layoutY1) {
        Rectangle Rectangle = getBoundsF();
        double w = Rectangle.getWidth();
        double h = Rectangle.getHeight();
//        paint.setStyle(Paint.Style.FILL);
//        paint.setColor(Color.WHITE);
        canvas.getGraphicsContext2D().setFill(Color.WHITE);
		canvas.getGraphicsContext2D().fillRoundRect(layoutX1, layoutY1, w, h, 5, 5);
        canvas.getGraphicsContext2D().setStroke(Color.BLACK);
		canvas.getGraphicsContext2D().strokeRoundRect(layoutX1, layoutY1, w, h, 5, 5);
        if (!shown) {
            return;
        }
//        paint.setTextSize(getCardWidth() / 4F);
		int left = (int) (layoutX1 + getLayoutX()) + FreeCellCard.getCardWidth() / 3;
		int top = (int) (layoutY1 + getLayoutY()) + FreeCellCard.getCardWidth() / 10 / 2;
//        drawable.setBounds(left, top, left + getCardWidth() / 4, top + getCardWidth() / 4);
//        drawable.draw(canvas);

        canvas.getGraphicsContext2D().strokeText(number.getRepresentation(), left - FreeCellCard.getCardWidth() / 4.,
            top + FreeCellCard.getCardWidth() / 4.);
    }




    public Rectangle getBounds() {
        if (bounds == null) {
            bounds = new Rectangle();
        }

		boundsF.setX(layoutX.get());
		boundsF.setY(layoutY.get());
        boundsF.setWidth(FreeCellCard.getCardWidth());
        boundsF.setHeight(FreeCellCard.getCardWidth());
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
		boundsF.setX(layoutX.get());
		boundsF.setY(layoutY.get());
        boundsF.setWidth(FreeCellCard.getCardWidth());
        boundsF.setHeight(FreeCellCard.getCardWidth());
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

	public static int getCardWidth() {
	    return cardWidth * 4 / 5;
	}

	public static void setCardWidth(int cardWidth) {
		FreeCellCard.cardWidth = cardWidth;
	}
}
