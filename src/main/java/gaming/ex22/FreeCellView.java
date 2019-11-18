/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gaming.ex22;

import static gaming.ex22.FreeCellStack.StackType.ASCENDING;
import static gaming.ex22.FreeCellStack.StackType.SIMPLE;
import static gaming.ex22.FreeCellStack.StackType.SUPPORT;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.animation.Timeline;
import javafx.event.EventType;
import javafx.scene.Group;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import org.slf4j.Logger;
import simplebuilder.SimpleDialogBuilder;
import simplebuilder.SimpleTimelineBuilder;
import utils.CommonsFX;
import utils.HasLogging;

/**
 * @author Note
 */
public class FreeCellView extends Group {
    private static final String RESET = "Reset";
    private static final String YOU_WON = "You Won";
    private static final int SIZE = 500;
    private static final int ANIMATION_DURATION = 200;
    private static final Logger LOG = HasLogging.log();
    private final FreeCellStack[] ascendingStacks = new FreeCellStack[4];
    private final FreeCellStack[] supportingStacks = new FreeCellStack[4];
    private final DragContext dragContext;
    private final List<FreeCellStack> cardStackList = new LinkedList<>();
    private final List<MotionHistory> history = new ArrayList<>();
    private final FreeCellStack[] simpleStacks = new FreeCellStack[8];
    private boolean youWin;

    public FreeCellView() {
        dragContext = new DragContext(this);
        onLayout();
        addEventHandler(MouseEvent.ANY, this::onTouchEvent);
        Rectangle e = new Rectangle(getWidth(), getHeight());
        e.setManaged(false);
        e.setFill(Color.DARKGREEN);
        e.setStroke(Color.BLACK);
        getChildren().add(0, e);

    }

    public void getBackInHistory() {
        if (!history.isEmpty()) {
            MotionHistory remove = history.remove(history.size() - 1);
            remove.cards.forEach(e -> createMovingCardAnimation(remove.targetStack, remove.originStack, e));
            dragContext.reset();
        }
    }

    public boolean onTouchEvent(MouseEvent event) {
        EventType<? extends MouseEvent> action = event.getEventType();
        if (action == MouseEvent.MOUSE_PRESSED) {
            handleMousePressed(event);
        } else if (action == MouseEvent.MOUSE_DRAGGED) {
            handleMouseDragged(event);
        } else if (action == MouseEvent.MOUSE_RELEASED) {
            handleMouseReleased();
        }
        return true;
    }

    public void reset() {
        getChildren().clear();
        youWin = false;
        cardStackList.clear();
        history.clear();
        double yOffset = FreeCellCard.getCardWidth() / (getWidth() > getHeight() ? 10 : 2);
        double xOffset = FreeCellCard.getCardWidth() / 10;
        for (int i = 0; i < 4; i++) {
            supportingStacks[i] = new FreeCellStack(SUPPORT, 0);
            supportingStacks[i].setLayoutX(i * getWidth() / 9 + xOffset);
            supportingStacks[i].setLayoutY(yOffset);
            cardStackList.add(supportingStacks[i]);
        }
        for (int i = 0; i < 4; i++) {
            ascendingStacks[i] = new FreeCellStack(ASCENDING, 0);
            ascendingStacks[i].setLayoutX(getWidth() / 9 * (i + 5) + xOffset);
            ascendingStacks[i].setLayoutY(yOffset);
            cardStackList.add(ascendingStacks[i]);
        }
        for (int i = 0; i < 8; i++) {
            simpleStacks[i] = new FreeCellStack(SIMPLE, i + 1);
            simpleStacks[i].setLayoutX(getWidth() / 8 * i + xOffset);
            simpleStacks[i].setLayoutY(FreeCellCard.getCardWidth() + xOffset + yOffset);
            simpleStacks[i].setMaxHeight(getHeight());
            cardStackList.add(simpleStacks[i]);
        }
        List<FreeCellCard> allCards = getAllCards();
        for (int i = 0; i < allCards.size(); i++) {
            FreeCellCard card = allCards.get(i);
            card.setShown(true);
            simpleStacks[i % 8].addCardsVertically(card);
        }
        getChildren().addAll(cardStackList);
        LOG.info("SOLITAIRE {}", "RESET");
    }

