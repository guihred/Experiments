/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gaming.ex19;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import org.slf4j.Logger;
import utils.CommonsFX;
import utils.HasLogging;

/**
 *
 * @author Note
 */
public class SudokuModel {

    private static final Logger LOG = HasLogging.log();

    public static final int MAP_NUMBER = 3;

    public static final int MAP_N_SQUARED = MAP_NUMBER * MAP_NUMBER;
    private Random random = new Random();
    private GridPane numberBoard = new GridPane();
    private List<NumberButton> numberOptions = new ArrayList<>();
    private List<SudokuSquare> sudokuSquares = new ArrayList<>();

    private SudokuSquare pressedSquare;

    public SudokuModel() {
        initialize();

    }

    public SudokuSquare getMapAt(int i, int j) {
        return sudokuSquares.get(i * MAP_N_SQUARED + j);
    }

    public Region getNumberBoard() {
        return numberBoard;
    }

    public void handleMouseMoved(MouseEvent s) {
        numberOptions.forEach(e -> e.setOver(e.getBoundsInParent().contains(s.getX(), s.getY())));
    }

    public void handleMousePressed(MouseEvent ev) {
        Optional<SudokuSquare> pressed = sudokuSquares.stream().filter(e -> !e.isPermanent())
                .filter(s -> s.getBoundsInParent().contains(ev.getX(), ev.getY())).findFirst();
        if (!pressed.isPresent()) {
            pressedSquare = null;
            return;
        }
        pressedSquare = pressed.get();
        Bounds boundsInParent = pressedSquare.getBoundsInParent();
        int halfTheSize = MAP_N_SQUARED / 2;
        double maxY = pressedSquare.getCol() > halfTheSize ? boundsInParent.getMinY() - 90 : boundsInParent.getMaxY();
        double maxX = pressedSquare.getRow() > halfTheSize ? boundsInParent.getMinX() - 90 : boundsInParent.getMaxX();
        numberBoard.setPadding(new Insets(maxY, 0, 0, maxX));
        numberBoard.setVisible(true);
        handleMouseMoved(ev);
    }

    public void handleMouseReleased(MouseEvent s) {
        Optional<NumberButton> findFirst = numberOptions.stream()
                .filter(e -> e.getBoundsInParent().contains(s.getX(), s.getY())).findFirst();
        if (pressedSquare != null && findFirst.isPresent()) {
            NumberButton node = findFirst.get();
            pressedSquare.setNumber(node.getNumber());
            updatePossibilities();
            pressedSquare = null;
        }
        numberBoard.setVisible(false);
        if (isFullyFilled()) {
            CommonsFX.displayDialog("You Won", "Reset", this::reset);
        }
    }

    public void solve() {
        boolean changed = true;
        while (changed) {
            changed = false;
            setSquareWithOnePossibility();
            for (int i = 0; i < MAP_NUMBER; i++) {
                for (int j = 0; j < MAP_NUMBER; j++) {
                    int row = i * MAP_NUMBER;
                    int col = j * MAP_NUMBER;
                    for (int k = 0; k < MAP_N_SQUARED; k++) {
                        int number = k;
                        List<SudokuSquare> squares = sudokuSquares.stream().filter(
                                e -> e.isEmpty() && e.getPossibilities().contains(number) && e.isInArea(row, col))
                                .collect(Collectors.toList());
                        if (squares.size() == 2) {
                            twoPossible(row, col, number, squares);
                        }
                        if (squares.size() == 1) {
                            changed = oneSolution(number, squares);
                        }
                    }
                }
            }
            setSquareWithOnePossibility();
        }
    }

    private void createRandomNumbers() {
        List<Integer> numbers = IntStream.rangeClosed(1, MAP_N_SQUARED).boxed().collect(Collectors.toList());
        int nTries = 0;
        for (int i = 0; i < MAP_N_SQUARED; i++) {
            for (int j = 0; j < MAP_N_SQUARED; j++) {
                int row = i;
                int col = j;
                Collections.shuffle(numbers);
                Optional<Integer> fitNumbers = numbers.stream().filter(n -> isNumberFit(n, row, col)).findFirst();
                getMapAt(i, j).setPermanent(true);
                if (!fitNumbers.isPresent()) {
                    nTries++;
                    j = -1;
                    sudokuSquares.stream().filter(e -> e.isInRow(row)).forEach(SudokuSquare::setEmpty);
                    if (nTries > 100) {
                        i = -1;
                        nTries = 0;
                        sudokuSquares.forEach(SudokuSquare::setEmpty);
                        break;
                    }
                } else {
                    getMapAt(i, j).setNumber(fitNumbers.get());
                }
            }
        }
    }

