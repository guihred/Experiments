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
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import simplebuilder.SimpleDialogBuilder;

/**
 *
 * @author Note
 */
public class SolitaireModel {
    private CardStack[] ascendingStacks = new CardStack[4];

    private CardDragContext dragContext = new CardDragContext();
    private Pane gridPane;
    private CardStack[] simpleStacks = new CardStack[7];

    public SolitaireModel(Pane gridPane, Scene scene) {
        this.gridPane = gridPane;
        initialize(scene);
    }

    public void makeDraggable(final Node node) {
        node.setOnMousePressed(this::handleMousePressed);
        node.setOnMouseDragged(this::handleMouseDragged);
        node.setOnMouseReleased(this::handleMouseReleased);
    }

    public void reset() {
        gridPane.getChildren().clear();
        initialize(gridPane.getScene());
    }

    private boolean checkAscendingCards(MouseEvent event) {
        if (isDoubleClicked(event)) {
            for (CardStack cardStack : ascendingStacks) {
                SolitaireCard solitaireCard = dragContext.cards.get(dragContext.cards.size() - 1);
                if (isNotAscendingStackCompatible(cardStack, solitaireCard)) {
                    continue;
                }
                cardStack.addCards(solitaireCard);
                if (isStackAllHidden(dragContext.stack)) {
                    dragContext.stack.getLastCards().setShown(true);
                }
                dragContext.cards = null;
                return true;
            }
        }
        return false;
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

    private List<CardStack> getHoveredStacks(CardStack[] stacks) {
        return Stream.of(stacks)
            .filter(s -> s.getBoundsInParent().intersects(dragContext.cards.get(0).getBoundsInParent()))
            .collect(Collectors.toList());
    }

    private void handleMouseDragged(MouseEvent event) {
        double offsetX = event.getScreenX() + dragContext.x;
        double offsetY = event.getScreenY() + dragContext.y;
        if (dragContext.cards != null) {
            List<SolitaireCard> cards = dragContext.cards;
            for (int i = 0; i < cards.size(); i++) {
                SolitaireCard c = cards.get(i);
                c.relocate(offsetX, offsetY + i * 30);
            }
        }
    }

    private void handleMousePressed(MouseEvent event) {
        Node node = (Node) event.getSource();
        dragContext.x = node.getBoundsInParent().getMinX() - event.getScreenX();
        dragContext.y = node.getBoundsInParent().getMinY() - event.getScreenY();
        dragContext.cards = null;
        if (node instanceof CardStack) {
            CardStack cardStack = (CardStack) node;
            if (Stream.of(simpleStacks).anyMatch(s -> s == node)) {
                ObservableList<SolitaireCard> cards = cardStack.getCards();
                List<SolitaireCard> lastCards = new ArrayList<>();
                for (SolitaireCard solitaireCard : cards.filtered(SolitaireCard::isShown)) {
                    if (solitaireCard.getLayoutY() < event.getY()) {
                        lastCards.clear();
                    }
                    lastCards.add(solitaireCard);
                }
                dragContext.cards = null;
                dragContext.stack = cardStack;
                if (!lastCards.isEmpty()) {
                    cardStack.removeCards(lastCards);
                    dragContext.cards = lastCards;
                    dragContext.y += event.getY();

                    lastCards.forEach(d -> {
                        d.setLayoutY(d.getLayoutY() + node.getBoundsInParent().getMinY());
                        d.setLayoutX(d.getLayoutX() + node.getBoundsInParent().getMinX());
                    });
                    gridPane.getChildren().addAll(lastCards);
                    dragContext.stack = cardStack;
                }
                return;
            }
            SolitaireCard lastCards = cardStack.removeLastCards();
            if (lastCards != null) {
                dragContext.y += event.getY();
                lastCards.setLayoutX(lastCards.getLayoutX() + node.getBoundsInParent().getMinX());
                lastCards.setLayoutY(lastCards.getLayoutY() + node.getBoundsInParent().getMinY());
                dragContext.cards = Arrays.asList(lastCards);
                gridPane.getChildren().add(lastCards);
                dragContext.stack = (CardStack) node;
            }
        }
    }

    private void handleMouseReleased(MouseEvent event) {
        if (isNullOrEmpty(dragContext.cards)) {
            return;
        }
        if (checkAscendingCards(event)) {
            return;
        }

        if (dragContext.cards.size() == 1) {
            for (CardStack cardStack : getHoveredStacks(ascendingStacks)) {
                if (isNotAscendingStackCompatible(cardStack, dragContext.cards.get(0))) {
                    continue;
                }
                cardStack.addCards(dragContext.cards);
                if (isStackAllHidden(dragContext.stack)) {
                    dragContext.stack.getLastCards().setShown(true);
                }
                dragContext.cards = null;
                verifyEnd();
                return;
            }
        }

        for (CardStack cardStack : getHoveredStacks(simpleStacks)) {
            if (isCardNotCompatibleWithStack(cardStack, dragContext.cards.get(0))) {
                continue;
            }
            cardStack.addCardsVertically(dragContext.cards);
            if (isStackAllHidden(dragContext.stack)) {
                dragContext.stack.getLastCards().setShown(true);
            }
            dragContext.cards = null;
            return;
        }
        if (Stream.of(simpleStacks).anyMatch(s -> s == dragContext.stack)) {
            dragContext.stack.addCardsVertically(dragContext.cards);
            return;
        }
        dragContext.stack.addCards(dragContext.cards);
        dragContext.cards = null;
        verifyEnd();
    }

    private void initialize(Scene scene) {

        List<SolitaireCard> allCards = getAllCards();
        CardStack dropCardStack = new CardStack();
        dropCardStack.layoutXProperty().bind(scene.widthProperty().divide(7).multiply(1));
        dropCardStack.setLayoutY(0);
        makeDraggable(dropCardStack);
        CardStack mainCardStack = new CardStack();
        mainCardStack.setLayoutX(0);
        mainCardStack.setLayoutY(0);
        mainCardStack.addCards(allCards);
        mainCardStack.setOnMouseClicked(e -> {
            if (!mainCardStack.getCards().isEmpty()) {
                SolitaireCard lastCards = mainCardStack.removeLastCards();
                lastCards.setShown(true);
                dropCardStack.addCards(lastCards);
            } else {
                List<SolitaireCard> removeAllCards = dropCardStack.removeAllCards();
                Collections.reverse(removeAllCards);
                removeAllCards.forEach(c -> c.setShown(false));
                mainCardStack.addCards(removeAllCards);
            }
        });

        gridPane.getChildren().add(mainCardStack);
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
            simpleStacks[i].setLayoutY(100);
            List<SolitaireCard> removeLastCards = mainCardStack.removeLastCards(i + 1);
            removeLastCards.forEach(c -> c.setShown(false));
            removeLastCards.get(i).setShown(true);
            simpleStacks[i].addCardsVertically(removeLastCards);

            makeDraggable(simpleStacks[i]);
            gridPane.getChildren().add(simpleStacks[i]);
        }
    }

