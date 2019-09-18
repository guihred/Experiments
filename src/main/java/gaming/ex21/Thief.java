package gaming.ex21;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Thief extends CatanResource {

    public Thief() {
        super("thief.png");
        view.setFitWidth(CatanResource.RADIUS / 2.);
    }

    public static void removeHalfOfCards(Map<PlayerColor, List<CatanCard>> cards) {
        for (List<CatanCard> list : cards.values()) {
            List<CatanCard> cardss = list.parallelStream().filter(e -> e.getDevelopment() == null)
                .collect(Collectors.toList());
            Collections.shuffle(cardss);
            if (cardss.size() > 7) {
                int size = cardss.size();
                Collections.shuffle(cardss);
                for (int i = 0; i < size / 2; i++) {
                    list.remove(cardss.remove(0));
                }
            }
        }
    }

}
