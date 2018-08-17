package gaming.ex17;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import simplebuilder.ResourceFXUtils;

public class PuzzleModel extends Group {

    public static final int PUZZLE_WIDTH = 8;
    public static final int PUZZLE_HEIGHT = 4;

    private PuzzlePiece[][] puzzle = initializePieces();
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

	private void addDragEvents(PuzzlePiece piece) {
        piece.setOnMousePressed(e -> onMousePressed(piece, e));
        piece.setOnMouseDragged(e -> onMouseDragged(piece, e));
        piece.setOnMouseReleased(e -> onMouseReleased(piece));
    }

    private void onMousePressed(PuzzlePiece piece, MouseEvent e) {
        if (intersectedPoint == null) {
            intersectedPoint = e.getPickResult().getIntersectedPoint();
            source = e.getSource();
            piece.toFront();
        }
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

    private void onMouseReleased(PuzzlePiece piece) {
        List<List<PuzzlePiece>> collect = linkedPieces.stream().filter(l -> !l.contains(piece)).collect(toList());
        for (int i = 0; i < collect.size(); i++) {
        	for (int j = 0; j < collect.get(i).size(); j++) {
        		PuzzlePiece puzzlePiece = collect.get(i).get(j);
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

	private Optional<List<PuzzlePiece>> groupWhichContains(PuzzlePiece p) {
		return linkedPieces.stream().filter(l -> l.contains(p))
				.findAny();
	}

	private double yDistance(PuzzlePiece puzzlePiece, PuzzlePiece p) {
		return (-puzzlePiece.getY() + p.getY()) * height + puzzlePiece.getLayoutY() - p.getLayoutY();
	}

    private boolean checkNeighbours(PuzzlePiece p, PuzzlePiece puzzlePiece) {
        return Math.abs(puzzlePiece.getX() - p.getX()) == 1 && puzzlePiece.getY() - p.getY() == 0
                || Math.abs(puzzlePiece.getY() - p.getY()) == 1 && puzzlePiece.getX() - p.getX() == 0;
    }

    private double distance(PuzzlePiece a, PuzzlePiece b) {
		double d = xDistance(a, b);
		double e = yDistance(a, b);
        return d * d + e * e;
    }

	private double xDistance(PuzzlePiece a, PuzzlePiece b) {
		return (-a.getX() + b.getX()) * width + a.getLayoutX() - b.getLayoutX();
	}

    private PuzzlePiece[][] initializePieces() {
        Image image = new Image(ResourceFXUtils.toURL("The_Horse_in_Motion.jpg").toString());
        width = (int) (image.getWidth() / PUZZLE_WIDTH);
        height = (int) (image.getHeight() / PUZZLE_HEIGHT);

		Random random = new Random();
        PuzzlePiece[][] puzzlePieces = new PuzzlePiece[PUZZLE_WIDTH][PUZZLE_HEIGHT];
        for (int i = 0; i < PUZZLE_WIDTH; i++) {
            for (int j = 0; j < PUZZLE_HEIGHT; j++) {
                puzzlePieces[i][j] = new PuzzlePiece(i, j, width, height);
				puzzlePieces[i][j].setLayoutX(random.nextDouble() * (PUZZLE_WIDTH - 1) * width);
				puzzlePieces[i][j].setLayoutY(random.nextDouble() * (PUZZLE_HEIGHT - 1) * height);
                puzzlePieces[i][j].setImage(image);
            }
        }
		PuzzlePath[] values = { PuzzlePath.ROUND, PuzzlePath.ZIGZAGGED, PuzzlePath.SQUARE, PuzzlePath.WAVE };
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

}
