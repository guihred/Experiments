/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gaming.ex13;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

/**
 *
 * @author Note
 */
public class SolitaireModel {

	private Pane gridPane;
	CardStack[] ascendingStacks = new CardStack[4];
	CardStack[] simpleStacks = new CardStack[7];

	public static SolitaireModel create(Pane gridPane, Scene scene) {
		return new SolitaireModel(gridPane, scene);
	}

	public SolitaireModel(Pane gridPane, Scene scene) {
		this.gridPane = gridPane;
		List<SolitaireCard> allCards = getAllCards();
		CardStack dropCardStack = new CardStack();
		dropCardStack.layoutXProperty().bind(scene.widthProperty().divide(7).multiply(1));
		dropCardStack.setLayoutY(0);
		dropCardStack.setOnMouseClicked(e -> {

		});
		makeDraggable(dropCardStack);
		CardStack cardStack = new CardStack();
		cardStack.setLayoutX(0);
		cardStack.setLayoutY(0);
		cardStack.addCards(allCards);
		cardStack.setOnMouseClicked(e -> {
			if (!cardStack.getCards().isEmpty()) {
				SolitaireCard lastCards = cardStack.removeLastCards();
				lastCards.setShown(true);
				dropCardStack.addCards(lastCards);
			} else {
				List<SolitaireCard> removeAllCards = dropCardStack.removeAllCards();
				Collections.reverse(removeAllCards);
				removeAllCards.forEach(c -> c.setShown(false));
				cardStack.addCards(removeAllCards);
			}
		});

		gridPane.getChildren().add(cardStack);
		gridPane.getChildren().add(dropCardStack);
		for (int i = 0; i < 4; i++) {
			ascendingStacks[i] = new CardStack();
			ascendingStacks[i].layoutXProperty().bind(scene.widthProperty().divide(7).multiply(3 + i));
			makeDraggable(ascendingStacks[i]);
			gridPane.getChildren().add(ascendingStacks[i]);
		}
		for (int i = 0; i < 7; i++) {
			simpleStacks[i] = new CardStack();
			simpleStacks[i].layoutXProperty().bind(scene.widthProperty().divide(7).multiply(i));
			simpleStacks[i].setLayoutY(200);
			List<SolitaireCard> removeLastCards = cardStack.removeLastCards(i + 1);
			System.out.println(removeLastCards);
			removeLastCards.forEach(c -> c.setShown(false));
			removeLastCards.get(i).setShown(true);
			simpleStacks[i].addCardsVertically(removeLastCards);

			makeDraggable(simpleStacks[i]);
			gridPane.getChildren().add(simpleStacks[i]);
		}

    }

	private List<SolitaireCard> getAllCards() {
		SolitaireNumber[] solitaireNumbers = SolitaireNumber.values();
		SolitaireSuit[] solitaireSuits = SolitaireSuit.values();
		List<SolitaireCard> allCards = new ArrayList<>();

		for (int i = 0; i < solitaireNumbers.length; i++) {
			for (int j = 0; j < solitaireSuits.length; j++) {
				SolitaireNumber a = solitaireNumbers[i];
				SolitaireSuit b = solitaireSuits[j];
				SolitaireCard solitaireCard = new SolitaireCard(a, b);
				makeDraggable(solitaireCard);
				allCards.add(solitaireCard);
			}
		}
		Collections.shuffle(allCards);
		return allCards;
	}

	public void makeDraggable(final Node node) {
		node.setOnMousePressed(onMousePressed);
		node.setOnMouseDragged(onMouseDragged);
		node.setOnMouseReleased(onMouseReleased);
	}

