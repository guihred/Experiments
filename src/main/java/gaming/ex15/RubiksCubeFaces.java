package gaming.ex15;

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


	private RubiksCubeFaces(RubiksGetFaceFunction get, RubiksSetFaceFunction set, Point3D axis) {
		getFunc = get;
		this.set = set;
		this.axis = axis;
	}

	public RubiksGetFaceFunction getFunc() {
		return getFunc;
	}

	public RubiksPiece get(RubiksPiece[][][] cube, int i, int j) {
		return getFunc.apply(cube, i, j);
	}


	public Point3D getAxis() {
		return axis;
	}

	public void set(RubiksPiece[][][] cube, int i, int j, RubiksPiece newPiece) {
		set.apply(cube, i, j, newPiece);
	}
}

@FunctionalInterface
interface RubiksGetFaceFunction {
	RubiksPiece apply(RubiksPiece[][][] cube, int i, int j);
}

@FunctionalInterface
interface RubiksSetFaceFunction {
	void apply(RubiksPiece[][][] cube, int i, int j, RubiksPiece newPiece);
}