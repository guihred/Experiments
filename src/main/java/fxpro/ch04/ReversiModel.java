package fxpro.ch04;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.binding.*;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;

public enum ReversiModel {
    MODEL;
    public static final int BOARD_SIZE = 8;
    private final ObjectProperty<Owner> turn = new SimpleObjectProperty<>(Owner.BLACK);
    @SuppressWarnings("unchecked")
    private final ObjectProperty<Owner>[][] board = new ObjectProperty[BOARD_SIZE][BOARD_SIZE];

    ReversiModel() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                board[i][j] = new SimpleObjectProperty<>(Owner.NONE);
            }
        }
        initBoard();
    }

    public BooleanBinding canFlip(final int cellX, final int cellY, final int directionX, final int directionY,
        final ObjectProperty<Owner> turn1) {

        List<ObjectExpression<?>> binds = new ArrayList<>();

        binds.add(turn1);
        int dx = cellX + directionX;
        int dy = cellY + directionY;
        while (dx >= 0 && dx < BOARD_SIZE && dy >= 0 && dy < BOARD_SIZE) {
            binds.add(board[dx][dy]);
            dx += directionX;
            dy += directionY;
        }

        return Bindings.createBooleanBinding(() -> {
            Owner turnVal = turn1.get();
            int x = cellX + directionX;
            int y = cellY + directionY;
            boolean first = true;
            while (xAndYWithinBoardSize(x, y) && board[x][y].get() != Owner.NONE) {
                if (board[x][y].get() == turnVal) {
                    return !first;
                }
                first = false;
                x += directionX;
                y += directionY;
            }
            return false;
        }, binds.toArray(new ObjectExpression[0]));
    }

    public void flip(int cellX, int cellY, int directionX, int directionY, ObjectProperty<Owner> turn1) {
        if (canFlip(cellX, cellY, directionX, directionY, turn1).get()) {
            int x = cellX + directionX;
            int y = cellY + directionY;
            while (xAndYWithinBoardSize(x, y) && board[x][y].get() != turn1.get()) {
                board[x][y].setValue(turn1.get());
                x += directionX;
                y += directionY;
            }
        }
    }

    public ObjectProperty<Owner> get(int i, int j) {
        return board[i][j];
    }

    public NumberExpression getScore(Owner owner) {
        NumberExpression score = new SimpleIntegerProperty();
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                score = score.add(Bindings.when(board[i][j].isEqualTo(owner)).then(1).otherwise(0));
            }
        }
        return score;
    }

    public ObjectProperty<Owner> getTurn() {
        return turn;
    }

    public NumberBinding getTurnsRemaining(Owner owner) {
        NumberExpression emptyCellCount = getScore(Owner.NONE);
        return Bindings.when(turn.isEqualTo(owner)).then(emptyCellCount.add(1).divide(2))
            .otherwise(emptyCellCount.divide(2));
    }

    public BooleanBinding legalMove(int x, int y) {

        return board[x][y].isEqualTo(Owner.NONE)
            .and(canFlip(x, y, 0, -1, turn).or(canFlip(x, y, -1, -1, turn)
                .or(canFlip(x, y, -1, 0, turn).or(canFlip(x, y, -1, 1, turn).or(canFlip(x, y, 0, 1, turn)
                    .or(canFlip(x, y, 1, 1, turn).or(canFlip(x, y, 1, 0, turn).or(canFlip(x, y, 1, -1, turn)))))))));
    }

    public void play(int cellX, int cellY) {
        if (legalMove(cellX, cellY).get()) {
            board[cellX][cellY].setValue(turn.get());
            flip(cellX, cellY, 0, -1, turn);
            flip(cellX, cellY, -1, -1, turn);
            flip(cellX, cellY, -1, 0, turn);
            flip(cellX, cellY, -1, 1, turn);
            flip(cellX, cellY, 0, 1, turn);
            flip(cellX, cellY, 1, 1, turn);
            flip(cellX, cellY, 1, 0, turn);
            flip(cellX, cellY, 1, -1, turn);
            turn.setValue(turn.getValue().opposite());
        }
    }

    public void restart() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                board[i][j].setValue(Owner.NONE);
            }
        }
        initBoard();
        turn.setValue(Owner.BLACK);
    }

    private void initBoard() {
        int center1 = BOARD_SIZE / 2 - 1;
        int center2 = BOARD_SIZE / 2;
        board[center1][center1].setValue(Owner.WHITE);
        board[center1][center2].setValue(Owner.BLACK);
        board[center2][center1].setValue(Owner.BLACK);
        board[center2][center2].setValue(Owner.WHITE);
    }

    public static ReversiModel getInstance() {
        return MODEL;
    }

    private static boolean xAndYWithinBoardSize(int x, int y) {
        return x >= 0 && x < BOARD_SIZE && y >= 0 && y < BOARD_SIZE;
    }

}
