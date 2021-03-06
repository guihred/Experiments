package gaming.ex12;

import javafx.scene.canvas.GraphicsContext;

public class Dog extends Player implements Enemy {
    private static final int INITIAL_Y = 236;
    private static final int INITIAL_X = 550;
    private static final int WANDER_WIDTH = 150;

	private static final double SPEED = 4. / 5;
	private RunAnimation walkingAnimation = new RunAnimation(ImageResource.DOG);
	private RunAnimation stoppedAnimation = new RunAnimation(ImageResource.DOG, 11, 1);
	private RunAnimation jumpAnimation = new RunAnimation(ImageResource.DOG, 7, 2);

	private DogStatus status = DogStatus.WALKING;

	public Dog() {
		super(ImageResource.DOG);
		setPositionY(INITIAL_Y);
		setPositionX(INITIAL_X);
		accelerationY = 1;
        final double i = 25;
        colisionY = i + 10;
        colisionX = i;
		colisionWidth = i;
		direction = Direction.LEFT;
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

	public void stop() {
		velocityX = 0;
		if (status == DogStatus.WALKING) {
			status = DogStatus.STOPPED;
		}
	}

    @Override
	public void update(double time) {
		super.update(time);
		if (status == DogStatus.STOPPED) {
            stoppedAnimation.update(time * SPEED);
		} else if (status == DogStatus.WALKING) {
            walkingAnimation.update(time * SPEED);
		} else {
            jumpAnimation.update(time * SPEED);
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

    private void jump() {
		if (status == DogStatus.WALKING || status == DogStatus.STOPPED) {
			velocityY = -15;
			accelerationY = 1;
			status = DogStatus.ATTACKING;
		}
	}

	private void walkRight() {
		direction = Direction.RIGHT;
		velocityX = 5;
	}

    private enum DogStatus {
		ATTACKING,
		STOPPED,
		WALKING;
	}

}
