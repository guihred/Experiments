/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gaming.ex10;

import gaming.ex10.MinesweeperSquare.State;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.EventHandler;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import simplebuilder.SimpleDialogBuilder;

/**
 *
 * @author Note
 */
public class MinesweeperModel {

    private static final int NUMBER_OF_BOMBS = 30;
    private static final int MAP_HEIGHT = 16;
    private static final int MAP_WIDTH = 16;

    private GridPane gridPane;
    private final MinesweeperSquare[][] map = new MinesweeperSquare[MAP_WIDTH][MAP_HEIGHT];
    private IntegerProperty nPlayed = new SimpleIntegerProperty(0);
    private long startTime;

    public MinesweeperModel(GridPane gridPane) {
        this.gridPane = gridPane;

        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[i].length; j++) {
                map[i][j] = new MinesweeperSquare(i, j);
            }
        }
        setBombs();
        startTime = System.currentTimeMillis();

    }

    public MinesweeperSquare[][] getMap() {
        return map;
    }

    private int countBombsAround(int i, int j) {
        int num = 0;
        for (int k = -1; k <= 1; k++) {
            for (int l = -1; l <= 1; l++) {
                if (l == 0 && k == 0) {
                    continue;
                }
                if (withinRange(i, k, MAP_WIDTH) && withinRange(l, j, MAP_HEIGHT)
                    && map[i + k][j + l].getMinesweeperImage() == MinesweeperImage.BOMB) {
                    num++;
                }
            }
        }
        return num;
    }

    private final EventHandler<MouseEvent> createMouseClickedEvent(MinesweeperSquare mem) {
        EventHandler<MouseEvent> mouseClicked = (MouseEvent event) -> handleClick(event, mem);
        mem.getFinalShape().setOnMouseClicked(mouseClicked);
        mem.getFlag().setOnMouseClicked(mouseClicked);
        mem.setOnMouseClicked(mouseClicked);
        return mouseClicked;
    }

    private void handleClick(MouseEvent event, MinesweeperSquare mem) {
        if (event.getButton() == MouseButton.SECONDARY) {
            toggleFlag(mem);
            return;
        }
        if (mem.getState() != MinesweeperSquare.State.HIDDEN) {
            return;
        }
        nPlayed.set(nPlayed.get() + 1);
        mem.setState(MinesweeperSquare.State.SHOWN);
        if (mem.getMinesweeperImage() == MinesweeperImage.BOMB) {
            if (nPlayed.get() != 0) {
                showDialog("You exploded!");
                return;
            }
            reset();
        }
        if (mem.getMinesweeperImage() == MinesweeperImage.BLANK) {
            showNeighbours(mem.getI(), mem.getJ());
        }
        if (verifyEnd()) {
            showDialog("You won in " + (System.currentTimeMillis() - startTime) / 1000 + " seconds! ");
        }
    }

    private void reset() {
        if (!gridPane.getScene().getWindow().isShowing()) {
            return;
        }
        nPlayed.set(0);
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[i].length; j++) {
                map[i][j].setFinalShape(null);
                map[i][j].setMinesweeperImage(MinesweeperImage.BLANK);
                map[i][j].setState(MinesweeperSquare.State.HIDDEN);
            }
        }
        setBombs();
        gridPane.getChildren().clear();
        for (int i = 0; i < MAP_WIDTH; i++) {
            for (int j = 0; j < MAP_HEIGHT; j++) {
                MinesweeperSquare map1 = map[i][j];
                gridPane.add(new StackPane(map1, map1.getFinalShape(), map1.getFlag()), i, j);
            }
        }
        startTime = System.currentTimeMillis();
    }

    private void setBombs() {

        List<MinesweeperSquare> sqrts = Stream.of(map).flatMap(Stream::of).collect(Collectors.toList());
        Collections.shuffle(sqrts);
        for (long count = 0; count < NUMBER_OF_BOMBS; count++) {
            final MinesweeperSquare mem = sqrts.remove(0);
            mem.setMinesweeperImage(MinesweeperImage.BOMB);
        }
        for (int i = 0; i < MAP_WIDTH; i++) {
            for (int j = 0; j < MAP_HEIGHT; j++) {
                if (map[i][j].getMinesweeperImage() == MinesweeperImage.BLANK) {
                    int num = countBombsAround(i, j);
                    if (num != 0) {
                        map[i][j].setNum(num);
                        map[i][j].setMinesweeperImage(MinesweeperImage.NUMBER);
                    }

                }
                createMouseClickedEvent(map[i][j]);
            }
        }
    }

    private void showDialog(String text2) {
        new SimpleDialogBuilder().text(text2).button("Reset", this::reset).bindWindow(gridPane).displayDialog();
    }

    private void showNeighbours(int i, int j) {
        map[i][j].setState(MinesweeperSquare.State.SHOWN);
        for (int k = -1; k <= 1; k++) {
            for (int l = -1; l <= 1; l++) {
                showWhiteSquares(i, j, k, l);
            }
        }

    }

    private void showWhiteSquares(int i, int j, int k, int l) {
        if (l == 0 && k == 0) {
            return;
        }
        if (withinRange(i, k, MAP_WIDTH) && withinRange(j, l, MAP_HEIGHT)) {
            if (map[i + k][j + l].getMinesweeperImage() == MinesweeperImage.BLANK
                && map[i + k][j + l].getState() == MinesweeperSquare.State.HIDDEN) {
                showNeighbours(i + k, j + l);
            }
            map[i + k][j + l].setState(MinesweeperSquare.State.SHOWN);
        }
    }

    private boolean verifyEnd() {
        return Stream.of(map).flatMap(Stream::of).noneMatch(s -> s.getState().equals(MinesweeperSquare.State.HIDDEN)
            && !s.getMinesweeperImage().equals(MinesweeperImage.BOMB));
    }

    private static void toggleFlag(MinesweeperSquare mem) {
        if (mem.getState() == State.HIDDEN) {
            mem.setState(MinesweeperSquare.State.FLAGGED);
        } else if (mem.getState() == State.FLAGGED) {
            mem.setState(MinesweeperSquare.State.HIDDEN);
        }
    }

    private static boolean withinRange(int i, int k, int mapWidth) {
        return i + k >= 0 && i + k < mapWidth;
    }

}
