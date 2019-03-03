package gaming.ex21;

public class Village extends CatanResource {
    public Village() {
        super("catan/village.png");
		view.setFitWidth(Terrain.RADIUS);
    }

    public Village(final PlayerColor color) {
        this();
        setPlayer(color);
    }

}
