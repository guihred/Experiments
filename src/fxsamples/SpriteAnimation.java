package fxsamples;
import javafx.animation.Interpolator;
import javafx.animation.Transition;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

public class SpriteAnimation extends Transition {
	private int columns;
	private int count;
	private int height;
	private ImageView imageView;
	private int lastIndex;
	private int offsetX;
	private int offsetY;
	private int width;

	public SpriteAnimation(ImageView imageView, Duration duration) {
		this.imageView = imageView;
		setCycleDuration(duration);
		setInterpolator(Interpolator.LINEAR);
	}

	@Override
	protected void interpolate(double k) {
		final int index = Math.min((int) Math.floor(k * count), count - 1);
		if (index != lastIndex) {
			final int x = index % columns * width + offsetX;
			final int y = index / columns * height + offsetY;
			imageView.setViewport(new Rectangle2D(x, y, width, height));
			lastIndex = index;
		}
	}

	public SpriteAnimation setColumns(int columns) {
		this.columns = columns;
		return this;
	}

	public SpriteAnimation setCount(int count) {
		this.count = count;
		return this;
	}

	public SpriteAnimation setHeight(int height) {
		this.height = height;
		return this;
	}

	public SpriteAnimation setImageView(ImageView imageView) {
		this.imageView = imageView;
		return this;
	}

	public SpriteAnimation setLastIndex(int lastIndex) {
		this.lastIndex = lastIndex;
		return this;
	}

	public SpriteAnimation setOffsetX(int offsetX) {
		this.offsetX = offsetX;
		return this;
	}

	public SpriteAnimation setOffsetY(int offsetY) {
		this.offsetY = offsetY;
		return this;
	}

	public SpriteAnimation setWidth(int width) {
		this.width = width;
		return this;
	}
}