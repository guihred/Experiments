package gaming.ex05;

public enum TetrisPiece {
    I(new int[][]{
        {1, 1, 1, 1}
    }),
    J(new int[][]{
        {0, 1},
        {0, 1},
        {1, 1}
    }),
    L(new int[][]{
        {1, 0},
        {1, 0},
        {1, 1}
    }),
    O(new int[][]{
        {1, 1},
        {1, 1}}),
    S(new int[][]{
        {1, 0},
        {1, 1},
        {0, 1}
    }),
    T(new int[][]{
        {0, 1},
        {1, 1},
        {0, 1}
    }),
    Z(new int[][]{
        {0, 1},
        {1, 1},
        {1, 0}
    });
	private final int[][] map;

    TetrisPiece(int[][] map) {
        this.map = map;
    }

	public int[][] getMap() {
		return map;
	}

}