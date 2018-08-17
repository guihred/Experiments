package gaming.ex16;

import java.util.Objects;

public class MadPonto {
    private float x;
    private float y;
    private MadCell cell;

    public MadPonto(float x, float y, MadCell c) {
        this.x = x;
        this.y = y;
        cell = c;
    }




    public MadPonto add(MadPonto vector) {
        return new MadPonto(x + vector.x, y + vector.y, cell);
    }

    public float cross(MadPonto vector) {
        return y * vector.x - x * vector.y;
    }

    public float dot(MadPonto vector) {
        return x * vector.x + y * vector.y;
    }

    @Override
    public boolean equals(Object obj) {

        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    public float mag() {
        return (float) Math.sqrt(x * x + y * y);
    }

    public MadPonto mult(float scalar) {
        return new MadPonto(x * scalar, y * scalar, cell);
    }

    public MadPonto sub(MadPonto vector) {
        return new MadPonto(x - vector.x, y - vector.y, cell);
    }

    @Override
    public String toString() {
        return "(" + x + "," + y + ")";
    }



    public float getX() {
        return x;
    }



    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }




    public MadCell getCell() {
        return cell;
    }




    public void setCell(MadCell cell) {
        this.cell = cell;
    }
}