    private void automaticCard() {

        int solitaireNumber = Stream.of(ascendingStacks)
            .map(e -> e.getLastCards() != null ? e.getLastCards().getNumber().getNumber() : 0)
            .min(Comparator.comparing(e -> e)).orElse(1);
        List<FreeCellStack> commonStacks = Stream.concat(Stream.of(simpleStacks), Stream.of(supportingStacks))
            .collect(Collectors.toList());
        for (FreeCellStack stack : commonStacks) {
            for (FreeCellStack cardStack : ascendingStacks) {
                FreeCellCard solitaireCard = stack.getLastCards();
                if (solitaireCard != null && !isNotAscendingStackCompatible(cardStack, solitaireCard)
                    && !Objects.equals(dragContext.stack, stack) && !solitaireCard.isAutoMoved()
                    && solitaireCard.getNumber().getNumber() <= solitaireNumber + 2) {
                    solitaireCard.setAutoMoved(true);
                    createMovingCardAnimation(stack, cardStack, solitaireCard);
                    MotionHistory motionHistory = new MotionHistory(Arrays.asList(solitaireCard), stack, cardStack);
                    history.add(motionHistory);
                    return;
                }
            }
        }
        if (!youWin
            && Stream.of(ascendingStacks).allMatch(e -> e.getCards().size() == FreeCellNumber.values().length)) {
            youWin = true;
            new SimpleDialogBuilder().text(YOU_WON).button(RESET, this::reset).displayDialog();
        }

    }

    private void createMovingCardAnimation(FreeCellStack originStack, FreeCellStack targetStack,
        FreeCellCard solitaireCard) {
        cardStackList.remove(targetStack);
        cardStackList.add(targetStack);
        solitaireCard.setShown(true);
        originStack.removeLastCards();
        double x = -targetStack.getLayoutX() + originStack.getLayoutX();
        double y = -targetStack.getLayoutY() + originStack.getLayoutY() + solitaireCard.getLayoutY();
        targetStack.addCards(solitaireCard);
        solitaireCard.setLayoutX(x);
        solitaireCard.setLayoutY(y);
        double value = targetStack.adjust();
        Timeline eatingAnimation = new SimpleTimelineBuilder()
            .addKeyFrame(Duration.millis(ANIMATION_DURATION), solitaireCard.layoutXProperty(), 0)
            .addKeyFrame(Duration.millis(ANIMATION_DURATION), solitaireCard.layoutYProperty(), value).build();
        originStack.adjust();
        targetStack.toFront();
        eatingAnimation.setOnFinished(e -> automaticCard());
        eatingAnimation.play();
    }

    private void createMovingCardAnimation(FreeCellStack originStack, FreeCellStack targetStack,
        FreeCellCard solitaireCard, boolean first, int cards) {
        cardStackList.remove(targetStack);
        cardStackList.add(targetStack);
        solitaireCard.setShown(true);
        originStack.removeLastCards();
        originStack.adjust();
        double x = -targetStack.getLayoutX() + originStack.getLayoutX();
        double y = -targetStack.getLayoutY() + originStack.getLayoutY() + solitaireCard.getLayoutY();
        targetStack.addCards(solitaireCard);
        double adjust = targetStack.adjust(cards);
        solitaireCard.setLayoutX(x);
        solitaireCard.setLayoutY(y);
        Timeline eatingAnimation = new SimpleTimelineBuilder()
            .addKeyFrame(Duration.millis(ANIMATION_DURATION), solitaireCard.layoutXProperty(), 0)
            .addKeyFrame(Duration.millis(ANIMATION_DURATION), solitaireCard.layoutYProperty(), adjust).build();
        originStack.adjust();
        if (first) {
            eatingAnimation.setOnFinished(e -> automaticCard());
        }
        targetStack.toFront();
        eatingAnimation.play();
    }

