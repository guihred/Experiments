package gaming.ex21;

public class Village extends CatanResource {
    public Village() {
        super("catan/village.png");
        view.setFitWidth(Terrain.RADIUS / 2.);
    }

    public Village(PlayerColor color) {
        this();
        setPlayer(color);
    }

}
