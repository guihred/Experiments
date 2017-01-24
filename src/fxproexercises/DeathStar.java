package fxproexercises;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import javafx.util.Duration;
public class DeathStar extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        float radius = 100;
        Point3D outraEsfera = new Point3D(-radius, -radius, radius);
        final TriangleMesh triangleMesh = createMesh(100, radius, outraEsfera);
        MeshView a = new MeshView(triangleMesh)
        ;

        a.setTranslateY(100);
        a.setTranslateX(100);
        a.setRotationAxis(Rotate.Y_AXIS);
        final Timeline timeline = new Timeline(
                new KeyFrame(
                        Duration.ZERO, new KeyValue(a.rotateProperty(), 0)),
                new KeyFrame(Duration.seconds(5), new KeyValue(a.rotateProperty(), 360))
        );

        timeline.setCycleCount(-1);
        timeline.play();
        primaryStage.setScene(new Scene(new Group(a)));
        primaryStage.show();
    }

    static TriangleMesh createMesh(final int div, final float r, final Point3D outraEsfera) {

        final int div2 = div / 2;

        final int nPoints = div * (div2 - 1) + 2;
        final int nTPoints = (div + 1) * (div2 - 1) + div * 2;
        final int nFaces = div * (div2 - 2) * 2 + div * 2;

        final float rDiv = 1.f / div;

        float points[] = new float[nPoints * 3];
        float tPoints[] = new float[nTPoints * 2];
        int faces[] = new int[nFaces * 6];

        int pPos = 0, tPos = 0;

        for (int y = 0; y < div2 - 1; ++y) {
            float va = rDiv * (y + 1 - div2 / 2) * 2 * (float) Math.PI;
            float sin_va = (float) Math.sin(va);
            float cos_va = (float) Math.cos(va);

            float ty = 0.5f + sin_va * 0.5f;
            for (int i = 0; i < div; ++i) {
                double a = rDiv * i * 2 * (float) Math.PI;
                float hSin = (float) Math.sin(a);
                float hCos = (float) Math.cos(a);
                points[pPos + 0] = hSin * cos_va * r;
                points[pPos + 2] = hCos * cos_va * r;
                points[pPos + 1] = sin_va * r;

                final Point3D point3D = new Point3D(points[pPos + 0], points[pPos + 1], points[pPos + 2]);
                double distance = outraEsfera.distance(point3D);
                if (distance <= r / 2) {
                    Point3D subtract = outraEsfera.subtract(point3D);
                    points[pPos + 0] = (float) subtract.getX();
                    points[pPos + 2] = (float) subtract.getZ();
                    points[pPos + 1] = (float) subtract.getY();

                }
                tPoints[tPos + 0] = 1 - rDiv * i;
                tPoints[tPos + 1] = ty;
                pPos += 3;
                tPos += 2;
            }
            tPoints[tPos + 0] = 0;
            tPoints[tPos + 1] = ty;
            tPos += 2;
        }

        points[pPos + 0] = 0;
        points[pPos + 1] = -r;
        points[pPos + 2] = 0;
        points[pPos + 3] = 0;
        points[pPos + 4] = r;
        points[pPos + 5] = 0;
        pPos += 6;

        int pS = (div2 - 1) * div;

        float textureDelta = 1.f / 256;
        for (int i = 0; i < div; ++i) {
            tPoints[tPos + 0] = rDiv * (0.5f + i);
            tPoints[tPos + 1] = textureDelta;
            tPos += 2;
        }

        for (int i = 0; i < div; ++i) {
            tPoints[tPos + 0] = rDiv * (0.5f + i);
            tPoints[tPos + 1] = 1 - textureDelta;
            tPos += 2;
        }

        int fIndex = 0;
        for (int y = 0; y < div2 - 2; ++y) {
            for (int x = 0; x < div; ++x) {
                int p0 = y * div + x;
                int p1 = p0 + 1;
                int p2 = p0 + div;
                int p3 = p1 + div;

                int t0 = p0 + y;
                int t1 = t0 + 1;
                int t2 = t0 + (div + 1);
                int t3 = t1 + (div + 1);

                // add p0, p1, p2
                faces[fIndex + 0] = p0;
                faces[fIndex + 1] = t0;
                faces[fIndex + 2] = p1 % div == 0 ? p1 - div : p1;
                faces[fIndex + 3] = t1;
                faces[fIndex + 4] = p2;
                faces[fIndex + 5] = t2;
                fIndex += 6;

                // add p3, p2, p1
                faces[fIndex + 0] = p3 % div == 0 ? p3 - div : p3;
                faces[fIndex + 1] = t3;
                faces[fIndex + 2] = p2;
                faces[fIndex + 3] = t2;
                faces[fIndex + 4] = p1 % div == 0 ? p1 - div : p1;
                faces[fIndex + 5] = t1;
                fIndex += 6;
            }
        }

        int p0 = pS;
        int tB = (div2 - 1) * (div + 1);
        for (int x = 0; x < div; ++x) {
            int p2 = x, p1 = x + 1, t0 = tB + x;
            faces[fIndex + 0] = p0;
            faces[fIndex + 1] = t0;
            faces[fIndex + 2] = p1 == div ? 0 : p1;
            faces[fIndex + 3] = p1;
            faces[fIndex + 4] = p2;
            faces[fIndex + 5] = p2;
            fIndex += 6;
        }

        p0 = p0 + 1;
        tB = tB + div;
        int pB = (div2 - 2) * div;

        for (int x = 0; x < div; ++x) {
            int p1 = pB + x, p2 = pB + x + 1, t0 = tB + x;
            int t1 = (div2 - 2) * (div + 1) + x, t2 = t1 + 1;
            faces[fIndex + 0] = p0;
            faces[fIndex + 1] = t0;
            faces[fIndex + 2] = p1;
            faces[fIndex + 3] = t1;
            faces[fIndex + 4] = p2 % div == 0 ? p2 - div : p2;
            faces[fIndex + 5] = t2;
            fIndex += 6;
        }

        TriangleMesh m = new TriangleMesh();
        m.getPoints().setAll(points);
        m.getTexCoords().setAll(tPoints);
        m.getFaces().setAll(faces);

        return m;
    }
    public static void main(String[] args) {

        launch(args);
    }

}
