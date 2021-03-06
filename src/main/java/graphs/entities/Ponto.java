package graphs.entities;

import java.util.Objects;

public class Ponto {
	private double x;
	private double y;
	private Cell c;


	public Ponto(double x, double y, Cell c) {
        this.x = x;
		this.y = y;
		this.c = c;
	}

	public Ponto add(Ponto vector) {
		return new Ponto(x + vector.x, y + vector.y, c);
	}

	public double cross(Ponto vector) {
		return y * vector.x - x * vector.y;
	}
	public double dot(Ponto vector) {
		return x * vector.x + y * vector.y;
	}

	@Override
	public boolean equals(Object obj) {

		return super.equals(obj);
	}

	public Cell getC() {
		return c;
	}

	public double getX() {
        return x;
    }

	public double getY() {
        return y;
    }
	@Override
	public int hashCode() {
		return Objects.hash(x, y);
	}

	public double mag() {
		return Math.sqrt(x * x + y * y);
	}

	public Ponto mult(double scalar) {
		return new Ponto(x * scalar, y * scalar, c);
	}

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public Ponto sub(Ponto vector) {
		return new Ponto(x - vector.x, y - vector.y, c);
	}

    @Override
	public String toString() {
		return "(" + x + "," + y + ")";
	}

}