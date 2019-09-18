package paintexp.tool;

import static utils.DrawOnPoint.within;
import static utils.DrawOnPoint.withinImage;

import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.PixelFormat.Type;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import utils.PixelHelper;

public class RectBuilder extends PaintTool {
    private double startX;
    private double endX;
    private double startY;
    private double endY;
    private double height;
    private double width;
    private double arc;

    private double radiusX = Math.min(arc, width / 2);
    private double radiusY = Math.min(arc, height / 2);
    private double centerY1 = Math.min(startY + radiusY, startY + height / 2);
    private double centerY2 = Math.max(endY - radiusY, startY - height / 2);
    private double centerX1 = Math.min(startX + radiusX, startX + width / 2);
    private double centerX2 = Math.max(endX - radiusX, endX - width / 2);

    public RectBuilder arc(double value) {
        arc = value;
        update();
        return this;
    }

    public RectBuilder bound(Bounds boundsInLocal) {
        startX = boundsInLocal.getMinX();
        endX = boundsInLocal.getMaxX();
        startY = boundsInLocal.getMinY();
        endY = boundsInLocal.getMaxY();
        height = boundsInLocal.getHeight();
        width = boundsInLocal.getWidth();
        update();
        return this;
    }

    public void copyImagePart(final Image srcImage, final WritableImage destImage, final Color ignoreColor) {
        int argb = PixelHelper.toArgb(ignoreColor);
        boolean isTransparent = Color.TRANSPARENT.equals(ignoreColor);
        PixelReader pixelReader = srcImage.getPixelReader();
        double srcWidth = srcImage.getWidth();
        double srcHeight = srcImage.getHeight();
        PixelWriter pixelWriter = destImage.getPixelWriter();
        Type type = pixelReader.getPixelFormat().getType();
        double destWidth = destImage.getWidth();
        double destHeight = destImage.getHeight();
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (within(i + endX, destWidth) && within(j + endY, destHeight) && within(i + startX, srcWidth)
                        && within(j + startY, srcHeight)) {
                    int color = pixelReader.getArgb(i + (int) startX, j + (int) startY);
                    if (Type.BYTE_BGRA_PRE == type) {
                        Color color2 = pixelReader.getColor(i + (int) startX, j + (int) startY);
                        color = PixelHelper.toArgb(Color.hsb(color2.getHue(), color2.getSaturation(),
                                color2.getBrightness(), color2.getOpacity()));
                    }
                    if (color != argb) {
                        pixelWriter.setArgb(i + (int) endX, j + (int) endY, isTransparent ? color | 0xFF000000 : color);
                    }
                }
            }
        }
    }

    @Override
    public Node createIcon() {
        return null;
    }

    public void drawFill(final PaintModel model) {
        final double x = centerX1;
        final double y = startY;
        new RectBuilder().startX(x).startY(y).width(centerX2 - centerX1).height(endY - startY).drawRect(model,
            model.getBackColor());
        final double x1 = startX;
        final double y1 = centerY1;
        new RectBuilder().startX(x1).startY(y1).width(endX - startX).height(centerY2 - centerY1).drawRect(model,
            model.getBackColor());
        for (int i = 0; i < radiusX; i++) {
            drawCirclePart(model, centerX1, centerY1, i, radiusY, Math.PI, model.getBackColor());
            drawCirclePart(model, centerX2, centerY1, i, radiusY, Math.PI * 3 / 2, model.getBackColor());
            drawCirclePart(model, centerX1, centerY2, i, radiusY, Math.PI / 2, model.getBackColor());
            drawCirclePart(model, centerX2, centerY2, i, radiusY, 0, model.getBackColor());
        }
    }

    public void drawRect(final PaintModel model, final Color backColor) {
        int startX2 = (int) startX;
        int startY2 = (int) startY;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (withinImage(startX2 + i, startY2 + j, model.getImage())) {
                    model.getImage().getPixelWriter().setColor(startX2 + i, startY2 + j, backColor);
                }
            }
        }
    }

    public void drawRect(final PaintModel model, final Color backColor, final double opacity) {
        int startX2 = (int) startX;
        int startY2 = (int) startY;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (withinImage(startX2 + i, startY2 + j, model.getImage())) {
                    PaintTool.drawPointTransparency(model, startX2 + i, startY2 + j, backColor, opacity);
                }
            }
        }
    }

    public void drawStroke(final PaintModel model) {
        drawLine(model, startX, centerY1, startX, centerY2);//LEFT
        drawLine(model, endX, centerY1, endX, centerY2);//RIGHT
        drawLine(model, centerX1, startY - 1, centerX2, startY - 1);//TOP
        drawLine(model, centerX1, endY, centerX2, endY);// BOTTOM
        drawCircle(model, centerX1, centerY1, radiusX, radiusY, Math.PI);//TOP-LEFT
        drawCircle(model, centerX2, centerY1, radiusX, radiusY, Math.PI * 3 / 2);//
        drawCircle(model, centerX1, centerY2, radiusX, radiusY, Math.PI / 2);
        drawCircle(model, centerX2, centerY2, radiusX, radiusY, 0);
    }

    public RectBuilder endX(double value) {
        endX = value;
        update();
        return this;
    }

    public RectBuilder endY(double value) {
        endY = value;
        update();
        return this;
    }

    public RectBuilder height(double value) {
        height = value;
        update();
        return this;
    }

    public RectBuilder startX(double value) {
        startX = value;
        update();
        return this;
    }

    public RectBuilder startY(double value) {
        startY = value;
        update();
        return this;
    }

    public RectBuilder width(double value) {
        width = value;
        update();
        return this;
    }

    protected void drawRect(final PaintModel model, final int color) {
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (withinImage(startX + i, startY + j, model.getImage())) {
                    int argb = model.getImage().getPixelReader().getArgb((int) startX + i, (int) startY + j);
                    if (argb == color) {
                        model.getImage().getPixelWriter().setColor((int) startX + i, (int) startY + j,
                                model.getBackColor());
                    }
                }
            }
        }
    }

    private void update() {
        radiusX = Math.min(arc, width / 2);
        radiusY = Math.min(arc, height / 2);
        centerY1 = Math.min(startY + radiusY, startY + height / 2);
        centerY2 = Math.max(endY - radiusY, startY - height / 2);
        centerX1 = Math.min(startX + radiusX, startX + width / 2);
        centerX2 = Math.max(endX - radiusX, endX - width / 2);
    }

    public static void moveArea(KeyCode code, Rectangle area2) {
        switch (code) {
            case RIGHT:
                area2.setLayoutX(area2.getLayoutX() + 1);
                break;
            case LEFT:
                area2.setLayoutX(area2.getLayoutX() - 1);
                break;
            case DOWN:
                area2.setLayoutY(area2.getLayoutY() + 1);
                break;
            case UP:
                area2.setLayoutY(area2.getLayoutY() - 1);
                break;
            default:
                break;
        }
    }
}