package gaming.ex17;

import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;

public class PuzzlePiece extends Group {
    public static final double SQRT_2 = Math.sqrt(0.5);
    private PuzzlePath down = PuzzlePath.STRAIGHT;
    private Image image;
    private ImagePattern imagePattern;
    private PuzzlePath left = PuzzlePath.STRAIGHT;
    private Path path;
    private PuzzlePath right = PuzzlePath.STRAIGHT;
    private PuzzlePath up = PuzzlePath.STRAIGHT;
    private double width = 50, height = 50;
    private int x = 0, y = 0;
    public PuzzlePiece(int x, int y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;


    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
    public void move(Point3D subtract) {
        setLayoutX(getLayoutX() + subtract.getX());
        setLayoutY(getLayoutY() + subtract.getY());
    }

	public void move(double x1, double y1) {
		setLayoutX(getLayoutX() + x1);
		setLayoutY(getLayoutY() + y1);
    }

    // public void draw(GraphicsContext gc) {
    // gc.beginPath();
    //
    // gc.setStroke(Color.ALICEBLUE);
    //
    // gc.setFillRule(FillRule.EVEN_ODD);
    // gc.setFill(getImagePattern());
    //
    // String string = getPosition();
    // String svgpath = string + up.getPath(1 * width, 0) + right.getPath(0, 1 *
    // height) + down.getPath(-1 * width, 0)
    // + left.getPath(0, -1 * height);
    // gc.appendSVGPath(svgpath);
    // gc.fill();
    // gc.stroke();
    // gc.closePath();
    // }

    public ImagePattern getImagePattern() {
        if (imagePattern == null) {
            imagePattern = new ImagePattern(image, -x * width, -y * height, image.getWidth(), image.getHeight(), false);
        }
        return imagePattern;
    }

    public PuzzlePath getDown() {
        return down;
    }

    public PuzzlePath getLeft() {
        return left;
    }

    public Path getPath() {
        if (path == null) {
            path = new Path();

            path.getElements().add(new MoveTo(0, 0));
            path.getElements().addAll(up.getPath(width, 0));
            path.getElements().addAll(right.getPath(0, height));
            path.getElements().addAll(down.getPath(-width, 0));
            path.getElements().addAll(left.getPath(0, -height));

			ImagePattern imagePattern1 = getImagePattern();
            path.prefWidth(width);
            path.prefHeight(height);
			path.setFill(imagePattern1);
        }
        return path;
    }

    public PuzzlePath getRight() {
        return right;
    }

    public PuzzlePath getUp() {
        return up;
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
