package gaming.ex21;

import javafx.beans.NamedArg;

public class Road extends CatanResource {
    public Road(@NamedArg("player") PlayerColor color) {
        this();
        setPlayer(color);
    }

    private  Road() {
        super("road.png");
		view.setPreserveRatio(false);
        view.setFitHeight(Terrain.RADIUS * 4. / 5);
		view.setFitWidth(10);
    }


}
