package gaming.ex17;

import javafx.beans.NamedArg;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Path;
import simplebuilder.SimplePathBuilder;
import utils.ex.SupplierEx;

public class PuzzlePiece extends Group {
    private PuzzlePath down = PuzzlePath.STRAIGHT;
    private Image image;
    private ImagePattern imagePattern;
    private PuzzlePath left = PuzzlePath.STRAIGHT;
    private Path path;
    private PuzzlePath right = PuzzlePath.STRAIGHT;
    private PuzzlePath up = PuzzlePath.STRAIGHT;
    private double width = 50;
    private double height = 50;
    private int x;
    private int y;

    public PuzzlePiece() {
    }

    public PuzzlePiece(@NamedArg("x") int x, @NamedArg("y") int y, @NamedArg("width") double width,
        @NamedArg("height") double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public ImagePattern getImagePattern() {
        return SupplierEx.orElse(imagePattern, () -> imagePattern = new ImagePattern(image, -x * width, -y * height,
            image.getWidth(), image.getHeight(), false));
    }

    public Path getPath() {
        return SupplierEx.orElse(path,
            () -> path = new SimplePathBuilder().moveTo(0, 0).add(up.getPath(width, 0)).add(right.getPath(0, height))
                .add(down.getPath(-width, 0)).add(left.getPath(0, -height)).fill(getImagePattern()).prefWidth(width)
                .prefHeight(height).build());
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void move(double x1, double y1) {
        setLayoutX(getLayoutX() + x1);
        setLayoutY(getLayoutY() + y1);
    }

    public void move(Point3D subtract) {
        setLayoutX(getLayoutX() + subtract.getX());
        setLayoutY(getLayoutY() + subtract.getY());
    }

    public void setDown(PuzzlePath down) {
        this.down = down;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public void setLeft(PuzzlePath left) {
        this.left = left;
    }

    public void setRight(PuzzlePath right) {
        this.right = right;
    }

    public void setUp(PuzzlePath up) {
        this.up = up;
    }

}
