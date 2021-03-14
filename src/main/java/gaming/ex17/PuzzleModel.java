package gaming.ex17;

import static java.util.stream.Collectors.toList;

import java.util.*;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import utils.ResourceFXUtils;

public class PuzzleModel extends Group {

    private static final int PUZZLE_WIDTH = 8;
    private static final int PUZZLE_HEIGHT = 4;

    private PuzzlePiece[][] puzzle = initializePieces();
    private int width;
    private int height;
    private Point3D intersectedPoint;
    private Object source;
    private List<List<PuzzlePiece>> linkedPieces = new LinkedList<>();

    public PuzzleModel() {
        for (int i = 0; i < PUZZLE_WIDTH; i++) {
            for (int j = 0; j < PUZZLE_HEIGHT; j++) {
                getChildren().add(puzzle[i][j]);
                addDragEvents(puzzle[i][j]);
                List<PuzzlePiece> e = new ArrayList<>();
                e.add(puzzle[i][j]);
                linkedPieces.add(e);
            }
        }
    }

    private void addDragEvents(PuzzlePiece piece) {
        piece.setOnMousePressed(e -> onMousePressed(piece, e));
        piece.setOnMouseDragged(e -> onMouseDragged(piece, e));
        piece.setOnMouseReleased(e -> onMouseReleased(piece));
    }

    private double distance(PuzzlePiece a, PuzzlePiece b) {
        double d = xDistance(a, b);
        double e = yDistance(a, b);
        return d * d + e * e;
    }

    private Optional<List<PuzzlePiece>> groupWhichContains(PuzzlePiece p) {
        return linkedPieces.stream().filter(l -> l.contains(p)).findAny();
    }

    private PuzzlePiece[][] initializePieces() {
        Image image = new Image(ResourceFXUtils.toURL("The_Horse_in_Motion.jpg").toString());
        width = (int) (image.getWidth() / PUZZLE_WIDTH);
        height = (int) (image.getHeight() / PUZZLE_HEIGHT);

        PuzzlePiece[][] puzzlePieces = new PuzzlePiece[PUZZLE_WIDTH][PUZZLE_HEIGHT];
        for (int i = 0; i < PUZZLE_WIDTH; i++) {
            for (int j = 0; j < PUZZLE_HEIGHT; j++) {
                puzzlePieces[i][j] = new PuzzlePiece(i, j, width, height);
                puzzlePieces[i][j].setLayoutX(Math.random() * (PUZZLE_WIDTH - 1) * width);
                puzzlePieces[i][j].setLayoutY(Math.random() * (PUZZLE_HEIGHT - 1) * height);
                puzzlePieces[i][j].setImage(image);
            }
        }
        List<PuzzlePath> values = Arrays.asList(PuzzlePath.ROUND, PuzzlePath.ZIGZAGGED, PuzzlePath.SQUARE,
            PuzzlePath.WAVE, PuzzlePath.ROUNDED, PuzzlePath.ROUNDED_2);
        for (int i = 0; i < PUZZLE_WIDTH; i++) {
            for (int j = 0; j < PUZZLE_HEIGHT; j++) {
                Collections.shuffle(values);
                PuzzlePath path = values.get(0);
                if (i < PUZZLE_WIDTH - 1) {
                    puzzlePieces[i][j].setRight(path);
                    puzzlePieces[i + 1][j].setLeft(path);
                }
                if (j < PUZZLE_HEIGHT - 1) {
                    puzzlePieces[i][j].setDown(path);
                    puzzlePieces[i][j + 1].setUp(path);
                }
                puzzlePieces[i][j].getChildren().add(puzzlePieces[i][j].getPath());

            }
        }

        return puzzlePieces;
    }

    private void onMouseDragged(PuzzlePiece piece, MouseEvent e) {
        if (intersectedPoint == null || source != e.getSource()) {
            return;
        }
        Point3D intersectedPoint2 = e.getPickResult().getIntersectedPoint();
        Point3D subtract = intersectedPoint2.subtract(intersectedPoint);
        Optional<List<PuzzlePiece>> findAny = groupWhichContains(piece);
        if (findAny.isPresent() && subtract.magnitude() <= width) {

            findAny.get().forEach(i -> i.move(subtract));

        }
    }

    private void onMousePressed(PuzzlePiece piece, MouseEvent e) {
        if (intersectedPoint == null) {
            intersectedPoint = e.getPickResult().getIntersectedPoint();
            source = e.getSource();
            piece.toFront();
        }
    }

    private void onMouseReleased(PuzzlePiece piece) {
        List<List<PuzzlePiece>> piecesGroups = linkedPieces.stream().filter(l -> !l.contains(piece)).collect(toList());
        for (int i = 0; i < piecesGroups.size(); i++) {
            for (int j = 0; j < piecesGroups.get(i).size(); j++) {
                PuzzlePiece puzzlePiece = piecesGroups.get(i).get(j);
                if (checkNeighbours(piece, puzzlePiece) && distance(puzzlePiece, piece) < width * width / 4) {
                    Optional<List<PuzzlePiece>> containsP = groupWhichContains(piece);
                    Optional<List<PuzzlePiece>> containsPuzzle = groupWhichContains(puzzlePiece);
                    if (containsP.isPresent() && containsPuzzle.isPresent()
                        && !containsP.get().equals(containsPuzzle.get())) {
                        containsPuzzle.get().addAll(containsP.get());

                        linkedPieces.remove(containsP.get());
                        double a = xDistance(puzzlePiece, piece);
                        double b = yDistance(puzzlePiece, piece);
                        containsPuzzle.get().forEach(PuzzlePiece::toFront);
                        containsP.get().forEach(z -> z.move(a, b));
                        return;
                    }
                }
            }
        }
        intersectedPoint = null;
    }

    private double xDistance(PuzzlePiece a, PuzzlePiece b) {
        return (-a.getX() + b.getX()) * width + a.getLayoutX() - b.getLayoutX();
    }

    private double yDistance(PuzzlePiece puzzlePiece, PuzzlePiece p) {
        return (-puzzlePiece.getY() + p.getY()) * height + puzzlePiece.getLayoutY() - p.getLayoutY();
    }

    private static boolean checkNeighbours(PuzzlePiece p, PuzzlePiece puzzlePiece) {
        return Math.abs(puzzlePiece.getX() - p.getX()) == 1 && puzzlePiece.getY() - p.getY() == 0
            || Math.abs(puzzlePiece.getY() - p.getY()) == 1 && puzzlePiece.getX() - p.getX() == 0;
    }

}
