package gaming.ex16;

import java.util.Objects;

public class MadEdgeDistance implements Comparable<MadEdgeDistance> {

    protected float distance;

    protected MadLinha edge;

    public MadEdgeDistance(MadLinha edge, float distance) {
        this.edge = edge;
        this.distance = distance;
    }
    @Override
    public int compareTo(MadEdgeDistance o) {
        return Double.compare(distance, o.distance);
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
        MadEdgeDistance other = (MadEdgeDistance) obj;
        return Float.floatToIntBits(distance) == Float.floatToIntBits(other.distance)
            && Objects.equals(edge, other.edge);
    }

    @Override
    public int hashCode() {
        return Objects.hash(distance, edge);
    }

}