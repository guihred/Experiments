package fxproexercises.ch04;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.NumberBinding;
import javafx.beans.binding.NumberExpression;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;

public class ReversiModel {

    public static final int BOARD_SIZE = 8;
    public ObjectProperty<Owner> turn = new SimpleObjectProperty<>(Owner.BLACK);
	@SuppressWarnings("unchecked")
	public ObjectProperty<Owner>[][] board = new ObjectProperty[BOARD_SIZE][BOARD_SIZE];

    private ReversiModel() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                board[i][j] = new SimpleObjectProperty<>(Owner.NONE);
            }
        }

        initBoard();
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
    public NumberExpression getScore(Owner owner) {
        NumberExpression score = new SimpleIntegerProperty();
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                score = score.add(Bindings.when(board[i][j].isEqualTo(owner)).then(1).otherwise(0));
            }
        }
        return score;
    }

    public NumberBinding getTurnsRemaining(Owner owner) {
        NumberExpression emptyCellCount = getScore(Owner.NONE);
        return Bindings.when(turn.isEqualTo(owner))
                .then(emptyCellCount.add(1).divide(2))
                .otherwise(emptyCellCount.divide(2));
    }
    public static ReversiModel getInstance() {
        return ReversiModelHolder.INSTANCE;
    }

    public BooleanBinding legalMove(int x, int y) {

        return board[x][y].isEqualTo(Owner.NONE).and(
                canFlip(x, y, 0, -1, turn).or(
                canFlip(x, y, -1, -1, turn).or(
                canFlip(x, y, -1, 0, turn).or(
                canFlip(x, y, -1, 1, turn).or(
                canFlip(x, y, 0, 1, turn).or(
                canFlip(x, y, 1, 1, turn).or(
                canFlip(x, y, 1, 0, turn).or(
                canFlip(x, y, 1, -1, turn))))))))
        );
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

    public void flip(int cellX, int cellY, int directionX, int directionY, ObjectProperty<Owner> turn) {
        if (canFlip(cellX, cellY, directionX, directionY, turn).get()) {
            int x = cellX + directionX;
            int y = cellY + directionY;
            while (x >= 0 && x < BOARD_SIZE && y >= 0 && y < BOARD_SIZE && board[x][y].get()
                    != turn.get()) {
                board[x][y].setValue(turn.get());
                x += directionX;
                y += directionY;
            }
        }
    }
    public BooleanBinding canFlip(final int cellX, final int cellY, final int directionX, final int directionY, final ObjectProperty<Owner> turn) {

		return new BooleanBinding() {
            {
                bind(turn);
                int x = cellX + directionX;
                int y = cellY + directionY;
                while (x >= 0 && x < BOARD_SIZE && y >= 0 && y < BOARD_SIZE) {
                    bind(board[x][y]);
                    x += directionX;
                    y += directionY;
                }
            }

            @Override
            protected boolean computeValue() {
                Owner turnVal = turn.get();
                int x = cellX + directionX;
                int y = cellY + directionY;
                boolean first = true;
                while (x >= 0 && x < BOARD_SIZE && y >= 0 && y < BOARD_SIZE && board[x][y].get()
                        != Owner.NONE) {
                    if (board[x][y].get() == turnVal) {
                        return !first;
                    }
                    first = false;
                    x += directionX;
                    y += directionY;
                }
                return false;
            }
        };
    }

    private static class ReversiModelHolder {

        private static final ReversiModel INSTANCE = new ReversiModel();
    }
}
