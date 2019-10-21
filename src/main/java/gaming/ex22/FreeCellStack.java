package gaming.ex22;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

class FreeCellStack extends StackOfCards {
    final StackType type;
    private final int n;
    private final List<FreeCellCard> cards = new ArrayList<>();
    private double maxHeight;
    private Rectangle boundsF;

    public FreeCellStack(StackType type, int n) {
        this.type = type;
        this.n = n;
    }

    public void addCards(Collection<FreeCellCard> cards1) {
        if (type == StackType.SIMPLE) {
            addCardsVertically(cards1);
        } else {
            addCards(cards1.toArray(new FreeCellCard[0]));
        }
    }

    public void addCards(FreeCellCard... cards1) {
        for (FreeCellCard solitaireCard : cards1) {
            if (!cards.contains(solitaireCard)) {
                cards.add(solitaireCard);
                solitaireCard.setLayoutX(0);
                solitaireCard.setLayoutY(0);
            }
        }
    }

    public void addCardsVertically(FreeCellCard... cards1) {
        for (FreeCellCard solitaireCard : cards1) {
            if (!cards.contains(solitaireCard)) {
                cards.add(solitaireCard);
                solitaireCard.setLayoutX(0);
            }
        }
        adjust();
    }

    public double adjust() {
        return adjust(cards.size());
    }

    public double adjust(int cards1) {
        if (type != StackType.SIMPLE) {
            return 0;
        }
        int layout = 0;
        for (int i = 0; i < cards.size(); i++) {
            FreeCellCard solitaireCard = cards.get(i);
            solitaireCard.setLayoutY(layout);
            layout += FreeCellCard.getCardWidth() / 3;
        }
        double spaceToDisplay = maxHeight - layoutY - FreeCellCard.getCardWidth() / 3F;
        if (FreeCellCard.getCardWidth() / 3 * cards1 <= spaceToDisplay) {
            return layout - FreeCellCard.getCardWidth() / 3F;
        }
        double newGap = spaceToDisplay / cards1;
        layout = 0;
        for (int i = 0; i < cards.size(); i++) {
            FreeCellCard solitaireCard = cards.get(i);
            solitaireCard.setLayoutY(layout);
            layout += newGap;
        }
        return layout - newGap;
    }

    public void draw(GraphicsContext gc) {
        gc.setStroke(Color.BLACK);
        gc.strokeRoundRect(getLayoutX(), getLayoutY(), FreeCellCard.getCardWidth(), FreeCellCard.getCardWidth(), 5, 5);
        for (FreeCellCard card : cards) {
            card.draw(gc, layoutX, layoutY);
        }
    }

    public Rectangle getBoundsF() {
        if (boundsF == null) {
            boundsF = new Rectangle();
        }
        double right = FreeCellCard.getCardWidth();
        double bottom = cards.isEmpty() ? FreeCellCard.getCardWidth() : getLastCards().getBounds().getHeight();
        boundsF.setX(layoutX);
        boundsF.setY(layoutY);
        boundsF.setWidth(right);
        boundsF.setHeight(bottom);
        return boundsF;
    }

    public List<FreeCellCard> getCards() {
        return cards;
    }

    public FreeCellCard getLastCards() {
        if (cards.isEmpty()) {
            return null;
        }
        return cards.get(cards.size() - 1);
    }

    public int getShownCards() {
        return (int) cards.stream().filter(FreeCellCard::isShown).count();
    }

    public void removeCards(List<FreeCellCard> cards1) {
        removeCards(cards1.toArray(new FreeCellCard[0]));
    }

    public void removeLastCards() {
        if (cards.isEmpty()) {
            return;
        }
        cards.remove(cards.size() - 1);
    }

    public void setMaxHeight(double maxHeight) {
        this.maxHeight = maxHeight;
    }

    @Override
    public String toString() {
        return "(" + "type=" + type + ", n=" + n + ')';
    }

    private void addCardsVertically(Collection<FreeCellCard> cards1) {
        addCardsVertically(cards1.toArray(new FreeCellCard[0]));
    }

    private void removeCards(FreeCellCard... cards1) {
        for (FreeCellCard solitaireCard : cards1) {
            cards.remove(solitaireCard);
        }
    }

    public enum StackType {
        SIMPLE,
        ASCENDING,
        SUPPORT
    }
}