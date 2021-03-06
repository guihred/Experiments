package gaming.ex16;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;
import utils.MatrixSolver;

public class MadTriangle {

    private MadPonto a;

    private MadPonto b;

    private MadPonto c;
    private boolean visited;

    public MadTriangle(MadPonto a, MadPonto b, MadPonto c) {
        this.a = a;
        this.b = b;
        this.c = c;
    }

    public double[] centerCircle() {
        float ay = a.getY();
        float ax = a.getX();
        float bx = b.getX();
        float by = b.getY();
        float cy = c.getY();
        float cx = c.getX();
        double[] coef2 = new double[] { -ay * ay - ax * ax + bx * bx + by * by,
                -ay * ay - ax * ax + cx * cx + cy * cy };
        double[][] matr = new double[][] { { 2 * (bx - ax), 2 * (by - ay) }, { 2 * (cx - ax), 2 * (cy - ay) }, };

        return MatrixSolver.solve(matr, coef2);
    }

    public boolean contains(MadPonto point) {
        float pab = point.sub(a).cross(b.sub(a));
        float pbc = point.sub(b).cross(c.sub(b));
        if (!hasSameSign(pab, pbc)) {
            return false;
        }
        float pca = point.sub(c).cross(a.sub(c));
        return hasSameSign(pab, pca);
    }

    public MadEdgeDistance findNearestEdge(MadPonto point) {
        MadEdgeDistance[] edges = new MadEdgeDistance[3];
        edges[0] = new MadEdgeDistance(new MadLinha(a, b),
                computeClosestPoint(new MadLinha(a, b), point).sub(point).mag());
        edges[1] = new MadEdgeDistance(new MadLinha(b, c),
                computeClosestPoint(new MadLinha(b, c), point).sub(point).mag());
        edges[2] = new MadEdgeDistance(new MadLinha(c, a),
                computeClosestPoint(new MadLinha(c, a), point).sub(point).mag());
        Arrays.sort(edges);
        return edges[0];
    }

    public MadPonto getA() {
        return a;
    }

    public MadPonto getB() {
        return b;
    }

    public MadPonto getC() {
        return c;
    }

    public MadPonto getNoneEdgeVertex(MadLinha edge) {
        if (!Objects.equals(a, edge.getA()) && !Objects.equals(a, edge.getB())) {
            return a;
        } else if (!Objects.equals(b, edge.getA()) && !Objects.equals(b, edge.getB())) {
            return b;
        } else if (!Objects.equals(c, edge.getA()) && !Objects.equals(c, edge.getB())) {
            return c;
        }
        return null;
    }

    public boolean hasVertex(MadPonto vertex) {
        return Objects.equals(a, vertex) || Objects.equals(b, vertex) || Objects.equals(c, vertex);
    }

    public boolean isNeighbour(MadLinha edge) {
        return Stream.of(a, b, c).anyMatch(edge.getA()::equals)
                && Stream.of(a, b, c).anyMatch(edge.getB()::equals);
    }

    public boolean isOrientedCCW() {
        float a11 = a.getX() - c.getX();
        float a21 = b.getX() - c.getX();

        float a12 = a.getY() - c.getY();
        float a22 = b.getY() - c.getY();

        float det = a11 * a22 - a12 * a21;

        return det > 0.0D;
    }

    public boolean isPointInCircumcircle(MadPonto point) {
        float a11 = a.getX() - point.getX();
        float a21 = b.getX() - point.getX();
        float a31 = c.getX() - point.getX();

        float a12 = a.getY() - point.getY();
        float a22 = b.getY() - point.getY();
        float a32 = c.getY() - point.getY();

        float a13 = (a.getX() - point.getX()) * (a.getX() - point.getX())
            + (a.getY() - point.getY()) * (a.getY() - point.getY());
        float a23 = (b.getX() - point.getX()) * (b.getX() - point.getX())
            + (b.getY() - point.getY()) * (b.getY() - point.getY());
        float a33 = (c.getX() - point.getX()) * (c.getX() - point.getX())
            + (c.getY() - point.getY()) * (c.getY() - point.getY());

        float det = a11 * a22 * a33 + a12 * a23 * a31 + a13 * a21 * a32 - a13 * a22 * a31 - a12 * a21 * a33
            - a11 * a23 * a32;

        if (isOrientedCCW()) {
            return det > 0.0D;
        }

        return det < 0.0D;
    }

    public boolean isVisited() {
        return visited;
    }

    public void setA(MadPonto a) {
        this.a = a;
    }

    public void setB(MadPonto b) {
        this.b = b;
    }

    public void setC(MadPonto c) {
        this.c = c;
    }

    public void setVisited(boolean visited) {
        this.visited = visited;
    }

    @Override
    public String toString() {
        return "MadTriangle [a=" + a + ", b=" + b + ", c=" + c + ", visited=" + visited + "]";
    }

    private static MadPonto computeClosestPoint(MadLinha edge, MadPonto point) {
        MadPonto ab = edge.getB().sub(edge.getA());
        float t = point.sub(edge.getA()).dot(ab) / ab.dot(ab);

        if (t < 0.F) {
            t = 0.F;
        } else if (t > 1.0D) {
            t = 1.F;
        }

        return edge.getA().add(ab.mult(t));
    }

    private static boolean hasSameSign(float a1, float b1) {
        return Math.signum(a1) == Math.signum(b1);
    }

}