    private void initialize() {
        numberBoard.setVisible(false);
        for (int i = 0; i < MAP_N_SQUARED; i++) {
            for (int j = 0; j < MAP_N_SQUARED; j++) {
                SudokuSquare sudokuSquare = new SudokuSquare(i, j);
                sudokuSquare.setPermanent(true);
                sudokuSquares.add(sudokuSquare);
            }
        }
        for (int i = 0; i < MAP_NUMBER; i++) {
            for (int j = 0; j < MAP_NUMBER; j++) {
                NumberButton child = new NumberButton(i * MAP_NUMBER + j + 1);
                numberOptions.add(child);
                numberBoard.add(child, j, i);
            }
        }
        NumberButton child = new NumberButton(0);
        numberOptions.add(child);
        numberBoard.add(child, 3, 0);
        reset();
    }

    private boolean isFullyFilled() {
        return sudokuSquares.stream().allMatch(e -> !e.isEmpty() && !e.isWrong());
    }

    private boolean isNumberFit(int n, int row, int col) {
        return sudokuSquares.stream().filter(e -> !e.isInPosition(row, col)).filter(s -> s.isInRow(row))
                .noneMatch(s -> s.getNumber() == n)
                && sudokuSquares.stream().filter(e -> !e.isInPosition(row, col)).filter(s -> s.isInArea(row, col))
                        .noneMatch(s -> s.getNumber() == n)
                && sudokuSquares.stream().filter(e -> !e.isInPosition(row, col)).filter(s -> s.isInCol(col))
                        .noneMatch(s -> s.getNumber() == n);
    }

    private boolean isNumberFit(SudokuSquare sudokuSquare, int n) {
        return isNumberFit(n, sudokuSquare.getRow(), sudokuSquare.getCol());
    }

    private boolean oneSolution(int number, List<SudokuSquare> squares) {
        SudokuSquare sq = squares.get(0);
        sq.setNumber(number);
        LOG.info("{} {}", number, sq);
        updatePossibilities();
        return true;
    }

    private void reset() {
        createRandomNumbers();
        List<SudokuSquare> all = sudokuSquares.stream().collect(Collectors.toList());
        Collections.shuffle(all);
        for (int i = 0; i < all.size(); i++) {
            SudokuSquare sudokuSquare = all.get(i);
            int previousN = sudokuSquare.setEmpty();
            updatePossibilities();
            solve();
            if (isFullyFilled()) {
                sudokuSquare.setPermanent(false);
            } else {
                sudokuSquare.setNumber(previousN);
            }
            sudokuSquares.stream().filter(t -> !t.isPermanent()).forEach(SudokuSquare::setEmpty);
        }
        updatePossibilities();
    }

    private void setSquareWithOnePossibility() {
        while (sudokuSquares.stream().anyMatch(e -> e.isEmpty() && e.getPossibilities().size() == 1)) {
            sudokuSquares.stream().filter(e -> e.isEmpty() && e.getPossibilities().size() == 1).forEach(sq -> {
                Integer number = sq.getPossibilities().get(0);
                sq.setNumber(number);
                LOG.info("{} {}", number, sq);
            });
            updatePossibilities();
        }
    }

    private void twoPossible(int row, int col, int number, List<SudokuSquare> squares) {
        LOG.info("corredor {} ({},{})", number, row, col);
        SudokuSquare sq1 = squares.get(1);
        SudokuSquare sq0 = squares.get(0);
        if (sq0.getRow() == sq1.getRow()) {
            for (int l = 0; l < MAP_N_SQUARED; l++) {
                SudokuSquare mapAt = getMapAt(sq0.getRow(), l);
                if (!mapAt.isInArea(row, col)) {
                    LOG.info("removing {} from {}", number, mapAt);
                    mapAt.getPossibilities().remove(Integer.valueOf(number));
                }
            }
        }
        if (sq0.getCol() == sq1.getCol()) {
            for (int l = 0; l < MAP_N_SQUARED; l++) {
                SudokuSquare mapAt = getMapAt(l, sq0.getCol());
                if (!mapAt.isInArea(row, col)) {
                    LOG.info("removing {} from {}", number, mapAt);
                    mapAt.getPossibilities().remove(Integer.valueOf(number));
                }
            }
        }
    }

    private void updatePossibilities() {
        sudokuSquares.forEach(sq -> sq.setPossibilities(IntStream.rangeClosed(1, MAP_N_SQUARED)
                .filter(n -> isNumberFit(sq, n)).boxed().collect(Collectors.toList())));
        sudokuSquares.forEach(sq -> sq.setWrong(!sq.isEmpty() && !sq.getPossibilities().contains(sq.getNumber())));
    }
}