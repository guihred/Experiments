package gaming.ex21;

public class City extends CatanResource {
    public City() {
        super("city.png");
		view.setFitWidth(Terrain.RADIUS);
    }

    public City(final PlayerColor color) {
        this();
        setPlayer(color);
    }

}
