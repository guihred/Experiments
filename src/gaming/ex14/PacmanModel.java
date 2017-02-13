package gaming.ex14;

import gaming.ex07.CreateMazeHandler;
import gaming.ex07.MazeSquare;
import gaming.ex14.Pacman.PacmanDirection;
import gaming.ex14.PacmanGhost.GhostColor;
import gaming.ex14.PacmanGhost.GhostStatus;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;
import javafx.animation.Animation;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class PacmanModel {

	public static final int MAZE_SIZE = 5;
	public static final int SQUARE_SIZE = 60;
	public static PacmanModel create(Group group, Scene scene) {
		return new PacmanModel(group, scene);
	}

	final IntegerProperty points = new SimpleIntegerProperty(0);
	final List<PacmanBall> balls = DoubleStream.iterate(SQUARE_SIZE / 2, d -> d + SQUARE_SIZE).limit(MAZE_SIZE * 2)
			.mapToObj(d -> d)
			.flatMap(d -> DoubleStream.iterate(SQUARE_SIZE / 2, e -> e + SQUARE_SIZE).limit(MAZE_SIZE * 2)
					.mapToObj(e -> new Double[] { d, e }))
			.map(PacmanBall::new).collect(Collectors.toList());

	final List<PacmanGhost> ghosts = Stream.of(GhostColor.RED, GhostColor.BLUE, GhostColor.ORANGE, GhostColor.GREEN)
			.map(PacmanGhost::new)
			.collect(Collectors.toList());

	final Pacman pacman = new Pacman();

	public PacmanModel(Group group, Scene scene) {
		Timeline timeline = new Timeline();
		MazeSquare[][] maze = initializeMaze();
		final EventHandler<ActionEvent> eventHandler = new CreateMazeHandler(timeline, maze);
		final KeyFrame keyFrame = new KeyFrame(Duration.seconds(.001), eventHandler);
		timeline.getKeyFrames().add(keyFrame);
		timeline.setCycleCount(Animation.INDEFINITE);
		timeline.play();
		timeline.statusProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue == Animation.Status.STOPPED) {
				createLabyrinth(maze, group);
			}
		});
		AnimationTimer animationTimer = new AnimationTimer() {
			long time;
			@Override
			public void handle(long now) {
				ghosts.forEach(g -> g.move(now, group.getChildren()));
				pacman.move(now, group.getChildren());
				List<PacmanBall> bal = balls.stream()
						.filter(b -> b.getBoundsInParent().intersects(pacman.getBoundsInParent()))
						.collect(Collectors.toList());
				if (!bal.isEmpty()) {
					points.set(points.get() + bal.size());
					balls.removeAll(bal);
					group.getChildren().removeAll(bal);
					if (bal.stream().anyMatch(PacmanBall::isSpecial)) {
						ghosts.stream().filter(g -> g.getStatus() == GhostStatus.ALIVE)
								.forEach(g -> g.setStatus(GhostStatus.AFRAID));
						time = 500;
					}
				}
				List<PacmanGhost> gh = ghosts.stream()
						.filter(b -> b.getBoundsInParent().intersects(pacman.getBoundsInParent()))
						.collect(Collectors.toList());
				if (!gh.isEmpty()) {
					if (gh.stream().anyMatch(g -> g.getStatus() == GhostStatus.ALIVE)) {
						pacman.die();
					} else {
						gh.forEach(g -> g.setStatus(GhostStatus.DEAD));
					}
				}

				if (time > 0) {
					time--;
					if (time == 0) {
						ghosts.stream().filter(g -> g.getStatus() == GhostStatus.AFRAID)
								.forEach(g -> g.setStatus(GhostStatus.ALIVE));
					}
				}
			}
		};

		animationTimer.start();
		Random random = new Random();
		for (int i = 0; i < 5; i++) {
			int nextInt = random.nextInt(balls.size());
			PacmanBall pacmanBall = balls.get(nextInt);
			pacmanBall.setSpecial(true);
		}

		group.getChildren().addAll(balls);
		group.getChildren().add(pacman);
		group.getChildren().addAll(ghosts);
		pacman.setLayoutY(30);
		pacman.setLayoutX(30);
		for (int i = 0; i < ghosts.size(); i++) {
			PacmanGhost ghost = ghosts.get(i);
			ghost.setStartPosition(265 + i % 2 * SQUARE_SIZE, 265 + i / 2 * SQUARE_SIZE);

		}
		scene.setOnKeyPressed(e -> {
			KeyCode code = e.getCode();
			switch (code) {
			case DOWN:
				pacman.turn(PacmanDirection.DOWN);
				break;
			case UP:
				pacman.turn(PacmanDirection.UP);
				break;
			case LEFT:

				pacman.turn(PacmanDirection.LEFT);
				break;
			case RIGHT:
				pacman.turn(PacmanDirection.RIGHT);
				break;
			case SPACE:
				pacman.turn(null);
				break;
			default:
				break;

			}
		});

	}

	private void addRectangle(Group group, double value, double value2, int width, int height) {
		Rectangle rectangle = new Rectangle(width, height, Color.BLUE);
		rectangle.setLayoutX(value);
		rectangle.setLayoutY(value2);
		group.getChildren().add(rectangle);
	}

	public void addRectangle(Group group, double value, double value2, int width, int height, Color blue) {
		Rectangle rectangle = new Rectangle(width, height, blue);
		rectangle.setLayoutX(value);
		rectangle.setLayoutY(value2);
		group.getChildren().add(rectangle);
	}
	private MazeSquare[][] createLabyrinth(MazeSquare[][] maze, Group group) {
		for (int i = 0; i < MAZE_SIZE; i++) {
			for (int j = 0; j < MAZE_SIZE; j++) {
				double layoutX = i * SQUARE_SIZE;
				double layoutX2 = MAZE_SIZE * 2 * SQUARE_SIZE - i * SQUARE_SIZE - SQUARE_SIZE;
				double layoutY = j * SQUARE_SIZE;
				double layoutY2 = MAZE_SIZE * 2 * SQUARE_SIZE - j * SQUARE_SIZE - SQUARE_SIZE;

				if (!maze[i][j].isWest()) {
					addRectangle(group, layoutX, layoutY, SQUARE_SIZE, 2);
					addRectangle(group, layoutX2, layoutY, SQUARE_SIZE, 2);
					addRectangle(group, layoutX, layoutY2 + SQUARE_SIZE, SQUARE_SIZE, 2);
					addRectangle(group, layoutX2, layoutY2 + SQUARE_SIZE, SQUARE_SIZE, 2);
				}
				if (!maze[i][j].isNorth()) {
					addRectangle(group, layoutX, layoutY, 2, SQUARE_SIZE);
					addRectangle(group, layoutX2 + SQUARE_SIZE, layoutY, 2, SQUARE_SIZE);
					addRectangle(group, layoutX, layoutY2, 2, SQUARE_SIZE);
					addRectangle(group, layoutX2 + SQUARE_SIZE, layoutY2, 2, SQUARE_SIZE);
				}
				if (!maze[i][j].isEast()) {
					addRectangle(group, layoutX, layoutY + SQUARE_SIZE, SQUARE_SIZE, 2);
					addRectangle(group, layoutX2, layoutY + SQUARE_SIZE, SQUARE_SIZE, 2);
					addRectangle(group, layoutX, layoutY2, SQUARE_SIZE, 2);
					addRectangle(group, layoutX2, layoutY2, SQUARE_SIZE, 2);
				}
				if (!maze[i][j].isSouth()) {
					addRectangle(group, layoutX + SQUARE_SIZE, layoutY, 2, SQUARE_SIZE);
					addRectangle(group, layoutX2, layoutY, 2, SQUARE_SIZE);
					addRectangle(group, layoutX + SQUARE_SIZE, layoutY2, 2, SQUARE_SIZE);
					addRectangle(group, layoutX2, layoutY2, 2, SQUARE_SIZE);
				}

			}
		}
		return maze;
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
				if (MAZE_SIZE - 1 == j && i % 3 == 0) {
					maze[i][j].setEast(true);
				}
				if (MAZE_SIZE - 1 == i && j % 3 == 0) {
					maze[i][j].setSouth(true);
				}
			}
		}
		return maze;
	}

}
