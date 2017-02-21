package labyrinth;

import java.util.Random;

import javafx.animation.AnimationTimer;
import javafx.scene.shape.MeshView;
import javafx.scene.transform.Rotate;

class MovimentacaoAleatoria extends AnimationTimer {
	private final CommomLabyrinth labyrinth3dWallTexture;
	private MeshView[] animais;
	// EAST, WEST, NORTH, SOUTH
	private int[] direction;
	private Random random = new Random();

	public MovimentacaoAleatoria(CommomLabyrinth labyrinth3dWallTexture, MeshView... animais) {
		this.labyrinth3dWallTexture = labyrinth3dWallTexture;
		this.animais = animais;
		direction = new int[animais.length];
	}

	@Override
	public void handle(long now) {
		for (int i = 0; i < animais.length; i++) {
			MeshView enemy = animais[i];

			final int step = 1;
			// NORTH
			if (direction[i] == 3) {
				enemy.setTranslateZ(enemy.getTranslateZ() + step);
			}
			// WEST
			if (direction[i] == 2) {
				enemy.setTranslateX(enemy.getTranslateX() - step);
			}
			// SOUTH
			if (direction[i] == 1) {
				enemy.setTranslateZ(enemy.getTranslateZ() - step);
			}
			// EAST
			if (direction[i] == 0) {
				enemy.setTranslateX(enemy.getTranslateX() + step);
			}
			if (labyrinth3dWallTexture.checkColision(enemy.getBoundsInParent())
					|| enemy.getTranslateZ() < 0
					|| enemy.getTranslateZ() > Labyrinth3DWallTexture.mapa[0].length * LabyrinthWall.SIZE

					|| enemy.getTranslateX() < 0
					|| enemy.getTranslateX() > Labyrinth3DWallTexture.mapa.length * LabyrinthWall.SIZE

			) {
				// NORTH
				if (direction[i] == 3) {
					enemy.setTranslateZ(enemy.getTranslateZ() - step);
				}
				// WEST
				if (direction[i] == 2) {
					enemy.setTranslateX(enemy.getTranslateX() + step);
				}
				// SOUTH
				if (direction[i] == 1) {
					enemy.setTranslateZ(enemy.getTranslateZ() + step);
				}
				// EAST
				if (direction[i] == 0) {
					enemy.setTranslateX(enemy.getTranslateX() - step);
				}
				enemy.setRotationAxis(Rotate.Y_AXIS);
				enemy.setRotate(direction[i] * 90);
				direction[i] = random.nextInt(4);

			}
			if (now % 1000 == 0) {
				direction[i] = random.nextInt(4);
			}
			if (labyrinth3dWallTexture.getCamera().getBoundsInParent().intersects(
					enemy.getBoundsInParent())) {
				labyrinth3dWallTexture.displayEndOfGame();
			}

		}
	}



}