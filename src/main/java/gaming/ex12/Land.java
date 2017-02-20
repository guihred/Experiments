package gaming.ex12;

import gaming.ex12.resources.ImageResource;

import javafx.scene.canvas.GraphicsContext;

public class Land extends Player {
	public static final int INITIAL_Y = 350;
	public static final int INITIAL_X = 100;
	private int[][] shape = new int[][] { { 34, 111, 98 }, { 21, 111, 98 } };

	public Land() {
		super(ImageResource.TILES);
		setPositionY(INITIAL_Y);
		setPositionX(INITIAL_X);
	}

	@Override
	public void render(GraphicsContext gc) {
		for (int i = 0; i < shape.length; i++) {
			for (int j = 0; j < shape[i].length; j++) {
				final int x = shape[i][j] % picture.getColumns() * picture.getWidth();
				final int y = shape[i][j] / picture.getColumns() * picture.getHeight();
				gc.drawImage(picture.asImage(), x, y, picture.getWidth(), picture.getHeight(), getPositionX() + j * picture.getScaledWidth() - j * 2,
						getPositionY() + i * picture.getScaledHeight() - i * 2, picture.getScaledWidth(), picture.getScaledHeight());
			}
		}

	}

}
