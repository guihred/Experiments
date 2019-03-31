package gaming.ex21;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

public class Thief extends CatanResource {

    private final Random random = new Random();

    public Thief() {
        super("thief.png");
        view.setFitWidth(Terrain.RADIUS / 2.);
    }

    public void removeHalfOfCards(Map<PlayerColor, List<CatanCard>> cards) {
        for (List<CatanCard> list : cards.values()) {
            List<CatanCard> cardss = list.parallelStream().filter(e -> e.getDevelopment() == null)
                .collect(Collectors.toList());
            if (cardss.size() > 7) {
                int size = cardss.size();
                for (int i = 0; i < size / 2; i++) {
                    list.remove(cardss.remove(random.nextInt(cardss.size())));
                }
            }
        }
    }

}
