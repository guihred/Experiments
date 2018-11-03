package gaming.ex15;

import java.util.*;
import java.util.Map.Entry;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
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
	private final int id;
    private Map<Point3D, Rotate> rotations = new HashMap<>();

	public RubiksPiece(double size) {
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
		rec2.setRotate(90);
		// right face
		Box rec3 = new Box(size, size, 1);
		rec3.setMaterial(new PhongMaterial(Color.WHITE));
		rec3.setTranslateX(-1 * size);
        rec3.setTranslateY(-size / 2);
		rec3.setRotationAxis(Rotate.Y_AXIS);
		rec3.setRotate(90);
		// left face
		Box rec4 = new Box(size, size, 1);
		rec4.setMaterial(new PhongMaterial(Color.BLUE));
		rec4.setTranslateX(0);
        rec4.setTranslateY(-size / 2);
		rec4.setRotationAxis(Rotate.Y_AXIS);
		rec4.setRotate(90);
		// top face
		Box rec5 = new Box(size, size, 1);
		rec5.setMaterial(new PhongMaterial(Color.YELLOW));
        rec5.setTranslateX(-size / 2);
		rec5.setTranslateY(-1 * size);
		rec5.setRotationAxis(Rotate.X_AXIS);
		rec5.setRotate(90);
		// front face
		Box rec6 = new Box(size, size, 1);
		rec6.setMaterial(new PhongMaterial(Color.RED));
        rec6.setTranslateX(-size / 2);
        rec6.setTranslateY(-size / 2);
        rec6.setTranslateZ(-size / 2);

		getChildren().addAll(rec1, rec2, rec3, rec4, rec5, rec6);
		if (RubiksCubeLauncher.DEBUG) {
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
                    Rotate e = new Rotate(180);
                    e.setAxis(i % 3 == 0 ? Rotate.Y_AXIS : Rotate.X_AXIS);
                    text.getTransforms().add(e);
                }

				arrayList.add(text);
			}
			getChildren().addAll(arrayList);

		}
		
	}

	public void rotate(RubiksCubeFaces face, DoubleProperty angle, boolean clockwise) {
        Rotate rotate = rotations.get(face.getAxis());
		double angle2 = Math.ceil((rotate.getAngle() + 360) % 360 / 90) * 90;
        DoubleBinding add = clockwise ^ (face == RubiksCubeFaces.BACK || face == RubiksCubeFaces.FRONT)
                ? angle.add(angle2)
                : angle.multiply(-1).add(angle2);
		rotate.angleProperty().bind(add);

	}

	public void setPivot(RubiksPiece pivot) {
		if (rotations.isEmpty()) {
			RubiksCubeFaces[] values = RubiksCubeFaces.values();
			for (RubiksCubeFaces face : values) {
				Rotate rotate = new Rotate(0, face.getAxis());
                rotate.setPivotX(pivot.getTranslateX() - getTranslateX() - RubiksCubeLauncher.RUBIKS_CUBE_SIZE / 2.0);
                rotate.setPivotY(pivot.getTranslateY() - getTranslateY() - RubiksCubeLauncher.RUBIKS_CUBE_SIZE / 2.0);
				rotate.setPivotZ(pivot.getTranslateZ() - getTranslateZ());
				getTransforms().add(rotate);
                rotations.put(face.getAxis(), rotate);
			}
		}
        getTransforms().add(new Rotate(0));
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
