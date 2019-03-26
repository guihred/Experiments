package gaming.ex21;

public class Road extends CatanResource {
    public Road() {
        super("road.png");
		view.setPreserveRatio(false);
        view.setFitHeight(Terrain.RADIUS * 4. / 5);
		view.setFitWidth(10);
    }

    public Road(final PlayerColor color) {
        this();
        setPlayer(color);
    }


}
