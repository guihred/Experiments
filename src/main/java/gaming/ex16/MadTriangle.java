package gaming.ex16;

import exercism.MatrixSolver;
import java.util.Arrays;

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

    @Override
    public String toString() {
        return "MadTriangle [a=" + a + ", b=" + b + ", c=" + c + ", visited=" + visited + "]";
    }

    public MadPonto getC() {
        return c;
    }

    public MadPonto getNoneEdgeVertex(MadLinha edge) {
        if (a != edge.getA() && a != edge.getB()) {
            return a;
        } else if (b != edge.getA() && b != edge.getB()) {
            return b;
        } else if (c != edge.getA() && c != edge.getB()) {
            return c;
        }
        return null;
    }

    public boolean hasVertex(MadPonto vertex) {
        return a == vertex || b == vertex || c == vertex;
    }

    public boolean isNeighbour(MadLinha edge) {

        return (a == edge.getA() || b == edge.getA() || c == edge.getA()) && (a == edge.getB() || b == edge.getB() || c == edge.getB());
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

        float a13 = (a.getX() - point.getX()) * (a.getX() - point.getX()) + (a.getY() - point.getY()) * (a.getY() - point.getY());
        float a23 = (b.getX() - point.getX()) * (b.getX() - point.getX()) + (b.getY() - point.getY()) * (b.getY() - point.getY());
        float a33 = (c.getX() - point.getX()) * (c.getX() - point.getX()) + (c.getY() - point.getY()) * (c.getY() - point.getY());

        float det = a11 * a22 * a33 + a12 * a23 * a31 + a13 * a21 * a32 - a13 * a22 * a31 - a12 * a21 * a33
                - a11 * a23 * a32;

        if (isOrientedCCW()) {
            return det > 0.0D;
        }

        return det < 0.0D;
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

    class MadEdgeDistance implements Comparable<MadEdgeDistance> {

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

    }

    private static MadPonto computeClosestPoint(MadLinha edge, MadPonto point) {
        MadPonto ab = edge.getB().sub(edge.getA());
        float t = point.sub(edge.getA()).dot(ab) / ab.dot(ab);

        if (t < 0.0f) {
            t = 0.0f;
        } else if (t > 1.0D) {
            t = 1.0f;
        }

        return edge.getA().add(ab.mult(t));
    }

    private static boolean hasSameSign(float a1, float b1) {
        return Math.signum(a1) == Math.signum(b1);
    }

    public boolean isVisited() {
        return visited;
    }

    public void setVisited(boolean visited) {
        this.visited = visited;
    }

}
