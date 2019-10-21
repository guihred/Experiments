package gaming.ex22;

import java.util.Collection;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class FreeCellStack extends Group {
    final StackType type;
    private final int n;
    private final ObservableList<FreeCellCard> cards = FXCollections.observableArrayList();
    private double maxHeight;

    public FreeCellStack(StackType type, int n) {
        this.type = type;
        this.n = n;
        setManaged(false);
        Rectangle e = new Rectangle(getLayoutX(), getLayoutY(), FreeCellCard.getCardWidth(),
            FreeCellCard.getCardWidth());
        e.setFill(Color.DARKGREEN);
        e.setStroke(Color.BLACK);
        e.setArcWidth(5);
        e.setArcHeight(5);
        getChildren().add(e);

        cards.addListener((Change<? extends FreeCellCard> c) -> {
            while (c.next()) {
                getChildren().addAll(c.getAddedSubList());
                getChildren().removeAll(c.getRemoved());
            }
        });

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
        double spaceToDisplay = maxHeight - getLayoutY() - FreeCellCard.getCardWidth() / 3.;
        if (FreeCellCard.getCardWidth() / 3 * cards1 <= spaceToDisplay) {
            return layout - FreeCellCard.getCardWidth() / 3;
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