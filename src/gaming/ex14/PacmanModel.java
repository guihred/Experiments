package gaming.ex14;

import gaming.ex07.MazeSquare;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

public class PacmanModel {

	private static final int MAZE_SIZE = 20;
	Pacman pacman = new Pacman();
	PacmanGhost ghost = new PacmanGhost(Color.RED);

	public PacmanModel(Pane group, Scene scene) {

		// Timeline timeline = new Timeline();
		// MazeSquare[][] maze = initializeMaze();
		// final EventHandler<ActionEvent> eventHandler = new
		// CreateMazeHandler(timeline, maze);
		// final KeyFrame keyFrame = new KeyFrame(Duration.seconds(.001),
		// eventHandler);
		// timeline.getKeyFrames().add(keyFrame);
		// timeline.setCycleCount(Animation.INDEFINITE);
		// timeline.play();

		group.getChildren().add(pacman);
		group.getChildren().add(ghost);
		scene.setOnKeyPressed(e -> {
			KeyCode code = e.getCode();
			switch (code) {
			case DOWN:
				pacman.setLayoutY(pacman.getLayoutY() + 10);
				pacman.turn(90);
				break;
			case UP:
				pacman.setLayoutY(pacman.getLayoutY() - 10);
				pacman.turn(270);
				break;
			case LEFT:
				pacman.setLayoutX(pacman.getLayoutX() - 10);
				pacman.turn(180);
				break;
			case RIGHT:
				pacman.turn(0);
				pacman.setLayoutX(pacman.getLayoutX() + 10);
				break;
			default:
				break;

			}
		});

	}

	private MazeSquare[][] initializeMaze() {
		MazeSquare[][] maze = new MazeSquare[MAZE_SIZE][MAZE_SIZE];
		for (int i = 0; i < MAZE_SIZE; i++) {
			for (int j = 0; j < MAZE_SIZE; j++) {
				maze[i][j] = new MazeSquare();
				if (i == 0) {
					maze[i][j].setNorth(false);
				}
				if (j == 0) {
					maze[i][j].setWest(false);
				}
				if (j == MAZE_SIZE - 1) {
					maze[i][j].setEast(false);
				}
				if (i == MAZE_SIZE - 1) {
					maze[i][j].setSouth(false);
				}
			}
		}
		return maze;
	}


	public static PacmanModel create(Pane group, Scene scene) {
		return new PacmanModel(group, scene);
	}

}
