package labyrinth;

import java.util.Random;
import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.shape.MeshView;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.stage.Modality;
import javafx.stage.Stage;

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
				stop();
				Stage dialogStage = new Stage();
				dialogStage.initModality(Modality.WINDOW_MODAL);
				Button button = new Button("Ok.");
				button.setOnAction(e -> {
					labyrinth3dWallTexture.getCamera().setTranslateX(0);
					labyrinth3dWallTexture.getCamera().setTranslateY(0);
					labyrinth3dWallTexture.getCamera().setTranslateZ(0);
					start();
					dialogStage.close();
				});
				VBox vbox = new VBox();
				vbox.getChildren().addAll(new Text("Voc� Morreu"), button);
				vbox.setAlignment(Pos.CENTER);
				vbox.setPadding(new Insets(5));
				dialogStage.setScene(new Scene(vbox));
				dialogStage.show();
			}

		}
	}



}