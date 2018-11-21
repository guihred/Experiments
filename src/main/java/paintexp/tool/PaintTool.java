package paintexp.tool;

import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.Bounds;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.*;
import javafx.scene.image.PixelFormat.Type;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import paintexp.PaintModel;
import simplebuilder.SimpleRectangleBuilder;

public abstract class PaintTool extends Group {
    public PaintTool() {
        getChildren().add(getIcon());
    }

    public abstract Node getIcon();

    public abstract Cursor getMouseCursor();

    @SuppressWarnings("unused")
    public void handleEvent(final MouseEvent e, final PaintModel model) {
        // DOES NOTHING
    }

    @SuppressWarnings("unused")
    public void handleKeyEvent(final KeyEvent e, final PaintModel paintModel) {
        // DOES NOTHING
    }

    @SuppressWarnings("unused")
    public void onSelected(final PaintModel model) {
        // DOES NOTHING
    }

    protected boolean containsPoint(final Node area2, final double localX, final double localY) {
        Bounds bounds = area2.getBoundsInParent();
        return area2.getLayoutX() < localX && localX < area2.getLayoutX() + bounds.getWidth()
                && area2.getLayoutY() < localY && localY < area2.getLayoutY() + bounds.getHeight();
    }

    protected void copyImagePart(final Image srcImage, final WritableImage destImage, final int x, final int y,
            final double width, final double height) {
        copyImagePart(srcImage, destImage, x, y, width, height, 0, 0);
    }