    private Collection<FreeCellStack> getHoveredStacks(FreeCellStack[] stacks) {
        FreeCellCard next = dragContext.cards.iterator().next();
        return Stream.of(stacks).filter(s -> s.getBoundsInParent().intersects(next.getBoundsInParent()))
            .collect(Collectors.toList());
    }

    private void handleMouseDragged(MouseEvent event) {

        if (dragContext.stack == null) {
            return;
        }
        double offsetX = event.getX() + dragContext.x;
        double offsetY = event.getY() + dragContext.y;
        int i = 0;
        for (FreeCellCard c : dragContext.cards) {
            c.relocate(offsetX, offsetY + i * FreeCellCard.getCardWidth() / 3F);
            i++;
        }
    }

    private void handleMousePressed(MouseEvent event) {
        FreeCellStack stack = cardStackList.stream().filter(e -> CommonsFX.containsMouse(e, event)).findFirst()
            .orElse(null);
        if (stack == null) {
            dragContext.reset();
            return;
        }
        dragContext.x = stack.getLayoutX() - event.getX();
        dragContext.y = stack.getLayoutY() - event.getY();
        if (stack.getType() == SIMPLE || stack.getType() == SUPPORT) {
            List<FreeCellCard> cards = stack.getCards();
            List<FreeCellCard> lastCards = new ArrayList<>();
            List<FreeCellCard> showCards = cards.stream().filter(FreeCellCard::isShown).collect(Collectors.toList());
            for (FreeCellCard solitaireCard : showCards) {
                if (solitaireCard.getLayoutY() + stack.getLayoutY() < event.getY()
                    || !lastCards.isEmpty() && !isStackContinuous(lastCards)) {
                    lastCards.clear();
                }

                if (lastCards.size() >= pileMaxSize()) {
                    lastCards.remove(0);
                }

                lastCards.add(solitaireCard);
            }
            dragContext.cards.clear();
            dragContext.stack = stack;
            if (!lastCards.isEmpty()) {
                stack.removeCards(lastCards);
                dragContext.cards.addAll(lastCards);
                dragContext.y = stack.getLayoutY() + lastCards.get(0).getLayoutY() - event.getY();
                handleMouseDragged(event);
                dragContext.stack = stack;
            }
        }
    }

    private void handleMouseReleased() {
        if (!youWin
            && Stream.of(ascendingStacks).allMatch(e -> e.getCards().size() == FreeCellNumber.values().length)) {
            youWin = true;
            new SimpleDialogBuilder().text(YOU_WON).button(RESET, this::reset).displayDialog();
        }
        if (isNullOrEmpty(dragContext.cards)) {
            return;
        }
        if (dragContext.stack == null) {
            dragContext.reset();
            return;
        }

        FreeCellCard first = dragContext.cards.iterator().next();
        if (handleSingleCard(first)) {
            return;
        }

        if (handleSimpleStack(first)) {
            return;
        }
        cardStackList.sort(
            Comparator.comparing(FreeCellStack::getType).thenComparing((FreeCellStack e) -> -e.getCards().size()));

        if (tryToPlaceCard(first)) {
            return;
        }
        dragContext.stack.addCards(dragContext.cards);
        dragContext.reset();
    }

    private boolean handleSimpleStack(FreeCellCard first) {
        for (FreeCellStack cardStack : getHoveredStacks(simpleStacks)) {
            while (dragContext.cards.size() > pileMaxSize(cardStack)) {
                FreeCellCard remove = dragContext.cards.remove(0);
                dragContext.stack.addCards(remove);
            }
            if (notAcceptsCard(first, cardStack)) {
                continue;
            }

            MotionHistory motionHistory = new MotionHistory(dragContext.cards, dragContext.stack, cardStack);
            history.add(motionHistory);
            cardStack.addCards(dragContext.cards);
            dragContext.reset();
            automaticCard();
            return true;
        }
        return false;
    }

