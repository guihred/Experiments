package paintexp.tool;

import static utils.DrawOnPoint.withinImage;

import javafx.beans.property.Property;
import javafx.collections.ObservableList;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import utils.ResourceFXUtils;

public final class PaintToolHelper {
    public static final int N_POINTS_MULTIPLIER = 16;

    private PaintToolHelper() {
    }

    public static void drawAndFillSquare(final PaintModel model, final int x2, final int y2, double r, Color color,
        double op) {
        RectBuilder.build().startX(x2).startY(y2).width(r).height(r).drawRect(color, op, model.getImage(),
            model.getImageVersions());
    }

    public static void drawCircle(WritableImage image, int centerX, int centerY, double radiusX, double radiusY,
        Color color) {
        double nPoints = Math.max(radiusX, radiusY) * N_POINTS_MULTIPLIER;
        for (double t = 0; t < 2 * Math.PI; t += 2 * Math.PI / nPoints) {
            int x = (int) Math.round(radiusX * Math.cos(t));
            int y = (int) Math.round(radiusY * Math.sin(t));
            drawPoint(image, x + centerX, y + centerY, color);
        }
    }

    public static void drawCirclePart(WritableImage image, double centerX, double centerY, double radiusX,
        double radiusY, double startAngle, Color frontColor) {
        double nPoints2 = Math.max(radiusX, radiusY) * N_POINTS_MULTIPLIER;
        double angle = Math.PI / 2;
        for (double t = 0; t < angle; t += 2 * Math.PI / nPoints2) {
            int x = (int) Math.round(radiusX * Math.cos(t + startAngle));
            int y = (int) Math.round(radiusY * Math.sin(t + startAngle));
            drawPoint(image, x + (int) centerX, y + (int) centerY, frontColor);
        }
    }

    public static void drawPoint(WritableImage image, int x2, int y2, Color frontColor) {
        if (withinImage(x2, y2, image)) {
            image.getPixelWriter().setColor(x2, y2, frontColor);
        }
    }

    public static void drawPointIf(WritableImage image, int x2, int y2, int color, Color backColor) {
        if (withinImage(x2, y2, image)) {
            int argb = image.getPixelReader().getArgb(x2, y2);
            if (argb == color) {
                image.getPixelWriter().setColor(x2, y2, backColor);
            }
        }
    }

    public static void drawPointTransparency(int x2, int y2, Color frontColor, double opacity, WritableImage image,
        ObservableList<WritableImage> imageVersions) {
        if (withinImage(x2, y2, image)) {
            int index = Math.max(imageVersions.size() - 1, 0);
            Color color = imageVersions.get(index).getPixelReader().getColor(x2, y2);
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
        for (int x = 0; x < w; x++) {
            drawPoint(image, startX + x, startY, color);
            drawPoint(image, startX, startY + x, color);
            drawPoint(image, startX + x, startY + w, color);
            drawPoint(image, startX + w, startY + x, color);
        }
    }

    public static void drawSquareLine(WritableImage image, ObservableList<WritableImage> imageVersions, int startX,
        int startY, int w, Color color, double opacity) {
        for (int x = 0; x < w; x++) {
            drawPointTransparency(startX + x, startY, color, opacity, image, imageVersions);
            drawPointTransparency(startX, startY + x, color, opacity, image, imageVersions);
            drawPointTransparency(startX + x, startY + w, color, opacity, image, imageVersions);
            drawPointTransparency(startX + w, startY + x, color, opacity, image, imageVersions);
        }
    }

    public static ImageView getIconByURL(String src) {
        return getIconByURL(src, 30);

    }

    public static ImageView getIconByURL(String src, double width) {
        ImageView icon1 = new ImageView(ResourceFXUtils.toExternalForm("paint/" + src));
        icon1.setPreserveRatio(true);
        icon1.setFitWidth(width);
        icon1.setFitHeight(width);
        icon1.maxWidth(width);
        icon1.maxHeight(width);
        return icon1;

    }

    public static void handleSlider(KeyEvent e, Property<Number> property, Slider slider) {
        if (e.getCode() == KeyCode.ADD || e.getCode() == KeyCode.PLUS) {
            property
                .setValue(Math.min(slider.getMax(), slider.getBlockIncrement() + property.getValue().doubleValue()));
        }
        if (e.getCode() == KeyCode.SUBTRACT || e.getCode() == KeyCode.MINUS) {
            property
                .setValue(Math.max(slider.getMin(), property.getValue().doubleValue() - slider.getBlockIncrement()));
        }
    }

    public static boolean isEqualImage(WritableImage image, WritableImage image2) {
        for (int i = 0; i < image.getWidth(); i++) {
            for (int j = 0; j < image.getHeight(); j++) {
                if (image.getPixelReader().getArgb(i, j) != image2.getPixelReader().getArgb(i, j)) {
                    return false;
                }
            }
        }
        return true;
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
