package fxtests;

import gaming.ex11.DotsLauncher;
import gaming.ex11.DotsSquare;
import gaming.ex13.CardStack;
import gaming.ex13.SolitaireCard;
import gaming.ex13.SolitaireLauncher;
import gaming.ex13.SolitaireModel;
import java.util.*;
import java.util.stream.Collectors;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.input.MouseButton;
import org.junit.Test;

public final class FXEngineSolitaireTest extends AbstractTestExecution {
	@Test
    public void verifyDots() throws Exception {
        show(DotsLauncher.class);
        Set<Node> queryAll = lookup(e -> e instanceof DotsSquare).queryAll().stream().limit(20)
            .collect(Collectors.toSet());
        Random random = new Random();
        for (Node next : queryAll) {
            drag(next, MouseButton.PRIMARY);
            int a = random.nextBoolean() ? 1 : -1;
            if (random.nextBoolean()) {
                moveBy(a * DotsSquare.SQUARE_SIZE, 0);
            } else {
                moveBy(0, a * DotsSquare.SQUARE_SIZE);
            }
            drop();
        }
    }

    @Test
	public void verifySolitaire() throws Exception {
		show(SolitaireLauncher.class);
		List<CardStack> cardStacks = lookup(".cardstack").queryAllAs(CardStack.class).stream()
				.collect(Collectors.toList());
		Collections.shuffle(cardStacks);
		targetPos(Pos.TOP_CENTER);
		for (CardStack cardStack : cardStacks) {
			if (cardStack.getCards().isEmpty()) {
				continue;
			}
			SolitaireCard card = getLastCard(cardStack);
			clickOn(cardStack);
			for (CardStack stack : cardStacks) {
				if (SolitaireModel.isNotAscendingStackCompatible(stack, card)) {
					if (SolitaireModel.isCardNotCompatibleWithStack(stack, card)) {
						continue;
					}
				}
				drag(card, MouseButton.PRIMARY);
				moveTo(stack);
				drop();
				if (!cardStack.getCards().contains(card)) {
					if (cardStack.getCards().size() <= 1) {
						continue;
					}
					card = getLastCard(cardStack);
					clickOn(cardStack);
				}
			}
		}
		targetPos(Pos.CENTER);
	}

	private static SolitaireCard getLastCard(CardStack cardStack) {

		ObservableList<SolitaireCard> children = cardStack.getCards();
		Optional<SolitaireCard> findFirst = children.stream().filter(e -> e.isShown()).findFirst();
		return findFirst.orElseGet(() -> children.get(children.size() - 1));
	}

}