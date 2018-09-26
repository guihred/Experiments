package gaming.ex16;

public class MadEdge implements Comparable<MadEdge> {

    private MadCell source;
    private MadCell target;
    private boolean main;

    public MadEdge(MadCell source, MadCell target) {
        this.source = source;
        this.target = target;
    }

    @Override
    public int compareTo(MadEdge o) {
        return 0;
    }

    public MadCell getSource() {
        return source;
    }

    public MadCell getTarget() {
        return target;
    }

    public boolean isMain() {
        return main;
    }

    public void setMain(boolean main) {
        this.main = main;
    }

    public void setSource(MadCell source) {
        this.source = source;
    }

    public void setTarget(MadCell target) {
        this.target = target;
    }

}
