package paintexp.tool;
import static paintexp.tool.DrawOnPoint.within;
import static paintexp.tool.DrawOnPoint.withinRange;

import javafx.beans.property.Property;
import javafx.event.EventType;
import javafx.geometry.Bounds;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelFormat.Type;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import paintexp.PaintModel;
import utils.ResourceFXUtils;

public abstract class PaintTool extends Group {
    private static final int N_POINTS_MULTIPLIER = 16;

    public PaintTool() {
		setId(getClass().getSimpleName());
        getChildren().add(getIcon());
    }

	public void drawRect(final PaintModel model, final double x, final double y, final double w, final double h) {
        drawRect(model, x, y, w, h, model.getBackColor());
    }

    public abstract Node getIcon();

	public Cursor getMouseCursor() {
		return Cursor.DEFAULT;
	}
    public void handleEvent(final MouseEvent e, final PaintModel model) {
        simpleHandleEvent(e, model);
        EventType<? extends MouseEvent> eventType = e.getEventType();
        if (MouseEvent.MOUSE_RELEASED.equals(eventType)) {
            model.createImageVersion();
        }

    }

    @SuppressWarnings("unused")
    public void handleKeyEvent(final KeyEvent e, final PaintModel paintModel) {
        // DOES NOTHING
    }

    @SuppressWarnings("unused")
	public void onDeselected(final PaintModel model) {
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

    protected void copyImagePart(final Image srcImage, final WritableImage destImage, final Bounds bounds) {
        int x = (int) bounds.getMinX();
        int y = (int) bounds.getMinY();
        double width = bounds.getWidth();
        double height = bounds.getHeight();
        copyImagePart(srcImage, destImage, x, y, width, height, 0, 0, Color.TRANSPARENT);
    }

    protected void copyImagePart(final Image srcImage, final WritableImage destImage, final int x, final int y,
            final double width, final double height, final int xOffset, final int yOffset, final Color ignoreColor) {
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
                if (within(i + xOffset, destWidth) && within(j + yOffset, destHeight) && within(i + x, srcWidth)
                        && within(j + y, srcHeight)) {
                    int color = pixelReader.getArgb(i + x, j + y);
                    if (Type.BYTE_BGRA_PRE == type) {
                        Color color2 = pixelReader.getColor(i + x, j + y);
                        color = PixelHelper.toArgb(Color.hsb(color2.getHue(), color2.getSaturation(), color2.getBrightness(),
                        color2.getOpacity()));
                    }
                    if (color != argb) {
                        pixelWriter.setArgb(i + xOffset, j + yOffset, isTransparent ? color | 0xFF000000 : color);
                    }
                }
            }
        }
    }



    protected void drawCircle(final PaintModel model, final double centerX, final double centerY, final double radiusX,
            final double radiusY, final double startAngle) {
        Color frontColor = model.getFrontColor();
        drawCirclePart(model, centerX, centerY, radiusX, radiusY, startAngle, frontColor);
    }

    protected void drawCircle(final PaintModel model, final int centerX, final int centerY, final double radiusX,
            final double radiusY, final Color color) {
        double nPoints = Math.max(radiusX, radiusY) * N_POINTS_MULTIPLIER;
        for (double t = 0; t < 2 * Math.PI; t += 2 * Math.PI / nPoints) {
            int x = (int) Math.round(radiusX * Math.cos(t));
            int y = (int) Math.round(radiusY * Math.sin(t));
            drawPoint(model, x + centerX, y + centerY, color);
        }
    }

    protected void drawCirclePart(final PaintModel model, final double centerX, final double centerY, final double radiusX,
            final double radiusY, final double startAngle, final Color frontColor) {
        double nPoints2 = Math.max(radiusX, radiusY) * N_POINTS_MULTIPLIER;
        double angle = Math.PI / 2;
        for (double t = 0; t < angle; t += 2 * Math.PI / nPoints2) {
            int x = (int) Math.round(radiusX * Math.cos(t + startAngle));
            int y = (int) Math.round(radiusY * Math.sin(t + startAngle));
            drawPoint(model, x + (int) centerX, y + (int) centerY, frontColor);
        }
    }

    protected void drawLine(final PaintModel model, final double startX, final double startY, final double endX,
            final double endY) {
        drawLine(model, startX, startY, endX, endY,
                (x, y) -> model.getImage().getPixelWriter().setColor(x, y, model.getFrontColor()));
    }

    protected void drawLine(final PaintModel model, final double startX, final double startY, final double endX,
			final double endY, final Color color) {
		drawLine(model, startX, startY, endX, endY, (x, y) -> model.getImage().getPixelWriter().setColor(x, y, color));
	}

	protected void drawLine(final PaintModel model, final double startX, final double startY, final double endX,
            final double endY, final DrawOnPoint onPoint) {
        double deltaX = endX - startX;
        double a = deltaX == 0 ? Double.NaN : (endY - startY) / deltaX;
        double b = Double.isNaN(a) ? Double.NaN : endY - a * endX;
        // y = x * a + b

        double minX = Math.min(startX, endX);
        double maxX = Math.max(startX, endX);
        double minY = Math.min(startY, endY);
        double maxY = Math.max(startY, endY);
        for (int x = (int) minX; x < maxX; x++) {
            int y = (int) (!Double.isNaN(a) ? Math.round(a * x + b) : endY);
            if (withinRange(x, y, model) && within(x, minX - 1, maxX + 1) && within(y, minY - 1, maxY + 1)) {
                onPoint.draw(x, y);
            }
        }
        for (int y = (int) minY; y < maxY; y++) {
            if (a != 0) {
                int x = (int) (Double.isNaN(a) ? startX : Math.round((y - b) / a));
                if (withinRange(x, y, model) && within(x, minX - 1, maxX + 1) && within(y, minY - 1, maxY + 1)) {
                    onPoint.draw(x, y);
                }
            }
        }
    }

    protected void drawPoint(final PaintModel model, final int x2, final int y2) {
        Color frontColor = model.getFrontColor();
        drawPoint(model, x2, y2, frontColor);
    }

    protected void drawPoint(final PaintModel model, final int x2, final int y2, final Color frontColor) {
        if (withinRange(x2, y2, model)) {
            model.getImage().getPixelWriter().setColor(x2, y2, frontColor);
        }
    }

    protected void drawPointIf(final PaintModel model, final int x2, final int y2, final int color, final Color backColor) {
        if (withinRange(x2, y2 , model)) {
            int argb = model.getImage().getPixelReader().getArgb(x2, y2 );
            if (argb == color) {
                model.getImage().getPixelWriter().setColor(x2, y2, backColor);
            }
        }
    }

    protected void drawRect(final PaintModel model, final double x, final double y, final double w, final double h,
            final Color backColor) {
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                if (withinRange((int) x + i, (int) y + j, model)) {
                    model.getImage().getPixelWriter().setColor((int) x + i, (int) y + j, backColor);
                }
            }
        }
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

    protected void drawSquareLine(final PaintModel model, final int startX, final int startY, final int w, final Color color) {
        for (int x = 0; x < w; x++) {
            drawPoint(model, startX+x, startY,color);
            drawPoint(model, startX, startY+x,color);
            drawPoint(model, startX+x, startY+w,color);
            drawPoint(model, startX+w, startY+x,color);
        }
    }

	protected void drawSquareLine(final PaintModel model, final int x, final int y, final int w, final int color) {
        Color backColor = model.getBackColor();
        for (int i = 0; i < w; i++) {
            drawPointIf(model, x + i, y, color, backColor);
            drawPointIf(model, x, y + i, color, backColor);
            drawPointIf(model, x + w, y + i, color, backColor);
            drawPointIf(model, x + i, y + w, color, backColor);
        }
    }

    protected ImageView getIconByURL(final String src) {
        ImageView icon = new ImageView(ResourceFXUtils.toExternalForm(src));
        icon.setPreserveRatio(true);
        icon.setFitWidth(10);
        icon.maxWidth(10);
        icon.maxHeight(10);
        return icon;

    }

    protected ImageView getIconByURL(final String src, final double width) {
		ImageView icon = new ImageView(ResourceFXUtils.toExternalForm(src));
		icon.setPreserveRatio(true);
		icon.setFitWidth(width);
		icon.maxWidth(width);
		icon.maxHeight(width);
		return icon;

	}

	protected void handleSlider(final KeyEvent e, final Property<Number> property, final Slider slider) {
			
			if (e.getCode() == KeyCode.ADD || e.getCode() == KeyCode.PLUS) {
			property.setValue(
					Math.min(slider.getMax(), slider.getBlockIncrement() + property.getValue().doubleValue()));
			}
			if (e.getCode() == KeyCode.SUBTRACT || e.getCode() == KeyCode.MINUS) {
			property.setValue(
					Math.max(slider.getMin(), property.getValue().doubleValue() - slider.getBlockIncrement()));
			}
		}

	@SuppressWarnings("unused")
    protected void onMouseDragged(final MouseEvent e, final PaintModel model) {
		// DOES NOTHING
        
    }


    @SuppressWarnings("unused")
    protected void onMousePressed(final MouseEvent e, final PaintModel model) {
		// DOES NOTHING
    }

    @SuppressWarnings("unused")
    protected void onMouseReleased(final PaintModel model) {
        
		// DOES NOTHING
    }



    protected void simpleHandleEvent(final MouseEvent e, final PaintModel model) {
        EventType<? extends MouseEvent> eventType = e.getEventType();
        if (MouseEvent.MOUSE_PRESSED.equals(eventType)) {
            onMousePressed(e, model);
        }
        if (MouseEvent.MOUSE_DRAGGED.equals(eventType)) {
            onMouseDragged(e, model);
        }
        if (MouseEvent.MOUSE_RELEASED.equals(eventType)) {
            onMouseReleased(model);
        }
    }

    protected void takeSnapshotFill(final PaintModel model, final Node line2) {
        Bounds bounds = line2.getBoundsInParent();
        int width = (int) bounds.getWidth() + 2;
        int height = (int) bounds.getHeight() + 2;
        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        WritableImage textImage = line2.snapshot(params, new WritableImage(width, height));
        int x = (int) bounds.getMinX();
        int y = (int) bounds.getMinY();
		copyImagePart(textImage, model.getImage(), 0, 0, width, height, x, y, Color.TRANSPARENT);
        model.getImageStack().getChildren().clear();
        ImageView imageView = new ImageView(model.getImage());
        model.getImageStack().getChildren().add(model.getRectangleBorder(imageView));
        model.getImageStack().getChildren().add(imageView);
    }


}