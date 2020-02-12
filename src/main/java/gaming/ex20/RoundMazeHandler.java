package gaming.ex20;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RoundMazeHandler {
    public static final int MAZE_WIDTH = 7;
    public static final int MAZE_HEIGHT = 40;
    private int r;
	private int c;
	private final Random random = new Random();
	private final List<RoundMazeSquare> history = new ArrayList<>();
    private final List<String> check = new ArrayList<>();
    private final RoundMazeSquare[][] createdMaze;

    public RoundMazeHandler(RoundMazeSquare[][] maze) {
		createdMaze = maze;
		history.add(maze[0][0]);
	}

    public void create() {
		while (!history.isEmpty()) {
			createdMaze[r][c].setVisited(true);
			check.clear();

            addPossibleSides();
			if (!check.isEmpty()) {
				history.add(createdMaze[r][c]);
				final String direction = check.get(random.nextInt(check.size()));
                setSidesByDirection(direction);
			} else {
                goBackIn(history);
			}
		}
        int cell = random.nextInt(RoundMazeHandler.MAZE_WIDTH);

		for (int i = 0; i < RoundMazeHandler.MAZE_HEIGHT / 8; i++) {
			createdMaze[0][(cell + i) % RoundMazeHandler.MAZE_HEIGHT].setNorth(true);
		}
    }

    private void addPossibleSides() {
        if (!createdMaze[r][w(c - 1)].isVisited()) {
        	check.add("L");
            check.add("L");
        }
        if (r > 0 && !createdMaze[r - 1][c].isVisited()) {
        	check.add("U");
        }
        if (!createdMaze[r][w(c + 1)].isVisited()) {
        	check.add("R");
            check.add("R");
        }
        if (r < createdMaze.length - 1 && !createdMaze[r + 1][c].isVisited()) {
        	check.add("D");
        }
    }

    private boolean goBackIn(List<RoundMazeSquare> history1) {
		final RoundMazeSquare remove = history1.remove(history1.size() - 1);
		for (int i = 0; i < createdMaze.length; i++) {
			for (int j = 0; j < createdMaze[i].length; j++) {
				if (createdMaze[i][j] == remove) {
					r = i;
					c = j;
					return true;
				}
			}
		}
		return false;
	}

    private void setSidesByDirection(final String direction) {
        if ("L".equals(direction)) {
        	createdMaze[r][c].setWest(true);
            c = w(c - 1);
        	createdMaze[r][c].setEast(true);
        }
        if ("U".equals(direction)) {
        	createdMaze[r][c].setNorth(true);
        	r = r - 1;
        	createdMaze[r][c].setSouth(true);
        }
        if ("R".equals(direction)) {
        	createdMaze[r][c].setEast(true);
            c = w(c + 1);
        	createdMaze[r][c].setWest(true);
        }
        if ("D".equals(direction)) {
        	createdMaze[r][c].setSouth(true);
        	r = r + 1;
        	createdMaze[r][c].setNorth(true);
        }
    }

    public static void createMaze(RoundMazeSquare[][] maze) {
        new RoundMazeHandler(maze).create();
    }

	private static int w(int c1) {
        return (c1 + RoundMazeHandler.MAZE_HEIGHT) % RoundMazeHandler.MAZE_HEIGHT;
    }
}