    private boolean handleSingleCard(FreeCellCard first) {
        if (dragContext.cards.size() == 1) {
            Collection<FreeCellStack> hoveredStacks = getHoveredStacks(ascendingStacks);
            hoveredStacks.addAll(getHoveredStacks(supportingStacks));
            for (FreeCellStack cardStack : hoveredStacks) {
                if (notAccept(first, cardStack)) {
                    continue;
                }
                MotionHistory motionHistory = new MotionHistory(dragContext.cards, dragContext.stack, cardStack);
                history.add(motionHistory);
                cardStack.addCards(dragContext.cards);
                dragContext.reset();
                automaticCard();
                if (!youWin && Stream.of(ascendingStacks)
                    .allMatch(e -> e.getCards().size() == FreeCellNumber.values().length)) {
                    youWin = true;
                    new SimpleDialogBuilder().text(YOU_WON).button(RESET, this::reset).displayDialog();
                }
                return true;
            }
        }
        return false;
    }

    private boolean moveFromSimpleStack(FreeCellCard firstCard, FreeCellStack currentStack) {
        if (currentStack.getType() == SIMPLE && isSimpleStackCompatible(firstCard, currentStack)
            && isStackContinuous(dragContext.cards)) {
            while (dragContext.cards.size() > pileMaxSize(currentStack)) {
                FreeCellCard remove = dragContext.cards.remove(0);
                dragContext.stack.addCards(remove);
            }
            dragContext.stack.addCards(dragContext.cards);
            boolean firstC = true;
            int finalSize = dragContext.cards.size() + currentStack.getShownCards();
            for (FreeCellCard c : dragContext.cards) {
                createMovingCardAnimation(dragContext.stack, currentStack, c, firstC, finalSize);
                firstC = false;
            }
            MotionHistory motionHistory = new MotionHistory(dragContext.cards, dragContext.stack, currentStack);
            history.add(motionHistory);
            dragContext.reset();
            return true;
        }
        return false;
    }

    private boolean notAccept(FreeCellCard first, FreeCellStack cardStack) {
        return Objects.equals(cardStack, dragContext.stack)
            || cardStack.getType() == ASCENDING && isNotAscendingStackCompatible(cardStack, first)
            || cardStack.getType() == SUPPORT && !cardStack.getCards().isEmpty();
    }

    private boolean notAcceptsCard(FreeCellCard first, FreeCellStack cardStack) {
        return cardStack.getCards().isEmpty()
            || first.getSuit().getColor() == cardStack.getLastCards().getSuit().getColor()
            || first.getNumber().getNumber() != cardStack.getLastCards().getNumber().getNumber() - 1
            || Objects.equals(cardStack, dragContext.stack);
    }

    private void onLayout() {
        FreeCellCard.setCardWidth(getWidth() / 8);
        if (cardStackList.isEmpty()) {
            reset();
        }
    }

    private long pileMaxSize() {
        return pileMaxSize(null);
    }

    private long pileMaxSize(FreeCellStack cardStack) {
        long supporting = Stream.of(supportingStacks).filter(e -> e.getCards().isEmpty()).count() + 1;
        long stackCount = Stream.of(simpleStacks).filter(e -> e.getCards().isEmpty() && !Objects.equals(cardStack, e))
            .count() + 1;
        return supporting * stackCount;
    }

