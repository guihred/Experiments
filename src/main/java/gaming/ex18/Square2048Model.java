/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gaming.ex18;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;

/**
 *
 * @author Note
 */
public class Square2048Model {
    public static final int MAP_HEIGHT = 4;
    public static final int MAP_WIDTH = 4;

    private GridPane gridPane;
    private final Square2048[][] map = new Square2048[MAP_WIDTH][MAP_HEIGHT];
    private IntegerProperty nPlayed = new SimpleIntegerProperty(0);
    private long startTime;
    private List<Square2048> mapAsList = new ArrayList<>();
    final Random random = new Random();

    public Square2048Model(GridPane gridPane) {
        this.gridPane = gridPane;

        for (int i = 0; i < getMap().length; i++) {
            for (int j = 0; j < getMap()[i].length; j++) {
                getMap()[i][j] = new Square2048(i, j);
                mapAsList.add(getMap()[i][j]);
            }
        }
        getMap()[random.nextInt(MAP_WIDTH)][random.nextInt(MAP_HEIGHT)].setNumber(newNumber());
        getMap()[random.nextInt(MAP_WIDTH)][random.nextInt(MAP_HEIGHT)].setNumber(newNumber());

        startTime = System.currentTimeMillis();
    }

    private int newNumber() {
        return (random.nextInt(2) + 1) * 2;
    }

    public Square2048[][] getMap() {
        return map;
    }

    public void handleKeyPressed(KeyEvent e) {
        final KeyCode code = e.getCode();
        int x = 0, y = 0;

        switch (code) {
            case UP:
            case W:
                y = -1;
                break;
            case LEFT:
            case A:
                x = -1;
                break;
            case RIGHT:
            case D:
                x = 1;
                break;
            case DOWN:
            case S:
                y = 1;
                break;
            default:
        }
        boolean changed = true;

        while (changed) {
            changed = false;
            for (int i = 0; i < getMap().length; i++) {
                for (int j = 0; j < getMap()[i].length; j++) {
                    if (!getMap()[i][j].isEmpty() && i + x >= 0 && i + x < MAP_WIDTH && j + y >= 0
                            && j + y < MAP_HEIGHT) {
                        if (getMap()[i + x][j + y].isEmpty()) {
                            getMap()[i + x][j + y].setNumber(getMap()[i][j].getNumber());
                            getMap()[i][j].setNumber(0);
                            changed = true;
                        } else if (getMap()[i + x][j + y].getNumber() == getMap()[i][j].getNumber()) {
                            getMap()[i + x][j + y].setNumber(getMap()[i][j].getNumber() * 2);
                            getMap()[i][j].setNumber(0);
                            changed = true;
                        }
                    }
                }
            }
        }
        
        List<Square2048> collect = mapAsList.stream().filter(Square2048::isEmpty).collect(Collectors.toList());
        if (!collect.isEmpty()) {
            collect.get(random.nextInt(collect.size())).setNumber(this.newNumber());
        }

    }

}