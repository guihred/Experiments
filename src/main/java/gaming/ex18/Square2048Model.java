/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gaming.ex18;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import simplebuilder.SimpleDialogBuilder;

/**
 *
 * @author Note
 */
public class Square2048Model {
    private static final int MAIN_GOAL = 2048;
    private static final int MAP_HEIGHT = 4;
    private static final int MAP_WIDTH = 4;

    private final Square2048[][] map = new Square2048[MAP_WIDTH][MAP_HEIGHT];
    private List<Square2048> mapAsList = new ArrayList<>();
    private final Random random = new Random();
    private GridPane gridPane;

    public Square2048Model(GridPane gridPane) {
        this.gridPane = gridPane;
        initialize();

    }

    public final Square2048[][] getMap() {
        return map;
    }

	public void handleKeyPressed(KeyEvent e) {
        final KeyCode code = e.getCode();
        int x = getX(code);
        int y = getY(code);

		changeMap(x, y);
        
        List<Square2048> emptySquares = mapAsList.stream().filter(Square2048::isEmpty).collect(Collectors.toList());
        Collections.shuffle(emptySquares);
        if (!emptySquares.isEmpty()) {
            emptySquares.remove(0).setNumber(newNumber());
        } else if (noPossibleMove()) {
            new SimpleDialogBuilder().text("You Lose").button("_Reset", () -> {
                gridPane.getChildren().clear();
                initialize();
            }).bindWindow(gridPane).displayDialog();
        }
        if (mapAsList.stream().anyMatch(s -> s.getNumber() == MAIN_GOAL)) {
            new SimpleDialogBuilder().text("You Won").button("_Reset", () -> {
                gridPane.getChildren().clear();
                initialize();
            }).bindWindow(gridPane).displayDialog();
        }

    }

	private void changeMap(int x, int y) {
		boolean changed = true;

        while (changed) {
            changed = false;
            for (int i = 0; i < getMap().length; i++) {
                for (int j = 0; j < getMap()[i].length; j++) {
                    if (!getMap()[i][j].isEmpty() && withinRange(x, y, i, j, MAP_WIDTH, MAP_HEIGHT)) {
                        changed = tryChange(x, y, changed, i, j);
                    }
                }
            }
        }
	}

    private void initialize() {
        for (int i = 0; i < getMap().length; i++) {
            for (int j = 0; j < getMap()[i].length; j++) {
                getMap()[i][j] = new Square2048();
                mapAsList.add(getMap()[i][j]);
            }
        }
        getMap()[random.nextInt(MAP_WIDTH)][random.nextInt(MAP_HEIGHT)].setNumber(newNumber());
        getMap()[random.nextInt(MAP_WIDTH)][random.nextInt(MAP_HEIGHT)].setNumber(newNumber());
        for (int i = 0; i < getMap().length; i++) {
            for (int j = 0; j < getMap()[i].length; j++) {
                Square2048 map1 = getMap()[i][j];
                gridPane.add(map1, i, j);
            }
        }
    }

	private int newNumber() {
        return (random.nextInt(2) + 1) * 2;
    }

	private boolean noPossibleMove() {
        int[][] directions= {{1,0},{0,1},{-1,0},{0,-1}};
        for (int[] dir : directions) {
            int x = dir[0];
            int y = dir[1];

            for (int i = 0; i < getMap().length; i++) {
                for (int j = 0; j < getMap()[i].length; j++) {
                    if (!getMap()[i][j].isEmpty() && within(i, x) && within(j, y)
                            && getMap()[i + x][j + y].getNumber() == getMap()[i][j].getNumber()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private boolean tryChange(int x, int y, boolean changed, int i, int j) {
        if (getMap()[i + x][j + y].isEmpty()) {
            getMap()[i + x][j + y].setNumber(getMap()[i][j].getNumber());
            getMap()[i][j].setNumber(0);
            return true;
        } else if (getMap()[i + x][j + y].getNumber() == getMap()[i][j].getNumber()) {
            getMap()[i + x][j + y].setNumber(getMap()[i][j].getNumber() * 2);
            getMap()[i][j].setNumber(0);
            return true;
        }
        return changed;
    }

    private static int getX(KeyCode code) {
        switch (code) {
            case LEFT:
            case A:
                return -1;
            case RIGHT:
            case D:
            	return 1;
            default:
        }
        return 0;
	}

    private static int getY(KeyCode code) {
    	switch (code) {
    		case UP:
            case W:
                return -1;
            case DOWN:
            case S:
                return 1;
    		default:
    			return 0;
    	}
    }

    private static boolean within(int i, int x) {
        return i + x >= 0 && i + x < MAP_WIDTH;
    }

    private static boolean withinRange(int x, int y, int i, int j, int mapWidth, int mapHeight) {
        return i + x >= 0 && i + x < mapWidth && j + y >= 0
                && j + y < mapHeight;
    }

}