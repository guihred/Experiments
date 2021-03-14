package gaming.ex12;

public class Tree extends Player {
    private static final int INITIAL_Y = 88;
    private static final int INITIAL_X = 250;
	public Tree() {
		super(ImageResource.TREE);
		setPositionY(INITIAL_Y);
		setPositionX(INITIAL_X);
        final int colisionSize = 140;
        colisionX = colisionSize;
        colisionWidth = colisionSize;
	}

}
