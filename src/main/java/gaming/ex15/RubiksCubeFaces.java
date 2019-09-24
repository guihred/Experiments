package gaming.ex15;

import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.geometry.Point3D;
import javafx.scene.transform.Rotate;

enum RubiksCubeFaces {
	UP((cube, i, j) -> cube[i][0][j], (cube, i, j, p) -> cube[i][0][j] = p, Rotate.Y_AXIS),
	DOWN((cube, i, j) -> cube[i][2][j], (cube, i, j, p) -> cube[i][2][j] = p, Rotate.Y_AXIS),
	LEFT((cube, i, j) -> cube[0][i][j], (cube, i, j, p) -> cube[0][i][j] = p, Rotate.X_AXIS),
	RIGHT((cube, i, j) -> cube[2][i][j], (cube, i, j, p) -> cube[2][i][j] = p, Rotate.X_AXIS),
	FRONT((cube, i, j) -> cube[i][j][0], (cube, i, j, p) -> cube[i][j][0] = p, Rotate.Z_AXIS),
	BACK((cube, i, j) -> cube[i][j][2], (cube, i, j, p) -> cube[i][j][2] = p, Rotate.Z_AXIS);
	private final RubiksGetFaceFunction getFunc;
	private final Point3D axis;
	private final RubiksSetFaceFunction set;

	RubiksCubeFaces(RubiksGetFaceFunction get, RubiksSetFaceFunction set, Point3D axis) {
		getFunc = get;
		this.set = set;
		this.axis = axis;
	}

	public RubiksPiece get(RubiksPiece[][][] cube, int i, int j) {
		return getFunc.apply(cube, i, j);
	}

	public Point3D getAxis() {
		return axis;
	}

	public RubiksGetFaceFunction getFunc() {
		return getFunc;
	}

	public void rotate(RubiksPiece e, DoubleProperty angle, boolean clockwise) {
		Rotate rotate = e.getRotations().get(getAxis());
		double angle2 = Math.ceil((rotate.getAngle() + 360) % 360 / 90) * 90;
		DoubleBinding add = clockwise ^ (this == RubiksCubeFaces.BACK || this == RubiksCubeFaces.FRONT)
				? angle.add(angle2)
				: angle.multiply(-1).add(angle2);
		rotate.angleProperty().bind(add);

	}

	public void set(RubiksPiece[][][] cube, int i, int j, RubiksPiece newPiece) {
		set.apply(cube, i, j, newPiece);
	}
}