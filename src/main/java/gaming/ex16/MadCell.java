package gaming.ex16;

public class MadCell {

    private int id;
    private float x;
    private float y;

    public MadCell(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

	public void relocate(float x1, float y1) {
		x = x1;
		y = y1;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    @Override
    public String toString() {
        return String.format("MadCell [%d, %f, %f]", getId(), getX(), getY());
    }

}
