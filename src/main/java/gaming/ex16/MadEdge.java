package gaming.ex16;

public class MadEdge implements Comparable<MadEdge> {

    private MadCell source;
    private MadCell target;
    private boolean main;

    public MadEdge(MadCell source, MadCell target) {
        this.source = source;
        this.target = target;
    }

    public MadCell getSource() {
        return source;
    }

    public void setSource(MadCell source) {
        this.source = source;
    }

    public MadCell getTarget() {
        return target;
    }

    public void setTarget(MadCell target) {
        this.target = target;
    }

    public boolean isMain() {
        return main;
    }

    public void setMain(boolean main) {
        this.main = main;
    }

    @Override
    public int compareTo(MadEdge o) {
        return 0;
    }

}
