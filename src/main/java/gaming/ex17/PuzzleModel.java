package gaming.ex17;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.image.Image;
import simplebuilder.ResourceFXUtils;

public class PuzzleModel extends Group {

    public static final int PUZZLE_WIDTH = 8;
    public static final int PUZZLE_HEIGHT = 4;

    PuzzlePiece[][] puzzle = initializePieces();
    private int width;
    private int height;
    private Point3D intersectedPoint;
    private Object source;
    private List<List<PuzzlePiece>> linkedPieces = new ArrayList<>();

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

    private void addDragEvents(PuzzlePiece p) {
        p.setOnMousePressed(e -> {
            if (intersectedPoint == null) {
                p.toFront();
                intersectedPoint = e.getPickResult().getIntersectedPoint();
                source = e.getSource();
            }
        });
        p.setOnMouseDragged(e -> {
            if (intersectedPoint == null || source != e.getSource()) {
                return;
            }
            p.toFront();
            Point3D intersectedPoint2 = e.getPickResult().getIntersectedPoint();
            Point3D subtract = intersectedPoint2.subtract(intersectedPoint);
            Optional<List<PuzzlePiece>> findAny = linkedPieces.stream().filter(l -> l.contains(p)).findAny();
            if (findAny.isPresent()) {
                findAny.get().forEach(i -> i.move(subtract));
            }
        });
        p.setOnMouseReleased(e -> {
            for (int i = 0; i < PUZZLE_WIDTH; i++) {
                for (int j = 0; j < PUZZLE_HEIGHT; j++) {
                    PuzzlePiece puzzlePiece = puzzle[i][j];
                    if (checkNeighbours(p, puzzlePiece)) {
                        if (distance(puzzlePiece, p) < width * width / 4) {
                            Optional<List<PuzzlePiece>> findAny = linkedPieces.stream().filter(l -> l.contains(p))
                                    .findAny();
                            Optional<List<PuzzlePiece>> findAny2 = linkedPieces.stream()
                                    .filter(l -> l.contains(puzzlePiece)).findAny();
                            if (findAny.isPresent() && findAny2.isPresent() && !findAny.get().equals(findAny2.get())) {
                                findAny2.get().addAll(findAny.get());
                                linkedPieces.remove(findAny.get());
                                double a = (-puzzlePiece.getX() + p.getX()) * width + puzzlePiece.getLayoutX() - p.getLayoutX();
                                double b = (-puzzlePiece.getY() + p.getY()) * height + puzzlePiece.getLayoutY() - p.getLayoutY();
                                findAny2.get().forEach(PuzzlePiece::toFront);
                                findAny.get().forEach(z -> z.move(a, b));
                            }
                        }
                    }
                }
            }
            intersectedPoint = null;

        });
    }

    private boolean checkNeighbours(PuzzlePiece p, PuzzlePiece puzzlePiece) {
        return Math.abs(puzzlePiece.getX() - p.getX()) == 1 && puzzlePiece.getY() - p.getY() == 0
                || Math.abs(puzzlePiece.getY() - p.getY()) == 1 && puzzlePiece.getX() - p.getX() == 0;
    }

    private double distance(PuzzlePiece a, PuzzlePiece b) {
        double d = a.getLayoutX() - b.getLayoutX();
        double e = a.getLayoutY() - b.getLayoutY();
        return d * d + e * e;
    }

    private PuzzlePiece[][] initializePieces() {
        Image image = new Image(ResourceFXUtils.toURL("The_Horse_in_Motion.jpg").toString());
        width = (int) (image.getWidth() / PUZZLE_WIDTH);
        height = (int) (image.getHeight() / PUZZLE_HEIGHT);

        PuzzlePiece[][] puzzlePieces = new PuzzlePiece[PUZZLE_WIDTH][PUZZLE_HEIGHT];
        for (int i = 0; i < PUZZLE_WIDTH; i++) {
            for (int j = 0; j < PUZZLE_HEIGHT; j++) {
                puzzlePieces[i][j] = new PuzzlePiece(i, j, width, height);
                puzzlePieces[i][j].setLayoutX(i * width);
                puzzlePieces[i][j].setLayoutY(j * height);
                puzzlePieces[i][j].setImage(image);
            }
        }
        PuzzlePath[] values = { PuzzlePath.ROUND, PuzzlePath.ZIGZAGGED, PuzzlePath.SQUARE };
        Random random = new Random();
        for (int i = 0; i < PUZZLE_WIDTH; i++) {
            for (int j = 0; j < PUZZLE_HEIGHT; j++) {
                PuzzlePath puzzlePath2 = values[random.nextInt(values.length)];
                if (i < PUZZLE_WIDTH - 1) {
                    puzzlePieces[i][j].setRight(puzzlePath2);
                    puzzlePieces[i + 1][j].setLeft(puzzlePath2);
                }
                if (j < PUZZLE_HEIGHT - 1) {
                    puzzlePieces[i][j].setDown(puzzlePath2);
                    puzzlePieces[i][j + 1].setUp(puzzlePath2);
                }
                puzzlePieces[i][j].getChildren().add(puzzlePieces[i][j].getPath());

            }
        }

        return puzzlePieces;
    }

    // public void draw(GraphicsContext gc) {
    // for (int i = 0; i < PUZZLE_WIDTH; i++) {
    // for (int j = 0; j < PUZZLE_HEIGHT; j++) {
    // puzzle[i][j].draw(gc);
    // }
    // }
    //
    // }

}
