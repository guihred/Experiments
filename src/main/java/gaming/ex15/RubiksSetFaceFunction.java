package gaming.ex15;

@FunctionalInterface
interface RubiksSetFaceFunction {
	void apply(RubiksPiece[][][] cube, int i, int j, RubiksPiece newPiece);
}