package gaming.ex16;

public class MadCell {

    String id;
    float x, y;

    public MadCell(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public void relocate(double x, double y) {
        this.x = (float) x;
        this.y = (float) y;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setX(float x) {
        this.x = x;
    }

    @Override
    public String toString() {
        return "[" + id + "," + x + "," + y + "]";
    }

    public void setY(float y) {
        this.y = y;
    }

}
