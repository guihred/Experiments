package gaming.ex13;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import simplebuilder.SimpleRectangleBuilder;

public class CardStack extends Region {

	private ObservableList<SolitaireCard> cards = FXCollections.observableArrayList();

	public CardStack() {
        setPrefSize(SolitaireCard.PREF_WIDTH, SolitaireCard.PREF_HEIGHT);
        setMinSize(SolitaireCard.PREF_WIDTH, SolitaireCard.PREF_HEIGHT);
		setPadding(new Insets(10));
        setBackground(new Background(new BackgroundFill(Color.GREEN, new CornerRadii(5), new Insets(1))));
        setManaged(false);
        getStyleClass().add("cardstack");
        setWidth(SolitaireCard.PREF_WIDTH);
        setHeight(SolitaireCard.PREF_HEIGHT);
        prefHeight(SolitaireCard.PREF_HEIGHT);
        setBorder(new Border(
            new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, new CornerRadii(5), BorderWidths.DEFAULT)));
        setShape(addRegion());
	}

    public void addCards(List<SolitaireCard> cards1) {
		addCards(cards1.toArray(new SolitaireCard[0]));
	}

	public void addCards(SolitaireCard... cards1) {
		for (SolitaireCard solitaireCard : cards1) {
			if (!cards.contains(solitaireCard)) {
				cards.add(solitaireCard);
				solitaireCard.setLayoutX(0);
				solitaireCard.setLayoutY(0);
				getChildren().add(solitaireCard);
			}
		}
	}

	public void addCardsVertically(List<SolitaireCard> cards1) {
		addCardsVertically(cards1.toArray(new SolitaireCard[0]));
	}
	public void addCardsVertically(SolitaireCard... cards1) {
		for (SolitaireCard solitaireCard : cards1) {
			if (!cards.contains(solitaireCard)) {
				cards.add(solitaireCard);
				solitaireCard.setLayoutX(0);
				getChildren().add(solitaireCard);
			}
		}
		double layout = 0;
		for (int i = 0; i < cards.size(); i++) {
			SolitaireCard solitaireCard = cards.get(i);
			solitaireCard.setLayoutY(layout);
			layout += solitaireCard.isShown() ? 30 : 15;
		}

	}

	public ObservableList<SolitaireCard> getCards() {
		return cards;
	}

    public SolitaireCard getLastCards() {
		if (cards.isEmpty()) {
			return null;
		}
		return cards.get(cards.size() - 1);
	}

	public List<SolitaireCard> removeAllCards() {
		getChildren().clear();
        List<SolitaireCard> cardsCopy = cards.stream().collect(Collectors.toList());
		cards.clear();
        return cardsCopy;
	}

	public void removeCards(List<SolitaireCard> cards1) {
		removeCards(cards1.toArray(new SolitaireCard[0]));
	}

	public void removeCards(SolitaireCard... cards1) {
		for (SolitaireCard solitaireCard : cards1) {
			if (cards.contains(solitaireCard)) {
				cards.remove(solitaireCard);
				getChildren().remove(solitaireCard);
			}
		}
	}

	public SolitaireCard removeLastCards() {
		if (cards.isEmpty()) {
			return null;
		}
		SolitaireCard solitaireCard = cards.remove(cards.size() - 1);
		getChildren().remove(solitaireCard);
		return solitaireCard;
	}

	public List<SolitaireCard> removeLastCards(int n) {
		if (cards.isEmpty()) {
            return Collections.emptyList();
		}
		List<SolitaireCard> lastCards = new ArrayList<>();
		for (int i = 0; i < n; i++) {
			SolitaireCard solitaireCard = cards.remove(cards.size() - 1);
			getChildren().remove(solitaireCard);
			lastCards.add(solitaireCard);
		}
		return lastCards;
	}

	public void  setCards(ObservableList<SolitaireCard> value) {
        addCards(value);
	}

    private static Rectangle addRegion() {
        return new SimpleRectangleBuilder().styleClass("cardStack").width(SolitaireCard.PREF_WIDTH)
            .arcHeight(10).arcWidth(10)
            .height(SolitaireCard.PREF_HEIGHT).build();
    }
}