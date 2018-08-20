package gaming.ex07;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

public class CreateMazeHandler implements EventHandler<ActionEvent> {
    private int r;
    private int c;
	private final Random random = new Random();
	private final Timeline timeline;
	private final List<MazeSquare> history = new ArrayList<>();
	private final List<String> check = new ArrayList<>();
	private final MazeSquare[][] createdMaze;

	public CreateMazeHandler(Timeline timeline, MazeSquare[][] maze) {
		this.timeline = timeline;
		createdMaze = maze;
		history.add(maze[0][0]);
	}

	@Override
	public void handle(ActionEvent event) {
		while (!history.isEmpty()) {
			createdMaze[r][c].setVisited(true);
			check.clear();

            addPossibleSides();
			if (!check.isEmpty()) {
				history.add(createdMaze[r][c]);
				final String direction = check.get(random.nextInt(check.size()));
                setSidesByDirection(direction);
			} else {
				boolean backIn = getBackIn(history);
				if (backIn) {
					return;
				}
			}
		}
        setSides();
        timeline.stop();
    }

    private void setSidesByDirection(final String direction) {
        if ("L".equals(direction)) {
        	createdMaze[r][c].setWest(true);
        	c = c - 1;
        	createdMaze[r][c].setEast(true);
        }
        if ("U".equals(direction)) {
        	createdMaze[r][c].setNorth(true);
        	r = r - 1;
        	createdMaze[r][c].setSouth(true);
        }
        if ("R".equals(direction)) {
        	createdMaze[r][c].setEast(true);
        	c = c + 1;
        	createdMaze[r][c].setWest(true);
        }
        if ("D".equals(direction)) {
        	createdMaze[r][c].setSouth(true);
        	r = r + 1;
        	createdMaze[r][c].setNorth(true);
        }
    }

    private void setSides() {
        for (int i = 0; i < createdMaze.length; i++) {
			for (int j = 0; j < createdMaze[i].length; j++) {
				MazeSquare mazeSquare = createdMaze[i][j];
				if (i > 0 && !mazeSquare.isEast() && !mazeSquare.isNorth() && !mazeSquare.isWest()) {
					createdMaze[i][j].setNorth(true);
					createdMaze[i - 1][j].setSouth(true);
				}
				if (i < createdMaze.length - 1 && !mazeSquare.isEast() && !mazeSquare.isSouth()
						&& !mazeSquare.isWest()) {
					createdMaze[i][j].setSouth(true);
					createdMaze[i + 1][j].setNorth(true);
				}
				if (j < createdMaze[i].length - 1 && !mazeSquare.isNorth() && !mazeSquare.isEast()
						&& !mazeSquare.isSouth()) {
					createdMaze[i][j].setEast(true);
					createdMaze[i][j + 1].setWest(true);
				}
				if (j > 0 && !mazeSquare.isNorth() && !mazeSquare.isWest() && !mazeSquare.isSouth()) {
					createdMaze[i][j].setWest(true);
					createdMaze[i][j - 1].setEast(true);
				}
			}
		}
    }

    private void addPossibleSides() {
        if (c > 0 && !createdMaze[r][c - 1].isVisited()) {
        	check.add("L");
        }
        if (r > 0 && !createdMaze[r - 1][c].isVisited()) {
        	check.add("U");
        }
        if (c < createdMaze.length - 1 && !createdMaze[r][c + 1].isVisited()) {
        	check.add("R");
        }
        if (r < createdMaze.length - 1 && !createdMaze[r + 1][c].isVisited()) {
        	check.add("D");
        }
    }

	private boolean getBackIn(List<MazeSquare> history1) {
		final MazeSquare remove = history1.remove(history1.size() - 1);
		for (int i = 0; i < createdMaze.length; i++) {
			for (int j = 0; j < createdMaze.length; j++) {
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