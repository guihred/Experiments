package gaming.ex16;

public class MadLinha {

    private final MadPonto a;
    private final MadPonto b;

    public MadLinha(MadPonto a, MadPonto b) {
        this.a = a;
        this.b = b;
    }

    public MadPonto getA() {
        return a;
    }

    public MadPonto getB() {
        return b;
    }

}
