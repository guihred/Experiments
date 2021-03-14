package gaming.ex12;

import javafx.scene.canvas.GraphicsContext;

public class Bird extends Player implements Enemy {
    private static final int INITIAL_Y = 100;
    private static final double INITIAL_X = 300;
    private static final int WANDER_WIDTH = 300;
	private RunAnimation flyingAnimation = new RunAnimation(ImageResource.BIRD);
	private RunAnimation fallingAnimation = new RunAnimation(ImageResource.BIRD, 20, 2);
	private RunAnimation fallenAnimation = new RunAnimation(ImageResource.BIRD, 6, 1);

	private BirdStatus status = BirdStatus.FLYING;

	public Bird() {
		super(ImageResource.BIRD);
		setPositionY(INITIAL_Y);
		setPositionX(INITIAL_X + INITIAL_X);
        final double i = 55;
        colisionY = i;
        colisionHeight = i + 10;
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

	public void stop() {
		velocityX = 0;
	}

	@Override
	public void update(double time) {
		super.update(time);
		if (status == BirdStatus.FLYING) {
            flyingAnimation.update(time * 4. / 5);
		} else if (status == BirdStatus.FALLING) {
            fallingAnimation.update(time * 4. / 5);
		} else {
            fallenAnimation.update(time * 4. / 5);
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

	private void flyLeft() {
		velocityX = -2;
		direction = Direction.RIGHT;
	}

    private enum BirdStatus {
		FALLEN,
		FLYING,
		FALLING;
	}

}
