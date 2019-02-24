package gaming.ex21;

public class Road extends CatanResource {
    public Road() {
        super("catan/road.png");
        view.setFitHeight(Terrain.RADIUS * 3. / 5);
    }

    public Road(PlayerColor color) {
        this();
        setPlayer(color);
    }


}
