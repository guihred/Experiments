package gaming.ex12;

import javafx.scene.canvas.GraphicsContext;

public class Bird extends Player implements Enemy {
	public static final int INITIAL_Y = 100;
	public static final double INITIAL_X = 300;
	public static final int WANDER_WIDTH = 300;
	private RunAnimation flyingAnimation = new RunAnimation(ImageResource.BIRD);
	private RunAnimation fallingAnimation = new RunAnimation(ImageResource.BIRD, 20, 2);
	private RunAnimation fallenAnimation = new RunAnimation(ImageResource.BIRD, 6, 1);

	private BirdStatus status = BirdStatus.FLYING;

	public Bird() {
		super(ImageResource.BIRD);
		setPositionY(INITIAL_Y);
		setPositionX(INITIAL_X + INITIAL_X);
		colisionY = 55;
		colisionHeight = 65;
	}

	public void flyLeft() {
		velocityX = -2;
		direction = Direction.RIGHT;
	}

	public void stop() {
		velocityX = 0;
	}

	public void fall() {
		if (status != BirdStatus.FALLEN) {
			accelerationY = 1;
			status = BirdStatus.FALLING;
		}
	}

	public void flyRight() {
		direction = Direction.LEFT;
		velocityX = 2;
	}

	enum BirdStatus {
		FALLEN,
		FLYING,
		FALLING;
	}

	@Override
	public void update(double time) {
		super.update(time);
		if (status == BirdStatus.FLYING) {
			flyingAnimation.update(time * 40.0 / 50.0);
		} else if (status == BirdStatus.FALLING) {
			fallingAnimation.update(time * 40.0 / 50.0);
		} else {
			fallenAnimation.update(time * 40.0 / 50.0);
		}
		if (getPositionY() <= INITIAL_Y) {
			velocityY = 0;
			accelerationY = 0;
			status = BirdStatus.FLYING;
		}
		if (status != BirdStatus.FALLING) {
			if (getPositionX() >= INITIAL_X + WANDER_WIDTH) {
				flyLeft();
			}
			if (getPositionX() <= INITIAL_X - WANDER_WIDTH) {
				flyRight();
			}
		}
	}

	@Override
	public void verticalCollision(Player player) {
		super.verticalCollision(player);
		if (player instanceof Ground) {
			status = BirdStatus.FALLEN;
			velocityX = 0;
		}
	}

	@Override
	public void render(GraphicsContext gc) {
		if (status == BirdStatus.FLYING) {
			flyingAnimation.render(gc, this);
		} else if (status == BirdStatus.FALLING) {
			fallingAnimation.render(gc, this);
		} else {
			fallenAnimation.render(gc, this);
		}
	}

	@Override
	public void attack(Player player) {
		if (status == BirdStatus.FLYING) {
			if (player.getPositionX() > getPositionX()) {
				flyRight();
			} else {
				flyLeft();
			}
			velocityY = 10;
			accelerationY = -1;
			status = BirdStatus.FALLING;
		}

	}

}
