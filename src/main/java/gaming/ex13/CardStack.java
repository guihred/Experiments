package gaming.ex13;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

class CardStack extends Pane {

	private ObservableList<SolitaireCard> cards = FXCollections.observableArrayList();

	public CardStack() {
		setPrefSize(50, 75);
		setPadding(new Insets(10));
		setBackground(new Background(new BackgroundFill(Color.GREEN, new CornerRadii(5), new Insets(1))));
		setStyle("-fx-border-color: black; -fx-border-width: 1; -fx-border-radius: 5;");
		styleProperty().bind(Bindings.createStringBinding(() -> {
			StringBuilder style = new StringBuilder();
			style.append("-fx-border-color: black; -fx-border-width: 1; -fx-border-radius: 5;");
			if (cards.isEmpty()) {
				style.append("-fx-background-color:green;");
			}
			return style.toString();
		}, cards));

	}

	public SolitaireCard getLastCards() {
		if (cards.isEmpty()) {
			return null;
		}
		return cards.get(cards.size() - 1);
	}

	public List<SolitaireCard> removeLastCards(int n) {
		if (cards.isEmpty()) {
			return null;
		}
		List<SolitaireCard> lastCards = new ArrayList<>();
		for (int i = 0; i < n; i++) {
			SolitaireCard solitaireCard = cards.remove(cards.size() - 1);
			getChildren().remove(solitaireCard);
			lastCards.add(solitaireCard);
		}
		return lastCards;
	}
	public SolitaireCard removeLastCards() {
		if (cards.isEmpty()) {
			return null;
		}
		SolitaireCard solitaireCard = cards.remove(cards.size() - 1);
		getChildren().remove(solitaireCard);
		return solitaireCard;
	}

	public void addCards(List<SolitaireCard> cards1) {
		addCards(cards1.toArray(new SolitaireCard[0]));
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

	public ObservableList<SolitaireCard> getCards() {
		return cards;
	}

	public List<SolitaireCard> removeAllCards() {
		getChildren().clear();
		List<SolitaireCard> collect = cards.stream().collect(Collectors.toList());
		cards.clear();
		return collect;
	}
}