    protected void copyImagePart(final Image srcImage, final WritableImage destImage, final int x, final int y,
            final double width, final double height, final int xOffset, final int yOffset) {
        PixelReader pixelReader = srcImage.getPixelReader();
        double srcWidth = srcImage.getWidth();
        double srcHeight = srcImage.getHeight();
        PixelWriter pixelWriter = destImage.getPixelWriter();
        Type type = pixelReader.getPixelFormat().getType();
        double destWidth = destImage.getWidth();
        double destHeight = destImage.getHeight();
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (within(i + xOffset, destWidth) && within(j + yOffset, destHeight) && within(i + x, srcWidth)
                        && within(j + y, srcHeight)) {
                    Color color = pixelReader.getColor(i + x, j + y);
                    if (Type.BYTE_BGRA_PRE == type) {
                        color = Color.hsb(color.getHue(), color.getSaturation(), color.getBrightness());
                    }
                    pixelWriter.setColor(i + xOffset, j + yOffset, color);
                }
            }
        }
    }

    protected void copyImagePart(final Image srcImage, final WritableImage destImage, final int x, final int y,
            final double width, final double height, final int xOffset, final int yOffset, final Color ignoreColor) {
        PixelReader pixelReader = srcImage.getPixelReader();
        double srcWidth = srcImage.getWidth();
        double srcHeight = srcImage.getHeight();
        PixelWriter pixelWriter = destImage.getPixelWriter();
        Type type = pixelReader.getPixelFormat().getType();
        double destWidth = destImage.getWidth();
        double destHeight = destImage.getHeight();
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (within(i + xOffset, destWidth) && within(j + yOffset, destHeight) && within(i + x, srcWidth)
                        && within(j + y, srcHeight)) {
                    Color color = pixelReader.getColor(i + x, j + y);
                    if (Type.BYTE_BGRA_PRE == type) {
                        color = Color.hsb(color.getHue(), color.getSaturation(), color.getBrightness());
                    }
                    if (!color.equals(ignoreColor)) {
                        pixelWriter.setColor(i + xOffset, j + yOffset, color);
                    }
                }
            }
        }
    }

    protected void drawCircle(final PaintModel model, final double centerX, final double centerY, final double radiusX,
            final double radiusY, final double nPoints, final double startAngle, final double angle) {
        Color frontColor = model.getFrontColor();

        drawCircle(model, centerX, centerY, radiusX, radiusY, nPoints, startAngle, angle, frontColor);
    }

    protected void drawCircle(final PaintModel model, final double centerX, final double centerY, final double radiusX,
            final double radiusY, final double nPoints, final double startAngle, final double angle, Color frontColor) {
        for (double t = 0; t < angle; t += 2 * Math.PI / nPoints) {
            int x = (int) Math.round(radiusX * Math.cos(t + startAngle));
            int y = (int) Math.round(radiusY * Math.sin(t + startAngle));
            drawPoint(model, x + (int) centerX, y + (int) centerY, frontColor);
        }
    }

    protected void drawCircle(final PaintModel model, final int centerX, final int centerY, final double radiusX,
            final double radiusY, final double nPoints, Color color) {

        for (double t = 0; t < 2 * Math.PI; t += 2 * Math.PI / nPoints) {
            int x = (int) Math.round(radiusX * Math.cos(t));
            int y = (int) Math.round(radiusY * Math.sin(t));
            drawPoint(model, x + centerX, y + centerY, color);
        }
    }

    protected void drawLine(final PaintModel model, final double startX, final double startY, final double endX,
            final double endY) {
        drawLine(model, startX, startY, endX, endY,
                (x, y) -> model.getImage().getPixelWriter().setColor(x, y, model.getFrontColor()));
    }

    protected void drawLine(final PaintModel model, final double startX, final double startY, final double endX,
            final double endY, final DrawOnPoint onPoint) {
        double d = endX - startX;
        double a = d == 0 ? Double.NaN : (endY - startY) / d;
        double b = Double.isNaN(a) ? Double.NaN : endY - a * endX;

        double minX = Double.min(startX, endX);
        double maxX = Double.max(startX, endX);
        for (int x = (int) minX; x < maxX; x++) {
            int y = (int) (!Double.isNaN(a) ? Math.round(a * x + b) : endY);
            if (withinRange(x, y, model)) {
                onPoint.draw(x, y);
            }
        }

        double minY = Double.min(startY, endY);
        double maxY = Double.max(startY, endY);
        for (int y = (int) minY; y < maxY; y++) {
            if (a != 0) {
                int x = (int) (Double.isNaN(a) ? startX : Math.round((y - b) / a));
                if (withinRange(x, y, model)) {
                    onPoint.draw(x, y);
                }
            }
        }
    }

    protected void drawPoint(final PaintModel model, final int x2, final int y2) {
        Color frontColor = model.getFrontColor();
        drawPoint(model, x2, y2, frontColor);
    }

    protected void drawRect(final PaintModel model, final double x, final double y, final double w, final double h) {
        drawRect(model, x, y, w, h, model.getBackColor());
    }

    protected void drawSquare(final PaintModel model, final int x, final int y, final int w, final Color backColor) {
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < w; j++) {
                if (withinRange(x + i, y + j, model)) {
                    model.getImage().getPixelWriter().setColor(x + i, y + j, backColor);
                }
            }
        }
    }

    protected void drawSquare(final PaintModel model, final int x, final int y, final int w, final int color) {
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < w; j++) {
                if (withinRange(x + i, y + j, model)) {
                    int argb = model.getImage().getPixelReader().getArgb(x + i, y + j);
                    if (argb == color) {
                        model.getImage().getPixelWriter().setColor(x + i, y + j, model.getBackColor());
                    }
                }
            }
        }
    }

    protected void makeResizable(final Node node) {
        Pane p = (Pane) node.getParent();
        p.getChildren().addAll(IntStream.range(0, 9).filter(i -> i != 4).mapToObj(i -> makeSquare(node, i))
                .collect(Collectors.toList()));

    }

    protected Rectangle makeSquare(final Node node, final int i) {
        Rectangle rectangle = new SimpleRectangleBuilder().width(5).height(5).fill(Color.WHITE).stroke(Color.BLACK)
                .managed(false).build();
        node.boundsInParentProperty().addListener((a, b, c) -> {
            rectangle.setLayoutX(i / 3 * (c.getWidth() / 2) + c.getMinX() - rectangle.getWidth() / 2);
            rectangle.setLayoutY(i % 3 * (c.getHeight() / 2) + c.getMinY() - rectangle.getHeight() / 2);
        });

        final Cursor vResize = i % 3 == 1 ? Cursor.H_RESIZE
                : i / 3 == 1 ? Cursor.V_RESIZE : i / 3 == i % 3 ? Cursor.SE_RESIZE : Cursor.NE_RESIZE;
        rectangle.setCursor(vResize);
        rectangle.addEventHandler(MouseEvent.ANY, new EventHandler<MouseEvent>() {
            private double initialX;
            private double initialY;

            @Override
            public void handle(final MouseEvent event) {
                EventType<? extends MouseEvent> eventType = event.getEventType();
                if (MouseEvent.MOUSE_PRESSED.equals(eventType)) {
                    initialX = event.getX();
                    initialY = event.getY();
                }
                if (MouseEvent.MOUSE_DRAGGED.equals(eventType)) {
                    if (vResize == Cursor.V_RESIZE || vResize == Cursor.SE_RESIZE) {
                        double height = node.getBoundsInParent().getHeight();
                        node.setScaleY((height + initialY - event.getY()) / height);
                    }
                    if (vResize == Cursor.H_RESIZE || vResize == Cursor.SE_RESIZE) {
                        double width = node.getBoundsInParent().getWidth();
                        node.setScaleX((width + initialX - event.getX()) / width);
                    }
                }
                if (MouseEvent.MOUSE_RELEASED.equals(eventType)) {
                    initialX = event.getX();
                    initialY = event.getX();
                }
            }
        });
        return rectangle;
    }

    protected double setWithinRange(final double num, final double min, final double max) {
        return Double.min(Double.max(min, num), max);
    }

    protected void takeSnapshot(PaintModel model, Node line2) {
        Bounds bounds = line2.getBoundsInParent();
        int width = (int) bounds.getWidth() + 2;
        int height = (int) bounds.getHeight() + 2;
        SnapshotParameters params = new SnapshotParameters();
        params.setFill(model.getBackColor());
        WritableImage textImage = line2.snapshot(params, new WritableImage(width, height));
        int x = (int) bounds.getMinX();
        int y = (int) bounds.getMinY();
        copyImagePart(textImage, model.getImage(), 0, 0, width, height, x, y, model.getBackColor());
        model.getImageStack().getChildren().remove(line2);
        model.getImageStack().getChildren().clear();
        model.getImageStack().getChildren().add(new ImageView(model.getImage()));
    }

    protected void takeSnapshotFill(PaintModel model, Node line2) {
        Bounds bounds = line2.getBoundsInParent();
        int width = (int) bounds.getWidth() + 2;
        int height = (int) bounds.getHeight() + 2;
        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        WritableImage textImage = line2.snapshot(params, new WritableImage(width, height));
        int x = (int) bounds.getMinX();
        int y = (int) bounds.getMinY();
        copyImagePart(textImage, model.getImage(), 0, 0, width, height, x, y, Color.TRANSPARENT);
        model.getImageStack().getChildren().remove(line2);
        model.getImageStack().getChildren().clear();
        model.getImageStack().getChildren().add(new ImageView(model.getImage()));
    }

    protected boolean within(final int y, final double min) {
        return 0 <= y && y < min;
    }

    protected boolean within(final int y, final double min, final double max) {
        return min <= y && y < max;
    }

    protected boolean withinRange(final int x, final int y, final int initialX, final int initialY, final double bound,
            final PaintModel model) {
        return within(y, Double.max(initialY - bound, 0), Double.min(initialY + bound, model.getImage().getHeight()))
                && within(x, Double.max(initialX - bound, 0),
                        Double.min(initialX + bound, model.getImage().getWidth()));
    }

    protected boolean withinRange(final int x, final int y, final PaintModel model) {
        return within(y, model.getImage().getHeight()) && within(x, model.getImage().getWidth());
    }

    private void drawPoint(final PaintModel model, final int x2, final int y2, Color frontColor) {
        if (withinRange(x2, y2, model)) {
            model.getImage().getPixelWriter().setColor(x2, y2, frontColor);
        }
    }

    private void drawRect(final PaintModel model, final double x, final double y, final double w, final double h,
            Color backColor) {
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                if (withinRange((int) x + i, (int) y + j, model)) {
                    model.getImage().getPixelWriter().setColor((int) x + i, (int) y + j, backColor);
                }
            }
        }
    }

    @FunctionalInterface
    interface DrawOnPoint {
        void draw(int x, int y);
    }

}