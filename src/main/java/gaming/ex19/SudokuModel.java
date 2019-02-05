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
        updatePossibilities();
        sudokuSquares.stream().filter(SudokuSquare::isNotEmpty).forEach(e -> e.setPermanent(true));
        boolean changed = true;
        while (changed) {
            changed = false;
            setSquareWithOnePossibility();
            for (int i = 0; i < MAP_N_SQUARED; i++) {
                for (int k = 0; k <= MAP_N_SQUARED; k++) {
                    int number = k;
                    List<SudokuSquare> area = getArea(i, number);
                    if (area.size() == 1) {
                        oneSolution(number, area);
                        changed = true;
                    }
                    List<SudokuSquare> row = getRow(i, number);
                    if (row.size() == 1) {
                        oneSolution(number, row);
                        changed = true;
                    }
                    List<SudokuSquare> col = getCol(i, number);
                    if (col.size() == 1) {
                        oneSolution(number, col);
                        changed = true;
                    }
                }
            }
            setSquareWithOnePossibility();
        }
    }

    private void clearPossibilities(List<SudokuSquare> squares) {
        for (int l = 0; l < squares.size(); l++) {
            SudokuSquare sq = squares.get(l);
            for (int k = l + 1; k < squares.size() && sq.getPossibilities().size() == 2; k++) {
                SudokuSquare sq2 = squares.get(k);
                if (Objects.equals(sq.getPossibilities(), sq2.getPossibilities())) {
                    removeFromCol(sq, sq2);
                    removeFromRow(sq, sq2);
                    removeFromArea(sq, sq2);
                }
            }
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

    private void display() {
        CommonsFX.displayDialog("Try to Solve", "Solve", () -> {
            solve();
            if (!isFullyFilled()) {
                display();
            }

        });
    }

    private List<SudokuSquare> getArea(int row) {
        return sudokuSquares.stream()
                .filter(e -> e.isEmpty() && e.isInArea(row % MAP_NUMBER * MAP_NUMBER, row / MAP_NUMBER * MAP_NUMBER))
                .collect(Collectors.toList());
    }

    private List<SudokuSquare> getArea(int i, int number) {
        return sudokuSquares.stream()
                .filter(e1 -> e1.isEmpty() && e1.isInArea(i % MAP_NUMBER * MAP_NUMBER, i / MAP_NUMBER * MAP_NUMBER))
                .filter(e -> e.getPossibilities().contains(number)).collect(Collectors.toList());
    }

    private List<SudokuSquare> getCol(int row) {
        return sudokuSquares.stream().filter(e -> e.isEmpty() && e.isInCol(row)).collect(Collectors.toList());
    }

    private List<SudokuSquare> getCol(int i, int number) {
        return sudokuSquares.stream().filter(e1 -> e1.isEmpty() && e1.isInCol(i))
                .filter(e -> e.getPossibilities().contains(number)).collect(Collectors.toList());
    }

    private List<SudokuSquare> getRow(int row) {
        return sudokuSquares.stream().filter(e -> e.isEmpty() && e.isInRow(row)).collect(Collectors.toList());
    }

    private List<SudokuSquare> getRow(int i, int number) {
        return sudokuSquares.stream().filter(e1 -> e1.isEmpty() && e1.isInRow(i))
                .filter(e -> e.getPossibilities().contains(number)).collect(Collectors.toList());
    }

    private void initialize() {
        numberBoard.setVisible(false);
        for (int i = 0; i < MAP_N_SQUARED; i++) {
            for (int j = 0; j < MAP_N_SQUARED; j++) {
                SudokuSquare sudokuSquare = new SudokuSquare(i, j);
                sudokuSquare.setPermanent(false);
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
        //        reset();

        display();
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

    private void oneSolution(int number, List<SudokuSquare> squares) {
        SudokuSquare sq = squares.get(0);
        sq.setNumber(number);
        LOG.info("{} {}", number, sq);
        updatePossibilities();
    }

    private void removeDuplicatedPossibilities() {
        for (int i = 0; i < MAP_N_SQUARED; i++) {
            for (int number = 1; number <= MAP_N_SQUARED; number++) {
                List<SudokuSquare> squares = getArea(i, number);
                if (squares.size() == 2) {
                    SudokuSquare sq0 = squares.get(0);
                    SudokuSquare sq1 = squares.get(1);
                    removeNumber(number, sq0, sq1);
                }
            }
        }
        for (int i = 0; i < MAP_N_SQUARED; i++) {
            clearPossibilities(getRow(i));
            clearPossibilities(getCol(i));
            clearPossibilities(getArea(i));
        }
    }

    private void removeFromArea(SudokuSquare sq, SudokuSquare sq2) {
        if (sq.getRow() / MAP_NUMBER == sq2.getRow() / MAP_NUMBER
                && sq.getCol() / MAP_NUMBER == sq2.getCol() / MAP_NUMBER) {
            int row = sq.getRow() / MAP_NUMBER;
            int col = sq.getCol() / MAP_NUMBER;
            for (int i = 0; i < MAP_NUMBER; i++) {
                for (int j = 0; j < MAP_NUMBER; j++) {
                    SudokuSquare mapAt = getMapAt(row * MAP_NUMBER + i, col * MAP_NUMBER + j);
                    if (!mapAt.equals(sq) && !mapAt.equals(sq2)) {
                        mapAt.getPossibilities().removeAll(sq.getPossibilities());
                    }
                }
            }
        }
    }

    private void removeFromCol(SudokuSquare sq, SudokuSquare sq2) {
        if (sq.getRow() == sq2.getRow()) {
            int row = sq.getRow();
            for (int j = 0; j < MAP_N_SQUARED; j++) {
                SudokuSquare mapAt = getMapAt(row, j);
                if (!mapAt.equals(sq) && !mapAt.equals(sq2)) {
                    mapAt.getPossibilities().removeAll(sq.getPossibilities());
                }
            }
        }
    }

    private void removeFromRow(SudokuSquare sq, SudokuSquare sq2) {
        if (sq.getCol() == sq2.getCol()) {
            int col = sq.getCol();
            for (int j = 0; j < MAP_N_SQUARED; j++) {
                SudokuSquare mapAt = getMapAt(j, col);
                if (!mapAt.equals(sq) && !mapAt.equals(sq2)) {
                    mapAt.getPossibilities().removeAll(sq.getPossibilities());
                }
            }
        }
    }

    private void removeNumber(int number, SudokuSquare sq0, SudokuSquare sq1) {
        boolean sameCol = sq0.getCol() == sq1.getCol();
        boolean sameRow = sq0.getRow() == sq1.getRow();
        if (sameCol || sameRow) {
            for (int l = 0; l < MAP_N_SQUARED; l++) {
                int row = !sameRow ? l : sq0.getRow();
                int col = !sameCol ? l : sq0.getCol();
                SudokuSquare mapAt = getMapAt(row, col);
                if (!mapAt.isInArea(sq0.getRow(), sq0.getCol())) {
                    mapAt.getPossibilities().remove(Integer.valueOf(number));
                }

            }
        }
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

    private void updatePossibilities() {
        sudokuSquares.forEach(sq -> sq.setPossibilities(IntStream.rangeClosed(1, MAP_N_SQUARED)
                .filter(n -> isNumberFit(sq, n)).boxed().collect(Collectors.toList())));
        sudokuSquares.forEach(sq -> sq.setWrong(!sq.isEmpty() && !sq.getPossibilities().contains(sq.getNumber())));

        removeDuplicatedPossibilities();
    }
}