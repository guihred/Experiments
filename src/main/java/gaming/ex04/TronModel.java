/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gaming.ex04;

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
public class TronModel {
    public static final int MAP_SIZE = 50;

	private final TronSquare[][] map = new TronSquare[MAP_SIZE][MAP_SIZE];

	private final ObservableList<TronSquare> snake = FXCollections.observableArrayList();

	private TronDirection direction = TronDirection.RIGHT;
	private final Random random = new Random();

    public TronModel() {
        for (int i = 0; i < MAP_SIZE; i++) {
            for (int j = 0; j < MAP_SIZE; j++) {
				map[i][j] = new TronSquare(i, j);
            }
        }

        int i = random.nextInt(MAP_SIZE);
        int j = random.nextInt(MAP_SIZE);

		snake.add(map[i][j]);
        i = random.nextInt(MAP_SIZE);
        j = random.nextInt(MAP_SIZE);
		map[i][j].setState(TronState.FOOD);
        snake.addListener((ListChangeListener.Change<? extends TronSquare> c) -> {
            c.next();
			c.getAddedSubList().forEach((TronSquare s) -> s.setState(TronState.SNAKE));
        });
        updateMap();
    }

    public TronDirection getDirection() {
		return direction;
	}

    public TronSquare[][] getMap() {
		return map;
	}

	public ObservableList<TronSquare> getSnake() {
		return snake;
	}

	public final void reset() {
        getSnake().clear();
        for (int i = 0; i < MAP_SIZE; i++) {
            for (int j = 0; j < MAP_SIZE; j++) {
				map[i][j].setState(TronState.NONE);
            }
        }
        int i = random.nextInt(MAP_SIZE);
        int j = random.nextInt(MAP_SIZE);

		getSnake().add(map[i][j]);
        i = random.nextInt(MAP_SIZE);
        j = random.nextInt(MAP_SIZE);
		map[i][j].setState(TronState.FOOD);

    }

	public void setDirection(TronDirection direction) {
		this.direction = direction;
	}

	public final boolean updateMap() {
        final TronSquare head = getSnake().get(0);
        int i = head.getI();
        int j = head.getJ();
        switch (getDirection()) {
            case LEFT:
                i = (i - 1 + MAP_SIZE) % MAP_SIZE;
                break;
            case RIGHT:
                i = (i + 1) % MAP_SIZE;
                break;
            case UP:
                j = (j - 1 + MAP_SIZE) % MAP_SIZE;
                break;
            case DOWN:
                j = (j + 1) % MAP_SIZE;
                break;
            default:
        }
		if (map[i][j].getState() == TronState.NONE) {
            getSnake().remove(getSnake().size() - 1);
		} else if (map[i][j].getState() == TronState.FOOD) {
            final List<TronSquare> emptySquares = Stream.of(map).flatMap(Stream::of)
					.filter(s -> s.getState() == TronState.NONE).collect(Collectors.toList());
            if (!emptySquares.isEmpty()) {
                emptySquares.get(random.nextInt(emptySquares.size())).setState(TronState.FOOD);
            }

        } else {

            return true;
        }
		if (!getSnake().contains(map[i][j])) {
			getSnake().add(0, map[i][j]);
		}


        
        return false;
    }


    

}
