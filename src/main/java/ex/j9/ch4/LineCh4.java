package ex.j9.ch4;

public class LineCh4 extends ShapeCh4 {

    private PointCh4 from;
    private PointCh4 to;

    public LineCh4(PointCh4 from, PointCh4 to) {
        super(from);
        this.from = from;
        this.to = to;
    }

    @Override
    public PointCh4 getCenter() {
        return new PointCh4((from.x + to.x) / 2, (from.y + to.y) / 2);
    }

}