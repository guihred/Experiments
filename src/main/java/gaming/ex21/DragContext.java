package gaming.ex21;

class DragContext {
    private double x;
    private double y;
    private CatanResource element;
    private SettlePoint point;
    private EdgeCatan edge;
    private Terrain terrain;

    public void edgeFadeOut(boolean isEnabled) {
        if (edge != null) {
            edge.fadeOut(isEnabled);
            edge = null;
        }
    }

    public EdgeCatan getEdge() {
        return edge;
    }

    public CatanResource getElement() {
        return element;
    }

    public SettlePoint getPoint() {
        return point;
    }

    public Terrain getTerrain() {
        return terrain;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public void pointFadeOut() {
        if (point != null) {
            point.toggleFade(1);
            point = null;
        }
    }

    public EdgeCatan setEdge(EdgeCatan edge) {
        this.edge = edge;
        return edge;
    }

    public void setElement(CatanResource element) {
        this.element = element;
    }

    public SettlePoint setPoint(SettlePoint point) {
        this.point = point;
        return point;
    }

    public Terrain setTerrain(Terrain terrain) {
        this.terrain = terrain;
        return terrain;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void toggleTerrain(int r) {
        if (terrain != null) {
            terrain.toggleFade(r);
            terrain = null;
        }
    }
}