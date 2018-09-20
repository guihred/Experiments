package gaming.ex12;

import gaming.ex12.Player.Direction;
import javafx.scene.canvas.GraphicsContext;

class RunAnimation {
	private ImageResource image;
	private int lastIndex;
	private int offsetX;
	private int offsetY;
	private int columns;
	private int count;

	public RunAnimation(ImageResource image, int index, int count) {
		this.image = image;
		this.count = count;
		columns = count;
		offsetX = index % image.getColumns() * image.getWidth();
		offsetY = index / image.getColumns() * image.getHeight();
	}
	public RunAnimation(ImageResource image) {
		this.image = image;
		columns = image.getColumns();
		count = image.getCount();
	}

	public void update(double k) {
		int floor = (int) Math.round(k * count);
		final int index = Math.min(floor, count - 1);
		if (index != lastIndex) {
			lastIndex = (lastIndex + 1) % count;
		}

	}

	public void render(GraphicsContext gc, Player player) {
		int index = lastIndex;
		final int x = index % columns * image.getWidth() + offsetX;
		final int y = index / columns * image.getHeight() + offsetY;

		double scaledWidth = image.getScaledWidth();
		double positionX = player.getPositionX();
		if (player.direction == Direction.LEFT) {
			positionX = positionX + scaledWidth;
			scaledWidth = -scaledWidth;
		}
		gc.drawImage(image.asImage(), x, y, image.getWidth(), image.getHeight(), positionX, player.getPositionY(), scaledWidth,
				image.getScaledHeight());
	}
}