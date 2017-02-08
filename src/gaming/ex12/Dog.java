package gaming.ex12;

import gaming.ex12.resources.ImageResource;
import javafx.scene.canvas.GraphicsContext;

public class Dog extends Player implements Enemy {
	public static final int INITIAL_Y = 236;
	public static final int INITIAL_X = 550;
	public static final int WANDER_WIDTH = 150;

	private RunAnimation walkingAnimation = new RunAnimation(ImageResource.DOG);
	private RunAnimation stoppedAnimation = new RunAnimation(ImageResource.DOG, 11, 1);
	private RunAnimation jumpAnimation = new RunAnimation(ImageResource.DOG, 7, 2);
	private DogStatus status = DogStatus.WALKING;

	public Dog() {
		super(ImageResource.DOG);
		setPositionY(INITIAL_Y);
		setPositionX(INITIAL_X);
		accelerationY = 1;
		colisionY = 35;
		colisionX = 25;
		colisionWidth = 25;
		direction = Direction.LEFT;
	}

	public void jump() {
		if (status == DogStatus.WALKING || status == DogStatus.STOPPED) {
			velocityY = -15;
			accelerationY = 1;
			status = DogStatus.ATTACKING;
		}
	}

	@Override
	public void verticalCollision(Player player) {
		super.verticalCollision(player);
		if (player instanceof Ground) {
			status = velocityX != 0 ? DogStatus.WALKING : DogStatus.STOPPED;
		}
	}

	public void walkLeft() {
		velocityX = -5;
		direction = Direction.LEFT;
	}

	public void stop() {
		velocityX = 0;
		if (status == DogStatus.WALKING) {
			status = DogStatus.STOPPED;
		}
	}

	public void walkRight() {
		direction = Direction.RIGHT;
		velocityX = 5;
	}

	enum DogStatus {
		ATTACKING,
		STOPPED,
		WALKING;
	}

	@Override
	public void update(double time) {
		super.update(time);
		if (status == DogStatus.STOPPED) {
			stoppedAnimation.update(time * 40.0 / 50.0);
		} else if (status == DogStatus.WALKING) {
			walkingAnimation.update(time * 40.0 / 50.0);
		} else {
			jumpAnimation.update(time * 40.0 / 50.0);
		}
		if (status != DogStatus.ATTACKING) {
			if (getPositionX() >= INITIAL_X + WANDER_WIDTH) {
				walkLeft();
			}
			if (getPositionX() <= INITIAL_X - WANDER_WIDTH) {
				walkRight();
			}
		}

	}

	@Override
	public void render(GraphicsContext gc) {
		if (status == DogStatus.STOPPED) {
			stoppedAnimation.render(gc, this);
		} else if (status == DogStatus.WALKING) {
			walkingAnimation.render(gc, this);
		} else {
			jumpAnimation.render(gc, this);
		}
	}

	@Override
	public void attack(Player player) {
		if (status != DogStatus.ATTACKING) {
			if (player.getPositionX() > getPositionX()) {
				walkRight();
			} else {
				walkLeft();
			}
			jump();
		}
	}

}
