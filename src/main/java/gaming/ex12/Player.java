package gaming.ex12;

import gaming.ex12.resources.ImageResource;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;

public class Player {

	public static final double MAX_VELOCITY_X = 15;
	private double positionX;
	private double positionY;
	protected double velocityX;
	protected double velocityY;
	protected double colisionX;
	protected double colisionY;
	protected double colisionWidth;
	protected double colisionHeight;
	protected double accelerationX;
	protected double accelerationY;
	protected boolean debug;
	protected ImageResource picture;
	protected Direction direction = Direction.RIGHT;

	enum Direction {
		RIGHT,
		LEFT
	}

	public Player(ImageResource picture) {
		this.picture = picture;
	}

	public void verticalCollision(Player player) {
		if (player instanceof Ground) {
			accelerationY = 0;
			velocityY = 0;
		}
	}

	public void update(double time) {
		if (Math.abs(velocityX) < MAX_VELOCITY_X) {
			velocityX += accelerationX * time;
		}
		velocityY += accelerationY * time;

		setPositionX(getPositionX() + velocityX * time);
		setPositionY(getPositionY() + velocityY * time);
	}

	public boolean isClose(Player player) {
		double a = (getPositionX() + picture.getScaledWidth() - colisionWidth) / 2;
		double b = (player.getPositionX() + player.picture.getScaledWidth() - player.colisionWidth) / 2;
		return Math.abs(a - b) < 50;
	}

	public void render(GraphicsContext gc) {
		if (debug) {
			gc.fillRect(getPositionX() + colisionX, getPositionY() + colisionY, picture.getScaledWidth() - colisionX - colisionWidth, picture.getScaledHeight()
					- colisionY - colisionHeight);
		}
		gc.drawImage(picture.asImage(), getPositionX(), getPositionY(), picture.getScaledWidth(), picture.getScaledHeight());
	}

	public Rectangle2D getBoundary() {

		return new Rectangle2D(getPositionX() + colisionX, getPositionY() + colisionY, picture.getScaledWidth() - colisionX
				- colisionWidth,
				picture.getScaledHeight() - colisionY - colisionHeight);

	}

	public boolean intersects(Player s) {
		return s.getBoundary().intersects(getBoundary());
	}

	public double getPositionX() {
		return positionX;
	}

	public final void setPositionX(double positionX) {
		this.positionX = positionX;
	}

	public double getPositionY() {
		return positionY;
	}

	public final void setPositionY(double positionY) {
		this.positionY = positionY;
	}

}