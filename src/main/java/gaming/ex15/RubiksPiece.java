package gaming.ex15;

import java.util.*;
import java.util.Map.Entry;
import javafx.collections.ObservableList;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;

public class RubiksPiece extends Group {
	private static int count;
	public static final int RUBIKS_CUBE_SIZE = 50;
	public static final boolean DEBUG = true;
	private final int id;
	private Map<Point3D, Rotate> rotations = new HashMap<>();

	public RubiksPiece() {
		this(RUBIKS_CUBE_SIZE);
	}

	private RubiksPiece(double size) {
		id = ++count;

		// back face
		Box rec1 = new Box(size, size, 1);
		rec1.setMaterial(new PhongMaterial(Color.GREEN));
		rec1.setTranslateX(-size / 2);
		rec1.setTranslateY(-size / 2);
		rec1.setTranslateZ(size / 2);
		// bottom face
		Box rec2 = new Box(size, size, 1);
		rec2.setMaterial(new PhongMaterial(Color.ORANGERED));
		rec2.setTranslateX(-size / 2);
		rec2.setTranslateY(0);
		rec2.setRotationAxis(Rotate.X_AXIS);
        final int straightAngle = 90;
        rec2.setRotate(straightAngle);
		// right face
		Box rec3 = new Box(size, size, 1);
		rec3.setMaterial(new PhongMaterial(Color.WHITE));
		rec3.setTranslateX(-1 * size);
		rec3.setTranslateY(-size / 2);
		rec3.setRotationAxis(Rotate.Y_AXIS);
        rec3.setRotate(straightAngle);
		// left face
		Box rec4 = new Box(size, size, 1);
		rec4.setMaterial(new PhongMaterial(Color.BLUE));
		rec4.setTranslateX(0);
		rec4.setTranslateY(-size / 2);
		rec4.setRotationAxis(Rotate.Y_AXIS);
        rec4.setRotate(straightAngle);
		// top face
		Box rec5 = new Box(size, size, 1);
		rec5.setMaterial(new PhongMaterial(Color.YELLOW));
		rec5.setTranslateX(-size / 2);
		rec5.setTranslateY(-1 * size);
		rec5.setRotationAxis(Rotate.X_AXIS);
        rec5.setRotate(straightAngle);
		// front face
		Box rec6 = new Box(size, size, 1);
		rec6.setMaterial(new PhongMaterial(Color.RED));
		rec6.setTranslateX(-size / 2);
		rec6.setTranslateY(-size / 2);
		rec6.setTranslateZ(-size / 2);

		getChildren().addAll(rec1, rec2, rec3, rec4, rec5, rec6);
		if (RubiksPiece.DEBUG) {
			ObservableList<Node> children2 = getChildren();
			List<Numbers3D> arrayList = new ArrayList<>();
			for (int i = 0; i < children2.size(); i++) {
				Numbers3D text = new Numbers3D(id);
				Node node = children2.get(i);
				text.setTranslateX(node.getTranslateX());
				text.setTranslateY(node.getTranslateY());
				text.setTranslateZ(node.getTranslateZ());
				text.setRotationAxis(node.getRotationAxis());
				text.setRotate(node.getRotate());
				if (i % 3 == 0 || i == 4) {
                    Rotate e = new Rotate(straightAngle * 2);
					e.setAxis(i % 3 == 0 ? Rotate.Y_AXIS : Rotate.X_AXIS);
					text.getTransforms().add(e);
				}

				arrayList.add(text);
			}
			getChildren().addAll(arrayList);
		}
	}

	public Map<Point3D, Rotate> getRotations() {
		return rotations;
	}


	public void setRotations(Map<Point3D, Rotate> rotations) {
		this.rotations = rotations;
	}

	@Override
	public String toString() {
		return String.format("%02d", id);
	}

	public void unbindAngle() {
		Transform concatenation = getTransforms().get(getTransforms().size() - 1);

		List<Entry<Point3D, Rotate>> entrySet = new ArrayList<>(rotations.entrySet());
		entrySet.sort(Comparator.comparing((Entry<Point3D, Rotate> e) -> !e.getValue().angleProperty().isBound()));
		for (Entry<Point3D, Rotate> entry : entrySet) {
			Rotate r = entry.getValue();
			concatenation = r.createConcatenation(concatenation);
			r.angleProperty().unbind();
			r.setAngle(0);
		}
		getTransforms().set(getTransforms().size() - 1, concatenation);

	}
}
