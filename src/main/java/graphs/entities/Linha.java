package graphs.entities;

public class Linha {

	private Ponto a;
	private Ponto b;

	public Linha(Ponto a, Ponto b) {
		this.a = a;
		this.b = b;
	}

    public Ponto getA() {
        return a;
    }

    public Ponto getB() {
        return b;
    }

    public void setA(Ponto a) {
        this.a = a;
    }

    public void setB(Ponto b) {
        this.b = b;
    }

}