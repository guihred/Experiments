package gaming.ex21;

import javafx.beans.NamedArg;

public class Village extends CatanResource {
    public Village(@NamedArg("player") final PlayerColor color) {
        this();
        setPlayer(color);
    }

    private Village() {
        super("village.png");
		view.setFitWidth(CatanResource.RADIUS);
    }

}
