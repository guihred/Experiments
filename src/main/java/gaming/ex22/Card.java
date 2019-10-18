package gaming.ex22;

public class Card {
    private static int cardWidth;
    protected double layoutX;
    protected double layoutY;
    protected boolean shown;

    public double getLayoutX() {
        return layoutX;
    }

    public double getLayoutY() {
        return layoutY;
    }

    public boolean isShown() {
        return shown;
    }

    public void setLayoutX(double layoutX) {
        this.layoutX = layoutX;
    }

    public void setLayoutY(double layoutY) {
        this.layoutY = layoutY;
    }

    public void setShown(boolean value) {
        shown = value;
    }

    public static int getCardWidth() {
        return cardWidth * 4 / 5;
    }

    public static void setCardWidth(int cardWidth) {
        Card.cardWidth = cardWidth;
    }

}