	DragContext dragContext = new DragContext();
	EventHandler<MouseEvent> onMousePressed = event -> {

		Node node = (Node) event.getSource();

		dragContext.x = node.getBoundsInParent().getMinX() - event.getScreenX();
		dragContext.y = node.getBoundsInParent().getMinY() - event.getScreenY();
		if (node instanceof CardStack) {
			CardStack cardStack = (CardStack) node;
			if (Stream.of(simpleStacks).anyMatch(s -> s == node)) {
				ObservableList<SolitaireCard> cards = cardStack.getCards();
				List<SolitaireCard> lastCards = new ArrayList<>();
				for (SolitaireCard solitaireCard : cards) {
					if (solitaireCard.isShown()) {
						if (solitaireCard.getLayoutY() < event.getY()) {
							lastCards.clear();
						}
						lastCards.add(solitaireCard);
					}
				}
				dragContext.cards = null;
				dragContext.stack = cardStack;
				if (!lastCards.isEmpty()) {
					cardStack.removeCards(lastCards);
					dragContext.cards = lastCards;
					gridPane.getChildren().addAll(lastCards);
					dragContext.stack = cardStack;
				}
				
			} else {
				SolitaireCard lastCards = cardStack.removeLastCards();
				if (lastCards != null) {
					dragContext.cards = Arrays.asList(lastCards);
					gridPane.getChildren().add(lastCards);
					dragContext.stack = (CardStack) node;
				}
			}
		}

	};
	EventHandler<MouseEvent> onMouseDragged = event -> {

		Node node = (Node) event.getSource();

		double offsetX = event.getScreenX() + dragContext.x;
		double offsetY = event.getScreenY() + dragContext.y;
		if (node instanceof SolitaireCard) {
			dragContext.dragged = true;
		}
		if (dragContext.cards != null) {
			List<SolitaireCard> cards = dragContext.cards;
			for (int i = 0; i < cards.size(); i++) {
				SolitaireCard c = cards.get(i);
				c.relocate(offsetX, offsetY + i * 30);
			}

		}

	};

	EventHandler<MouseEvent> onMouseReleased = event -> {
		if (dragContext.cards != null) {
			if (dragContext.cards.size() == 1) {
				for (CardStack cardStack : ascendingStacks) {
					if (cardStack.getBoundsInParent().intersects(dragContext.cards.get(0).getBoundsInParent())) {
						if (cardStack.getCards().isEmpty() && dragContext.cards.get(0).getNumber() != SolitaireNumber.ACE) {
							break;
						}
						if (!cardStack.getCards().isEmpty()
								&& (dragContext.cards.get(0).getSuit() != cardStack.getLastCards().getSuit() || dragContext.cards
										.get(0).getNumber().getNumber() != cardStack.getLastCards().getNumber().getNumber() + 1)) {
							break;
						}
						cardStack.addCards(dragContext.cards);
						if (!dragContext.stack.getCards().isEmpty()) {
							if (dragContext.stack.getCards().stream().noneMatch(SolitaireCard::isShown)) {
								dragContext.stack.getLastCards().setShown(true);
							}
						}
						dragContext.cards = null;
						return;
					}
				}
			}
			for (CardStack cardStack : simpleStacks) {
				if (cardStack.getBoundsInParent().intersects(dragContext.cards.get(0).getBoundsInParent())) {
					if (cardStack.getCards().isEmpty()
							&& dragContext.cards.get(0).getNumber() != SolitaireNumber.KING) {
						break;
					}
					if (!cardStack.getCards().isEmpty()
							&& (dragContext.cards.get(0).getSuit().getColor() == cardStack.getLastCards().getSuit()
									.getColor()
									|| dragContext.cards.get(0).getNumber()
											.getNumber() != cardStack.getLastCards().getNumber().getNumber() - 1)) {
						break;
					}

					cardStack.addCardsVertically(dragContext.cards);
					if (!dragContext.stack.getCards().isEmpty()) {
						if (dragContext.stack.getCards().stream().noneMatch(SolitaireCard::isShown)) {
							dragContext.stack.getLastCards().setShown(true);
						}

					}
					dragContext.cards = null;
					return;
				}
			}
			if (Stream.of(simpleStacks).anyMatch(s -> s == dragContext.stack)) {
				dragContext.stack.addCardsVertically(dragContext.cards);
			} else {
				dragContext.stack.addCards(dragContext.cards);
			}
		}

	};

	class DragContext {
		double x;
		double y;
		boolean dragged;
		List<SolitaireCard> cards;
		CardStack stack;
	}

}
