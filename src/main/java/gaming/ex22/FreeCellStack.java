package gaming.ex22;

//import android.graphics.RectF;
//import java.awt.Canvas;
//import java.awt.Paint;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

class FreeCellStack extends StackOfCards {
    final StackType type;
    private final int n;
    private final List<FreeCellCard> cards = new ArrayList<>();
//    private final Paint paint = new Paint();
    private double maxHeight;
    private Rectangle boundsF;

    FreeCellStack(StackType type, int n) {
        this.type = type;
        this.n = n;
//        paint.setColor(Color.BLACK);
//        paint.setStyle(Paint.Style.STROKE);
    }

    public void draw(Canvas canvas) {
//        paint.setColor(Color.BLACK);
//        paint.setStyle(Paint.Style.STROKE);
        double right = (double) FreeCellCard.getCardWidth() + layoutX;
        double bottom = (double) FreeCellCard.getCardWidth() + layoutY;
        canvas.getGraphicsContext2D().setFill(Color.BLACK);
        canvas.getGraphicsContext2D().strokeRoundRect(getLayoutX(), getLayoutY(), right, bottom, 5, 5);
        for (FreeCellCard card : cards) {
            card.draw(canvas, layoutX, layoutY);
        }

    }


    @Override
    public String toString() {
        return "(" +
                "type=" + type +
                ", n=" + n +
                ')';
    }

    void addCards(Collection<FreeCellCard> cards) {
        if (type == StackType.SIMPLE) {
            addCardsVertically(cards);
        } else {
            addCards(cards.toArray(new FreeCellCard[0]));
        }
    }

    void addCards(FreeCellCard... cards) {
        for (FreeCellCard solitaireCard : cards) {
            if (!this.cards.contains(solitaireCard)) {
                this.cards.add(solitaireCard);
                solitaireCard.setLayoutX(0);
                solitaireCard.setLayoutY(0);
            }
        }
    }

    void addCardsVertically(FreeCellCard... cards) {
        for (FreeCellCard solitaireCard : cards) {
            if (!this.cards.contains(solitaireCard)) {
                this.cards.add(solitaireCard);
                solitaireCard.setLayoutX(0);
            }
        }
        adjust();

    }

    double adjust() {
        return adjust(cards.size());
    }

    double adjust(int cards) {
        if (type != StackType.SIMPLE) {
            return 0;
        }
        int layout = 0;
        for (int i = 0; i < this.cards.size(); i++) {
            FreeCellCard solitaireCard = this.cards.get(i);
            solitaireCard.setLayoutY(layout);
            layout += FreeCellCard.getCardWidth() / 3;
        }
        double spaceToDisplay = maxHeight - layoutY - FreeCellCard.getCardWidth() / 3F;
        if (FreeCellCard.getCardWidth() / 3 * cards <= spaceToDisplay) {
            return layout - FreeCellCard.getCardWidth() / 3F;
        }
        double newGap = spaceToDisplay / cards;
        layout = 0;
        for (int i = 0; i < this.cards.size(); i++) {
            FreeCellCard solitaireCard = this.cards.get(i);
            solitaireCard.setLayoutY(layout);
            layout += newGap;
        }
        return layout - newGap;

    }

    Rectangle getBoundsF() {
        if (boundsF == null) {
            boundsF = new Rectangle();
        }
        double right = FreeCellCard.getCardWidth();
        double bottom = cards.isEmpty() ? FreeCellCard.getCardWidth() : getLastCards().getBoundsF().getHeight();

        boundsF.setX(layoutX);
        boundsF.setY(layoutY);
        boundsF.setWidth(right);
        boundsF.setHeight(bottom);
        return boundsF;
    }

    List<FreeCellCard> getCards() {
        return cards;
    }

    FreeCellCard getLastCards() {
        if (cards.isEmpty()) {
            return null;
        }
        return cards.get(cards.size() - 1);
    }

    int getShownCards() {
        return (int) cards.stream().filter(FreeCellCard::isShown).count();
    }

    void removeCards(List<FreeCellCard> cards) {
        removeCards(cards.toArray(new FreeCellCard[0]));
    }

    void removeLastCards() {
        if (cards.isEmpty()) {
            return;
        }

        cards.remove(cards.size() - 1);
    }

    void setMaxHeight(int maxHeight) {
        this.maxHeight = maxHeight;
    }

    private void addCardsVertically(Collection<FreeCellCard> cards) {
        addCardsVertically(cards.toArray(new FreeCellCard[0]));
    }

    private void removeCards(FreeCellCard... cards) {
        for (FreeCellCard solitaireCard : cards) {
            this.cards.remove(solitaireCard);
        }
    }

    public enum StackType {
        SIMPLE,
        ASCENDING,
        SUPPORT
    }
}