    private void verifyEnd() {
        if (Stream.of(ascendingStacks).allMatch(e -> e.getCards().size() == SolitaireNumber.values().length)) {
            new SimpleDialogBuilder().text("You Win").button("Reset", this::reset).bindWindow(gridPane).displayDialog();
        }
    }

    public static SolitaireModel create(Pane gridPane, Scene scene) {
        return new SolitaireModel(gridPane, scene);
    }

    public static boolean isCardNotCompatibleWithStack(CardStack cardStack, SolitaireCard solitaireCard) {
        if (cardStack.getCards().isEmpty() && solitaireCard.getNumber() != SolitaireNumber.KING) {
            return true;
        }

        return !cardStack.getCards().isEmpty()
            && (solitaireCard.getSuit().getColor() == cardStack.getLastCards().getSuit().getColor()
                || solitaireCard.getNumber().getNumber() != cardStack.getLastCards().getNumber().getNumber() - 1);
    }

    public static boolean isNotAscendingStackCompatible(CardStack cardStack, SolitaireCard solitaireCard) {
        return isStackEmptyAndCardIsNotAce(cardStack, solitaireCard) || isNotNextCardInStack(cardStack, solitaireCard);
    }

    private static boolean isDoubleClicked(MouseEvent event) {
        return event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2;
    }

    private static boolean isNotNextCardInStack(CardStack cardStack, SolitaireCard solitaireCard) {
        return !cardStack.getCards().isEmpty() && (solitaireCard.getSuit() != cardStack.getLastCards().getSuit()
            || solitaireCard.getNumber().getNumber() != cardStack.getLastCards().getNumber().getNumber() + 1);
    }

    private static boolean isNullOrEmpty(List<?> cards) {
        return cards == null || cards.isEmpty();
    }

    private static boolean isStackAllHidden(CardStack stack) {
        return !stack.getCards().isEmpty() && stack.getCards().stream().noneMatch(SolitaireCard::isShown);
    }

    private static boolean isStackEmptyAndCardIsNotAce(CardStack cardStack, SolitaireCard solitaireCard) {
        return cardStack.getCards().isEmpty() && solitaireCard.getNumber() != SolitaireNumber.ACE;
    }

    private static class CardDragContext {
        protected List<SolitaireCard> cards;
        protected CardStack stack;
        protected double x;
        protected double y;
    }

}
