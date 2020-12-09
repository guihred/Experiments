
package paintexp.tool;

import static utils.DrawOnPoint.within;
import static utils.DrawOnPoint.withinImage;

import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.*;
import javafx.scene.image.PixelFormat.Type;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Transform;
import utils.DrawOnPoint;
import utils.PixelHelper;

public final class RectBuilder {
    public static final int N_POINTS_MULTIPLIER = 16;
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

    private RectBuilder() {
    }

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
        for (int i = 0; i <= width; i++) {
            for (int j = 0; j <= height; j++) {
                if (isWithin(srcWidth, srcHeight, destWidth, destHeight, i, j)) {
                    int color = getColor(pixelReader, type, i, j);
                    if (color != argb) {
                        pixelWriter.setArgb(i + (int) endX, j + (int) endY, isTransparent ? color | 0xFF000000 : color);
                    }
                }
            }
        }
    }

    public void copyImagePartTransparency(Image srcImage, WritableImage destImage, WritableImage currentImage) {
        PixelReader pixelReader = srcImage.getPixelReader();
        double srcWidth = srcImage.getWidth();
        double srcHeight = srcImage.getHeight();
        double destWidth = destImage.getWidth();
        double destHeight = destImage.getHeight();
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (isWithin(srcWidth, srcHeight, destWidth, destHeight, i, j)) {
                    Color color2 = pixelReader.getColor(i + (int) startX, j + (int) startY);
                    Color color = PixelHelper.asColor(PixelHelper.toArgb(color2) | 0xFF000000);
                    RectBuilder.drawPointTransparency(i + (int) endX, j + (int) endY, color, color2.getOpacity(),
                            destImage, currentImage);
                }

            }
        }
    }

    public void drawCircle(WritableImage image, WritableImage currentImage, Color color, double opacity) {
        double nPoints = Math.max(width, height) * RectBuilder.N_POINTS_MULTIPLIER;
        for (double t = 0; t < 2 * Math.PI; t += 2 * Math.PI / nPoints) {
            int x = (int) Math.round(width * Math.cos(t));
            int y = (int) Math.round(height * Math.sin(t));
            RectBuilder.drawPointTransparency(x + (int) startX, y + (int) startY, color, opacity, image, currentImage);
        }
    }

    public void drawCircleFill(WritableImage image, WritableImage currentImage, Color color, double opacity) {
        for (int width2 = 0; width2 < width; width2++) {
            double nPoints = Math.max(width2, height) * RectBuilder.N_POINTS_MULTIPLIER;
            for (double t = 0; nPoints > 0 && t < 2 * Math.PI; t += 2 * Math.PI / nPoints) {
                int x = (int) Math.round(width2 * Math.cos(t));
                int y = (int) Math.round(height * Math.sin(t));
                RectBuilder.drawPointTransparency(x + (int) startX, y + (int) startY, color, opacity, image,
                        currentImage);
            }
        }
    }

    public void drawCirclePattern(WritableImage image, WritableImage currentImage, Color backcolor, double opacity) {
        for (double w = 0; w <= width; w++) {
            for (double h = 0; h <= height; h++) {
                double nPoints = Math.max(w, h) * RectBuilder.N_POINTS_MULTIPLIER;
                if (nPoints == 0) {
                    continue;
                }
                for (double t = 0; t < 2 * Math.PI; t += 2 * Math.PI / nPoints) {
                    int x = (int) Math.round(w * Math.cos(t));
                    int y = (int) Math.round(h * Math.sin(t));
                    Color color = backcolor;
                    if (withinImage(x + endX, y + endY, image)) {
                        color = image.getPixelReader().getColor(x + (int) endX, y + (int) endY);
                    }
                    RectBuilder.drawPointTransparency(x + (int) startX, y + (int) startY, color, opacity, image,
                            currentImage);
                }
            }
        }
    }

    public void drawFill(WritableImage image, Color backColor) {
        RectBuilder.build().startX(centerX1).startY(startY).width(centerX2 - centerX1).height(endY - startY)
                .drawRect(image, backColor);
        RectBuilder.build().startX(startX).startY(centerY1).width(endX - startX).height(centerY2 - centerY1)
                .drawRect(image, backColor);
        for (int i = 0; i < radiusX; i++) {
            RectBuilder.drawCirclePart(image, centerX1, centerY1, i, radiusY, Math.PI, backColor);
            RectBuilder.drawCirclePart(image, centerX2, centerY1, i, radiusY, Math.PI * 3 / 2, backColor);
            RectBuilder.drawCirclePart(image, centerX1, centerY2, i, radiusY, Math.PI / 2, backColor);
            RectBuilder.drawCirclePart(image, centerX2, centerY2, i, radiusY, 0, backColor);
        }
    }

    public void drawLine(WritableImage image, Color frontColor) {
        drawLine(image, (x, y) -> image.getPixelWriter().setColor(x, y, frontColor));
    }

    public void drawLine(WritableImage image, DrawOnPoint onPoint) {
        double deltaX = endX - startX;
        double a = deltaX == 0 ? Double.NaN : (endY - startY) / deltaX;
        double b = Double.isNaN(a) ? Double.NaN : endY - a * endX;
        // y = x * a + b

        drawXAxis(image, onPoint, a, b);
        drawYAxis(image, onPoint, a, b);
    }

    public void drawLine(WritableImage image, WritableImage currentImage, Color color, double opacity) {
        drawLine(image, (x, y) -> RectBuilder.drawPointTransparency(x, y, color, opacity, image, currentImage));
    }

    public void drawRect(final Color backColor, final double opacity, WritableImage image, WritableImage currentImage) {
        int startX2 = (int) startX;
        int startY2 = (int) startY;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (withinImage(startX2 + i, startY2 + j, image)) {
                    RectBuilder.drawPointTransparency(startX2 + i, startY2 + j, backColor, opacity, image,
                            currentImage);
                }
            }
        }
    }

    public void drawRect(final int color, WritableImage image, Color backColor) {
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (withinImage(startX + i, startY + j, image)) {
                    int argb = image.getPixelReader().getArgb((int) startX + i, (int) startY + j);
                    if (argb == color) {
                        image.getPixelWriter().setColor((int) startX + i, (int) startY + j, backColor);
                    }
                }
            }
        }
    }

    public void drawRect(WritableImage image, final Color backColor) {
        int startX2 = (int) startX;
        int startY2 = (int) startY;
        for (int i = 0; i <= width; i++) {
            for (int j = 0; j <= height; j++) {
                if (withinImage(startX2 + i, startY2 + j, image)) {
                    image.getPixelWriter().setColor(startX2 + i, startY2 + j, backColor);
                }
            }
        }
    }

    public void drawRect(WritableImage image, Image exception, final Color backColor) {
        int startX2 = (int) startX;
        int startY2 = (int) startY;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j <= height; j++) {
                if (withinImage(i, j, exception)) {
                    int argb = exception.getPixelReader().getArgb(i, j);
                    if (argb != 0 && withinImage(startX2 + i, startY2 + j, image)) {
                        image.getPixelWriter().setColor(startX2 + i, startY2 + j, backColor);
                    }
                }
            }
        }
    }

    public void drawStroke(WritableImage image, Color frontColor) {
        // LEFT
        RectBuilder.build().startX(startX).startY(centerY1).endX(startX).endY(centerY2).drawLine(image, frontColor);
        // RIGHT
        RectBuilder.build().startX(endX).startY(centerY1).endX(endX).endY(centerY2).drawLine(image, frontColor);
        // TOP
        RectBuilder.build().startX(centerX1).startY(startY).endX(centerX2).endY(startY).drawLine(image, frontColor);
        // BOTTOM
        RectBuilder.build().startX(centerX1).startY(endY).endX(centerX2).endY(endY).drawLine(image, frontColor);
        // TOP-LEFT
        RectBuilder.drawCirclePart(image, centerX1, centerY1, radiusX, radiusY, Math.PI, frontColor);
        // BOTTOM-LEFT
        RectBuilder.drawCirclePart(image, centerX2, centerY1, radiusX, radiusY, Math.PI * 3 / 2, frontColor);
        // BOTTOM-RIGHT
        RectBuilder.drawCirclePart(image, centerX1, centerY2, radiusX, radiusY, Math.PI / 2, frontColor);
        // TOP-RIGHT
        RectBuilder.drawCirclePart(image, centerX2, centerY2, radiusX, radiusY, 0, frontColor);
    }

    public RectBuilder endX(double value) {
        endX = Math.round(value);
        update();
        return this;
    }

    public RectBuilder endY(double value) {
        endY = Math.round(value);
        update();
        return this;
    }

    public RectBuilder height(double value) {
        height = Math.round(value);
        update();
        return this;
    }

    public WritableImage invertSelection(WritableImage image, WritableImage exception, final Color backColor) {
        WritableImage writableImage =
                new WritableImage(image.getPixelReader(), (int) image.getWidth(), (int) image.getHeight());
        int startX2 = (int) startX;
        int startY2 = (int) startY;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j <= height; j++) {
                if (withinImage(i, j, exception)) {
                    int argb = exception.getPixelReader().getArgb(i, j);
                    if (argb != 0 && withinImage(startX2 + i, startY2 + j, writableImage)) {
                        writableImage.getPixelWriter().setColor(startX2 + i, startY2 + j, backColor);
                    }
                }
            }
        }
        return writableImage;
    }

    public RectBuilder startX(double value) {
        startX = Math.round(value);
        update();
        return this;
    }

    public RectBuilder startY(double value) {
        startY = Math.round(value);
        update();
        return this;
    }

    public RectBuilder width(double value) {
        width = Math.round(value);
        update();
        return this;
    }

    private void drawXAxis(WritableImage image, DrawOnPoint onPoint, double a, double b) {
        double minX = Math.min(startX, endX);
        double maxX = Math.max(startX, endX);
        double minY = Math.min(startY, endY);
        double maxY = Math.max(startY, endY);
        for (int x = (int) minX; x < maxX; x++) {
            int y = (int) (!Double.isNaN(a) ? Math.round(a * x + b) : endY);
            if (withinImage(x, y, image) && within(x, minX - 1, maxX + 1) && within(y, minY - 1, maxY + 1)) {
                onPoint.draw(x, y);
            }
        }
    }

    private void drawYAxis(WritableImage image, DrawOnPoint onPoint, double a, double b) {
        double minX = Math.min(startX, endX);
        double maxX = Math.max(startX, endX);
        double minY = Math.min(startY, endY);
        double maxY = Math.max(startY, endY);
        for (int y = (int) minY; y < maxY; y++) {
            if (a != 0) {
                int x = (int) (Double.isNaN(a) ? startX : Math.round((y - b) / a));
                if (withinImage(x, y, image) && within(x, minX - 1, maxX + 1) && within(y, minY - 1, maxY + 1)) {
                    onPoint.draw(x, y);
                }
            }
        }
    }

    private int getColor(PixelReader pixelReader, Type type, int i, int j) {
        if (Type.BYTE_BGRA_PRE == type) {
            Color color2 = pixelReader.getColor(i + (int) startX, j + (int) startY);
           return PixelHelper.toArgb(Color.hsb(color2.getHue(), color2.getSaturation(),
                    color2.getBrightness(), color2.getOpacity()));
        }
        return pixelReader.getArgb(i + (int) startX, j + (int) startY);
    }

    private boolean isWithin(double srcWidth, double srcHeight, double destWidth, double destHeight, int i, int j) {
        return within(i + endX, destWidth) && within(j + endY, destHeight) && within(i + startX, srcWidth)
                && within(j + startY, srcHeight);
    }

    private void update() {
        radiusX = Math.round(Math.min(arc, width / 2));
        radiusY = Math.round(Math.min(arc, height / 2));
        centerY1 = Math.round(Math.min(startY + radiusY, startY + height / 2));
        centerY2 = Math.round(Math.max(endY - radiusY, startY - height / 2));
        centerX1 = Math.round(Math.min(startX + radiusX, startX + width / 2));
        centerX2 = Math.round(Math.max(endX - radiusX, endX - width / 2));
    }

    public static RectBuilder build() {
        return new RectBuilder();
    }

    public static void copyImagePart(Image srcImage, WritableImage destImage, Color transparent, Rectangle bounds) {
        int x = (int) bounds.getLayoutX();
        int y = (int) bounds.getLayoutY();
        double width = bounds.getWidth();
        double height = bounds.getHeight();
        RectBuilder.build().startX(x).startY(y).width(width).height(height).copyImagePart(srcImage, destImage,
                transparent);
    }

    public static void copyImagePart(Image srcImage, WritableImage destImage, Rectangle bounds) {
        Color transparent = Color.TRANSPARENT;
        copyImagePart(srcImage, destImage, transparent, bounds);
    }

    public static void drawPoint(WritableImage image, int x2, int y2, Color frontColor) {
        if (withinImage(x2, y2, image)) {
            image.getPixelWriter().setColor(x2, y2, frontColor);
        }
    }

    public static void drawPointTransparency(int x2, int y2, Color frontColor, double opacity, WritableImage image,
            WritableImage currentImage) {
        if (withinImage(x2, y2, image)) {
            Color color = currentImage.getPixelReader().getColor(x2, y2);
            Color color2 = color.interpolate(frontColor, opacity);
            image.getPixelWriter().setColor(x2, y2, color2);
        }
    }

    public static void drawSquareLine(WritableImage image, Color backColor, int x, int y, int w, int color) {
        for (int i = 0; i < w; i++) {
            drawPointIf(image, x + i, y, color, backColor);
            drawPointIf(image, x, y + i, color, backColor);
            drawPointIf(image, x + w, y + i, color, backColor);
            drawPointIf(image, x + i, y + w, color, backColor);
        }
    }

    public static void drawSquareLine(WritableImage image, int startX, int startY, int w, Color color) {
        for (int x = 0; x <= w; x++) {
            drawPoint(image, startX + x, startY, color);
            drawPoint(image, startX, startY + x, color);
            drawPoint(image, startX + x, startY + w, color);
            drawPoint(image, startX + w, startY + x, color);
        }
    }

    public static void drawSquareLine(WritableImage image, WritableImage currentImage, int startX, int startY, int w,
            Color color, double opacity) {
        for (int x = 0; x < w; x++) {
            drawPointTransparency(startX + x, startY, color, opacity, image, currentImage);
            drawPointTransparency(startX, startY + x, color, opacity, image, currentImage);
            drawPointTransparency(startX + x, startY + w, color, opacity, image, currentImage);
            drawPointTransparency(startX + w, startY + x, color, opacity, image, currentImage);
        }
    }

    public static boolean isEqualImage(WritableImage image, WritableImage image2) {
        if (image.getWidth() != image2.getWidth() || image.getHeight() != image2.getHeight()) {
            return false;
        }
        for (int i = 0; i < image.getWidth(); i++) {
            for (int j = 0; j < image.getHeight(); j++) {
                if (image.getPixelReader().getArgb(i, j) != image2.getPixelReader().getArgb(i, j)) {
                    return false;
                }
            }
        }
        return true;
    }

    public static WritableImage printNodeToImage(Node line2, WritableImage image) {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        Bounds local = line2.getBoundsInLocal();
        params.setTransform(Transform.scale(width / local.getWidth(), height / local.getHeight()));
        return line2.snapshot(params, new WritableImage(width, height));
    }

    public static WritableImage resizeImage(final WritableImage image, double newWidth, double newHeight) {

        int newWidth2 = Math.max((int) newWidth, 1);
        int newHeight2 = Math.max((int) newHeight, 1);
        WritableImage newImage = new WritableImage(newWidth2, newHeight2);
        double width = image.getWidth();
        double height = image.getHeight();
        double yRatio = newHeight / height;
        double xRatio = newWidth / width;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                Color color = image.getPixelReader().getColor(i, j);
                int x = (int) Math.round(i * xRatio);
                int y = (int) Math.round(j * yRatio);
                if (withinImage(x, y, newImage)) {
                    newImage.getPixelWriter().setColor(x, y, color);
                }
                RectBuilder.setPixels(newImage, color, x, y, xRatio, yRatio);
            }
        }
        return newImage;
    }

    public static void takeSnapshot(Node line2, WritableImage image, Group imageStack, ImageView imageView,
            Node rectangleBorder, WritableImage currentImage) {
        Bounds bounds = line2.getBoundsInParent();
        int width = (int) bounds.getWidth() + 2;
        int height = (int) bounds.getHeight() + 2;
        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        WritableImage textImage = line2.snapshot(params, new WritableImage(width, height));
        int x = (int) bounds.getMinX();
        int y = (int) bounds.getMinY();
        build().startX(0).startY(0).width(width).height(height).endX(x).endY(y).copyImagePartTransparency(textImage,
                image, currentImage);
        imageStack.getChildren().clear();
        imageStack.getChildren().add(rectangleBorder);
        imageStack.getChildren().add(imageView);
    }

    public static void takeSnapshotFill(Node line2, WritableImage image, Group imageStack, ImageView imageView,
            Node rectangleBorder) {
        Bounds bounds = line2.getBoundsInParent();
        int width = (int) bounds.getWidth() + 2;
        int height = (int) bounds.getHeight() + 2;
        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        WritableImage textImage = line2.snapshot(params, new WritableImage(width, height));
        int x = (int) bounds.getMinX();
        int y = (int) bounds.getMinY();
        build().startX(0).startY(0).width(width).height(height).endX(x).endY(y).copyImagePart(textImage, image,
                Color.TRANSPARENT);
        imageStack.getChildren().clear();
        imageStack.getChildren().add(rectangleBorder);
        imageStack.getChildren().add(imageView);
    }

    private static void drawCirclePart(WritableImage image, double centerX, double centerY, double radiusX,
            double radiusY, double startAngle, Color frontColor) {
        double nPoints2 = Math.max(radiusX, radiusY) * RectBuilder.N_POINTS_MULTIPLIER;
        double angle = Math.PI / 2;
        for (double t = 0; t < angle; t += 2 * Math.PI / nPoints2) {
            int x = (int) Math.round(radiusX * Math.cos(t + startAngle));
            int y = (int) Math.round(radiusY * Math.sin(t + startAngle));
            RectBuilder.drawPoint(image, x + (int) centerX, y + (int) centerY, frontColor);
        }
    }

    private static void drawPointIf(WritableImage image, int x2, int y2, int color, Color backColor) {
        if (withinImage(x2, y2, image)) {
            int argb = image.getPixelReader().getArgb(x2, y2);
            if (argb == color) {
                image.getPixelWriter().setColor(x2, y2, backColor);
            }
        }
    }

    private static void setPixels(WritableImage newImage, Color color, int x, int y, double xRatio, double yRatio) {
        for (int l = 0; l < xRatio; l++) {
            for (int k = 0; k < yRatio; k++) {
                if (withinImage(x + l, y + k, newImage)) {
                    newImage.getPixelWriter().setColor(x + l, y + k, color);
                }
            }
        }
    }
}