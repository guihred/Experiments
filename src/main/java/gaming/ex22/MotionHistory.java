package gaming.ex22;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

class MotionHistory {
    protected final List<FreeCellCard> cards = new ArrayList<>();
    protected FreeCellStack originStack;
    protected FreeCellStack targetStack;

    MotionHistory(Collection<FreeCellCard> cards, FreeCellStack originStack, FreeCellStack targetStack) {
        this.originStack = originStack;
        this.targetStack = targetStack;
        this.cards.addAll(cards);
    }

    MotionHistory(FreeCellCard cards, FreeCellStack originStack, FreeCellStack targetStack) {
        this.originStack = originStack;
        this.targetStack = targetStack;
        this.cards.add(cards);
    }
}