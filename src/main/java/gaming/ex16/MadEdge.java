package gaming.ex16;

import java.util.Objects;

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

    public boolean edgeHasCell(MadCell cellB, MadCell cellC) {
        return source.equals(cellC) && target.equals(cellB) || source.equals(cellB) && target.equals(cellC);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!getClass().isInstance(obj)) {
            return false;
        }
        MadEdge other = (MadEdge) obj;
        return Objects.equals(source, other.source) && Objects.equals(target, other.target);
    }

    public MadCell getSource() {
        return source;
    }

    public MadCell getTarget() {
        return target;
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, target);
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
