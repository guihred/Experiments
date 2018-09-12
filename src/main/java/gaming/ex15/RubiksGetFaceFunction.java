package gaming.ex15;

@FunctionalInterface
interface RubiksGetFaceFunction {
	RubiksPiece apply(RubiksPiece[][][] cube, int i, int j);
}