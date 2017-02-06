package gaming.ex07;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

public class CreateMazeHandler implements EventHandler<ActionEvent> {
	int r = 0, c = 0;
	final Random random = new Random();
	Timeline timeline;
	List<MazeSquare> history = new ArrayList<>();
	List<String> check = new ArrayList<>();
	MazeSquare[][] createdMaze;

	public CreateMazeHandler(Timeline timeline, MazeSquare[][] maze) {
		this.timeline = timeline;
		createdMaze = maze;
		history.add(maze[0][0]);
	}

	@Override
	public void handle(ActionEvent event) {
		while (!history.isEmpty()) {
			createdMaze[r][c].visited.set(true);
			check.clear();

			if (c > 0 && !createdMaze[r][c - 1].visited.get()) {
				check.add("L");
			}
			if (r > 0 && !createdMaze[r - 1][c].visited.get()) {
				check.add("U");
			}
			if (c < MazeModel.MAZE_SIZE - 1 && !createdMaze[r][c + 1].visited.get()) {
				check.add("R");
			}
			if (r < MazeModel.MAZE_SIZE - 1 && !createdMaze[r + 1][c].visited.get()) {
				check.add("D");
			}
			if (!check.isEmpty()) {
				history.add(createdMaze[r][c]);
				final String direction = check.get(random.nextInt(check.size()));
				if ("L".equals(direction)) {
					createdMaze[r][c].west.set(true);
					c = c - 1;
					createdMaze[r][c].east.set(true);
				}
				if ("U".equals(direction)) {
					createdMaze[r][c].north.set(true);
					r = r - 1;
					createdMaze[r][c].south.set(true);
				}
				if ("R".equals(direction)) {
					createdMaze[r][c].east.set(true);
					c = c + 1;
					createdMaze[r][c].west.set(true);
				}
				if ("D".equals(direction)) {
					createdMaze[r][c].south.set(true);
					r = r + 1;
					createdMaze[r][c].north.set(true);
				}
			} else {
				boolean backIn = getBackIn(history);
				if (backIn) {
					return;
				}
			}
		}
		timeline.stop();
	}

	private boolean getBackIn(List<MazeSquare> history) {
		final MazeSquare remove = history.remove(history.size() - 1);
		for (int i = 0; i < MazeModel.MAZE_SIZE; i++) {
			for (int j = 0; j < MazeModel.MAZE_SIZE; j++) {
				if (createdMaze[i][j] == remove) {
					r = i;
					c = j;
					return true;
				}
			}
		}
		return false;
	}
}