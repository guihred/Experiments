package gaming.ex16;

import java.util.Arrays;

import exercism.MatrixSolver;

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
        float ay = a.y;
        float ax = a.x;
        float bx = b.x;
        float by = b.y;
        float cy = c.y;
        float cx = c.x;
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
        if (a != edge.a && a != edge.b) {
            return a;
        } else if (b != edge.a && b != edge.b) {
            return b;
        } else if (c != edge.a && c != edge.b) {
            return c;
        }
        return null;
    }

    public boolean hasVertex(MadPonto vertex) {
        return a == vertex || b == vertex || c == vertex;
    }

    public boolean isNeighbour(MadLinha edge) {

        return (a == edge.a || b == edge.a || c == edge.a) && (a == edge.b || b == edge.b || c == edge.b);
    }

    public boolean isOrientedCCW() {
        float a11 = a.x - c.x;
        float a21 = b.x - c.x;

        float a12 = a.y - c.y;
        float a22 = b.y - c.y;

        float det = a11 * a22 - a12 * a21;

        return det > 0.0D;
    }

    public boolean isPointInCircumcircle(MadPonto point) {
        float a11 = a.x - point.x;
        float a21 = b.x - point.x;
        float a31 = c.x - point.x;

        float a12 = a.y - point.y;
        float a22 = b.y - point.y;
        float a32 = c.y - point.y;

        float a13 = (a.x - point.x) * (a.x - point.x) + (a.y - point.y) * (a.y - point.y);
        float a23 = (b.x - point.x) * (b.x - point.x) + (b.y - point.y) * (b.y - point.y);
        float a33 = (c.x - point.x) * (c.x - point.x) + (c.y - point.y) * (c.y - point.y);

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
        MadPonto ab = edge.b.sub(edge.a);
        float t = point.sub(edge.a).dot(ab) / ab.dot(ab);

        if (t < 0.0f) {
            t = 0.0f;
        } else if (t > 1.0D) {
            t = 1.0f;
        }

        return edge.a.add(ab.mult(t));
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
