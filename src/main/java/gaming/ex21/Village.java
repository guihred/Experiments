package gaming.ex21;

public class Village extends CatanResource {
    public Village() {
        super("village.png");
		view.setFitWidth(Terrain.RADIUS);
    }

    public Village(final PlayerColor color) {
        this();
        setPlayer(color);
    }

}