    private boolean placeCard(FreeCellCard firstCard, FreeCellStack currentStack) {
        if (moveFromSimpleStack(firstCard, currentStack)) {
            return true;
        }
        if (currentStack.getType() == ASCENDING && dragContext.cards.size() == 1
            && isCompatibleAscending(firstCard, currentStack)) {
            dragContext.stack.addCards(dragContext.cards);
            createMovingCardAnimation(dragContext.stack, currentStack, firstCard);
            MotionHistory motionHistory = new MotionHistory(dragContext.cards, dragContext.stack, currentStack);
            history.add(motionHistory);
            dragContext.reset();
            return true;
        }
        if (dragContext.stack.getType() == SIMPLE && currentStack.getType() == SUPPORT
            && currentStack.getCards().isEmpty() && dragContext.cards.size() == 1) {
            dragContext.stack.addCards(dragContext.cards);
            createMovingCardAnimation(dragContext.stack, currentStack, firstCard);
            MotionHistory motionHistory = new MotionHistory(dragContext.cards, dragContext.stack, currentStack);
            history.add(motionHistory);
            dragContext.reset();
            return true;
        }
        return false;
    }

    private boolean tryToPlaceCard(FreeCellCard first) {
        for (FreeCellStack e : cardStackList) {
            if (Objects.equals(e, dragContext.stack)) {
                continue;
            }
            if (placeCard(first, e)) {
                return true;
            }
        }
        return false;
    }

    private static List<FreeCellCard> getAllCards() {
        FreeCellNumber[] solitaireNumbers = FreeCellNumber.values();
        FreeCellSuit[] solitaireSuits = FreeCellSuit.values();
        List<FreeCellCard> allCards = new ArrayList<>();
        for (FreeCellNumber number : solitaireNumbers) {
            for (FreeCellSuit suit : solitaireSuits) {
                FreeCellCard solitaireCard = new FreeCellCard(number, suit);
                allCards.add(solitaireCard);
            }
        }
        Collections.shuffle(allCards);
        return allCards;
    }

    private static double getHeight() {
        return SIZE;
    }

    private static double getWidth() {
        return SIZE;
    }

    private static boolean isCompatibleAscending(FreeCellCard first, FreeCellStack e) {
        return first.getNumber() == FreeCellNumber.ACE && e.getCards().isEmpty()
            || !e.getCards().isEmpty() && first.getSuit() == e.getLastCards().getSuit()
                && first.getNumber().getNumber() == e.getLastCards().getNumber().getNumber() + 1;
    }

    private static boolean isNotAscendingStackCompatible(FreeCellStack cardStack, FreeCellCard solitaireCard) {
        return isStackEmptyAndCardIsNotAce(cardStack, solitaireCard)
            || !cardStack.getCards().isEmpty() && (solitaireCard.getSuit() != cardStack.getLastCards().getSuit()
                || solitaireCard.getNumber().getNumber() != cardStack.getLastCards().getNumber().getNumber() + 1);
    }

    private static boolean isNullOrEmpty(Collection<?> cards) {
        return cards == null || cards.isEmpty();
    }

    private static boolean isSimpleStackCompatible(FreeCellCard first, FreeCellStack e) {
        return e.getCards().isEmpty()
            || !(e.getCards().isEmpty() || first.getSuit().getColor() == e.getLastCards().getSuit().getColor()
                || first.getNumber().getNumber() != e.getLastCards().getNumber().getNumber() - 1);
    }

    private static boolean isStackContinuous(Collection<FreeCellCard> first) {
        if (first.isEmpty()) {
            return false;
        }
        int n = -1;
        Color color = null;
        for (FreeCellCard c : first) {
            if (n == -1) {
                n = c.getNumber().getNumber();
                color = c.getSuit().getColor();
                continue;
            }
            if (color == c.getSuit().getColor()) {
                return false;
            }
            if (n != c.getNumber().getNumber() + 1) {
                return false;
            }
            n = c.getNumber().getNumber();
            color = c.getSuit().getColor();
        }
        return true;
    }

    private static boolean isStackEmptyAndCardIsNotAce(FreeCellStack cardStack, FreeCellCard solitaireCard) {
        return cardStack.getCards().isEmpty() && solitaireCard.getNumber() != FreeCellNumber.ACE;
    }

}
