package cubesystem;

import javafx.application.Application;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.shape.VertexFormat;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;

public class DeathStar extends Application {

    private static final int HALF_SPHERE_ANGLE = 180;
    // the bigger the higher resolution
	private static final int DIVISION = 200;
	// radius of the sphere
    private static final float RADIUS = 300;
    private Point3D sphere = new Point3D(-RADIUS, 0, -RADIUS * 3 / 2);

	@Override
	public void start(Stage primaryStage) throws Exception {
        final TriangleMesh triangleMesh = createMesh(DIVISION, RADIUS, sphere);
		MeshView mesh = new MeshView(triangleMesh);

        mesh.setTranslateY(RADIUS);
        mesh.setTranslateX(RADIUS);
		mesh.setRotationAxis(Rotate.Y_AXIS);
		Scene scene = new Scene(new Group(mesh));
		// uncomment if you want to move the other sphere

		scene.setOnKeyPressed(e -> handleKeyPressed(mesh, e));

		primaryStage.setScene(scene);
		primaryStage.show();
	}

	private void handleKeyPressed(MeshView a, KeyEvent e) {
		KeyCode code = e.getCode();
		switch (code) {
		case UP:
			sphere = sphere.add(0, -10, 0);
			break;
		case DOWN:
			sphere = sphere.add(0, 10, 0);
			break;
		case LEFT:
			sphere = sphere.add(-10, 0, 0);
			break;
		case RIGHT:
			sphere = sphere.add(10, 0, 0);
			break;
		case W:
			sphere = sphere.add(0, 0, 10);
			break;
		case S:
			sphere = sphere.add(0, 0, -10);
			break;
		default:
			return;
		}
        a.setMesh(createMesh(DIVISION, RADIUS, sphere));
	}

	public static void main(String[] args) {

		launch(args);
	}

	private static void checkDistance(final float radius, final Point3D centerOtherSphere, float[] points, int pPos) {
        Rotate rotate = new Rotate(HALF_SPHERE_ANGLE, centerOtherSphere);
		final Point3D point3D = new Point3D(points[pPos + 0], points[pPos + 1], points[pPos + 2]);
		double distance = centerOtherSphere.distance(point3D);
		if (distance <= radius) {
			Point3D subtract = centerOtherSphere.subtract(point3D);
			Point3D transform = rotate.transform(subtract);
			points[pPos + 0] = (float) transform.getX();
			points[pPos + 1] = (float) transform.getY();
			points[pPos + 2] = (float) transform.getZ();
		}
	}

	private static TriangleMesh createMesh(final int division, final float radius, final Point3D centerOtherSphere) {
		final int div2 = division / 2;
		final int nPoints = division * (div2 - 1) + 2;
		final int nTPoints = (division + 1) * (div2 - 1) + division * 2;
		final float rDiv = 1.F / division;
        final float[] points = new float[nPoints * 3];
        final float[] tPoints = new float[nTPoints * 2];
        int pPos = 0;
        int tPos = 0;
		for (int y = 0; y < div2 - 1; ++y) {
            int m = div2 / 2;
            float va = rDiv * (y + 1 - m) * 2 * (float) Math.PI;
			float sinVal = (float) Math.sin(va);
			float cosVal = (float) Math.cos(va);

            float ty = 1F / 2F + sinVal / 2F;
			for (int i = 0; i < division; ++i) {
				double a = rDiv * i * 2 * (float) Math.PI;
				float hSin = (float) Math.sin(a);
				float hCos = (float) Math.cos(a);
				points[pPos + 0] = hSin * cosVal * radius;
				points[pPos + 2] = hCos * cosVal * radius;
				points[pPos + 1] = sinVal * radius;

				checkDistance(radius, centerOtherSphere, points, pPos);
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
		points[pPos + 1] = -radius;
		points[pPos + 2] = 0;
		checkDistance(radius, centerOtherSphere, points, pPos);
		points[pPos + 3] = 0;
		points[pPos + 4] = radius;
		points[pPos + 5] = 0;
		checkDistance(radius, centerOtherSphere, points, pPos + 3);

		float textureDelta = 1.F / 256;
		for (int i = 0; i < division; ++i) {
            tPoints[tPos + 0] = rDiv * (1 / 2F + i);
			tPoints[tPos + 1] = textureDelta;
			tPos += 2;
		}

		for (int i = 0; i < division; ++i) {
            tPoints[tPos + 0] = rDiv * (1 / 2F + i);
			tPoints[tPos + 1] = 1 - textureDelta;
			tPos += 2;
		}
        //Faces for every sphere can be reused
        int[] faces = GolfBall.createFaces(division);
        TriangleMesh m = new TriangleMesh(VertexFormat.POINT_TEXCOORD);
		m.getPoints().setAll(points);
		m.getTexCoords().setAll(tPoints);
		m.getFaces().setAll(faces);
		return m;
	}

}
