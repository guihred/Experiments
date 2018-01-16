/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gaming.ex01;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

/**
 *
 * @author Note
 */
public class SnakeModel {
    public static final int MAP_SIZE = 50;

	private final SnakeSquare[][] map = new SnakeSquare[MAP_SIZE][MAP_SIZE];

	private final ObservableList<SnakeSquare> snake = FXCollections.observableArrayList();

	private SnakeDirection direction = SnakeDirection.RIGHT;
	private final Random random = new Random();

	public SnakeModel() {
		for (int i = 0; i < MAP_SIZE; i++) {
			for (int j = 0; j < MAP_SIZE; j++) {
				map[i][j] = new SnakeSquare(i, j);
			}
		}

		int i = random.nextInt(MAP_SIZE);
		int j = random.nextInt(MAP_SIZE);

		snake.add(map[i][j]);
		i = random.nextInt(MAP_SIZE);
		j = random.nextInt(MAP_SIZE);
		map[i][j].setState(SnakeState.FOOD);
		snake.addListener((ListChangeListener.Change<? extends SnakeSquare> c) -> {
			c.next();
			c.getAddedSubList().forEach((SnakeSquare s) -> s.setState(SnakeState.SNAKE));
			c.getRemoved().forEach((SnakeSquare s) -> s.setState(SnakeState.NONE));
		});
		updateMap();
	}

	public final void reset() {
		snake.clear();
		for (int i = 0; i < MAP_SIZE; i++) {
			for (int j = 0; j < MAP_SIZE; j++) {
				map[i][j].setState(SnakeState.NONE);
			}
		}
		int i = random.nextInt(MAP_SIZE);
		int j = random.nextInt(MAP_SIZE);

		snake.add(map[i][j]);
		i = random.nextInt(MAP_SIZE);
		j = random.nextInt(MAP_SIZE);
		map[i][j].setState(SnakeState.FOOD);

	}

	public final boolean updateMap() {
		final SnakeSquare head = snake.get(0);
        int i = head.getI();
        int j = head.getJ();
		switch (direction) {
		case LEFT:
			i = (i - 1 + MAP_SIZE) % MAP_SIZE;
			break;
		case UP:
			j = (j - 1 + MAP_SIZE) % MAP_SIZE;
			break;
		case RIGHT:
			i = (i + 1) % MAP_SIZE;
			break;
		case DOWN:
			j = (j + 1) % MAP_SIZE;
			break;
		default:
		}
        if (map[i][j].getState() == SnakeState.SNAKE) {
            return true;
        }
		if (map[i][j].getState() == SnakeState.NONE) {
			snake.remove(snake.size() - 1);
		} else if (map[i][j].getState() == SnakeState.FOOD) {
			final List<SnakeSquare> collect = Stream.of(map).flatMap(Stream::of)
					.filter(s -> s.getState() == SnakeState.NONE).collect(Collectors.toList());
			if (!collect.isEmpty()) {
				collect.get(random.nextInt(collect.size())).setState(SnakeState.FOOD);
			}

        }
		if (!snake.contains(map[i][j])) {
			snake.add(0, map[i][j]);
		}
		return false;
	}

	public SnakeSquare[][] getMap() {
		return map;
	}

	public ObservableList<SnakeSquare> getSnake() {
		return snake;
	}

	public SnakeDirection getDirection() {
		return direction;
	}

	public void setDirection(SnakeDirection direction) {
		this.direction = direction;
	}

}
