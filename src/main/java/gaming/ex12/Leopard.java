package gaming.ex12;

import javafx.scene.canvas.GraphicsContext;

public class Leopard extends Player {
    private static final int INITIAL_Y = 238;

	private RunAnimation stoppedAnimation = new RunAnimation(ImageResource.LEOPARD, 10, 1);
	private RunAnimation runAnimation = new RunAnimation(ImageResource.LEOPARD);
	private RunAnimation jumpAnimation = new RunAnimation(ImageResource.LEOPARD, 5, 3);
	private LeopardStatus status = LeopardStatus.STOPPED;

	public Leopard() {
		super(ImageResource.LEOPARD);
		setPositionY(INITIAL_Y);
		colisionY = 50;
		colisionX = 30;
        colisionWidth = 7 * 5.;
	}

	public void jump() {
		if (status == LeopardStatus.RUNNING || status == LeopardStatus.STOPPED) {
			velocityY = -15;
			accelerationY = 1;
			status = LeopardStatus.JUMPING;
		}
	}

	@Override
	public void render(GraphicsContext gc) {
		if (status == LeopardStatus.RUNNING) {
			runAnimation.render(gc, this);
		} else if (status == LeopardStatus.JUMPING) {
			jumpAnimation.render(gc, this);
		} else {
			stoppedAnimation.render(gc, this);
		}

	}

	public void stop() {
		velocityX = 0;
		accelerationX = 0;
		if (status == LeopardStatus.RUNNING) {
			status = LeopardStatus.STOPPED;
		}
	}

	@Override
	public void update(double time) {
		super.update(time);
		if (status == LeopardStatus.RUNNING) {
			runAnimation.update(time);
		} else if (status == LeopardStatus.JUMPING) {
            jumpAnimation.update(time * 4 / 3);
		} else {
			stoppedAnimation.update(time);
		}

		if (getPositionY() >= INITIAL_Y) {
			setPositionY(INITIAL_Y);
			accelerationY = 0;
			status = velocityX != 0 ? LeopardStatus.RUNNING : LeopardStatus.STOPPED;
		}

	}

	public void walkLeft() {
		if (velocityX == 0) {
			velocityX = -5;
		}
		direction = Direction.LEFT;
		accelerationX = -1;
	}

	public void walkRight() {
		direction = Direction.RIGHT;
		if (velocityX == 0) {
			velocityX = 5;
		}
		accelerationX = 1;
	}

    private enum LeopardStatus {
		STOPPED,
		RUNNING,
		JUMPING;
	}

}
