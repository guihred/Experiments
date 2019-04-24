package gaming.ex12;

import javafx.scene.canvas.GraphicsContext;

public class Land extends Player {
    public static final int INITIAL_Y = 350;
    public static final int INITIAL_X = 100;
    private static final int[][] SHAPE = new int[][] { { 34, 111, 98 }, { 21, 111, 98 } };

    public Land() {
        super(ImageResource.TILES);
        setPositionY(INITIAL_Y);
        setPositionX(INITIAL_X);
    }

    @Override
    public void render(GraphicsContext gc) {
        for (int i = 0; i < SHAPE.length; i++) {
            for (int j = 0; j < SHAPE[i].length; j++) {
                final int x = SHAPE[i][j] % picture.getColumns() * picture.getWidth();
                final int y = SHAPE[i][j] / picture.getColumns() * picture.getHeight();
                gc.drawImage(picture.asImage(), x, y, picture.getWidth(), picture.getHeight(),
                    getPositionX() + j * picture.getScaledWidth() - j * 2,
                    getPositionY() + i * picture.getScaledHeight() - i * 2, picture.getScaledWidth(),
                    picture.getScaledHeight());
            }
        }

    }

}
