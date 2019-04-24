package gaming.ex12;

import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;

public class Ground extends Player {
    private static final int GROUND_HEIGHT = 25;
    public static final int INITIAL_Y = 350;
	public static final int INITIAL_X = 0;
	private int width = 1;
	private int height = 1;

	public Ground() {
		super(ImageResource.GROUND);
		setPositionY(INITIAL_Y);
		setPositionX(INITIAL_X);
        colisionY = GROUND_HEIGHT;
	}

	public Ground(int width, int height) {
		super(ImageResource.GROUND);
		this.width = width;
		this.height = height;
		setPositionY(INITIAL_Y);
		setPositionX(INITIAL_X);
        colisionY = GROUND_HEIGHT;
	}

	@Override
	public Rectangle2D getBoundary() {
        return new Rectangle2D(getPositionX() + colisionX, getPositionY() + colisionY,
            width * picture.getScaledWidth() - colisionX - colisionWidth,
            height * picture.getScaledHeight() - colisionY - colisionHeight);
	}

	@Override
	public void render(GraphicsContext gc) {
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
                gc.drawImage(picture.asImage(), i % picture.getColumns() * picture.getWidth(), j * picture.getHeight(),
                    picture.getWidth(), picture.getHeight(), getPositionX() + i * picture.getScaledWidth(),
                    getPositionY() + j * picture.getScaledHeight(), picture.getScaledWidth(),
                    picture.getScaledHeight());
			}
		}
	}

}
