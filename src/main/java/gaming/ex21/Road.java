package gaming.ex21;

public class Road extends CatanResource {
    public Road() {
        super("catan/road.png");
		view.setFitHeight(Terrain.RADIUS);
    }

    public Road(final PlayerColor color) {
        this();
        setPlayer(color);
    }


}
