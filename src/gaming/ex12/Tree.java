package gaming.ex12;

import gaming.ex12.resources.ImageResource;

public class Tree extends Player {
	public static final int INITIAL_Y = 88;
	public static final int INITIAL_X = 250;
	public Tree() {
		super(ImageResource.TREE);
		setPositionY(INITIAL_Y);
		setPositionX(INITIAL_X);
		colisionX = 140;
		colisionWidth = 140;
	}

}
