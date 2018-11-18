package paintexp.tool;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import javafx.collections.ObservableList;
import javafx.event.EventType;
import javafx.geometry.Bounds;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.PixelFormat.Type;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import org.slf4j.Logger;
import paintexp.PaintModel;
import simplebuilder.SimpleRectangleBuilder;
import utils.HasLogging;

public class SelectRectTool extends PaintTool {

	private static final Logger LOG = HasLogging.log();
	private Rectangle icon;
	private Rectangle area;
	private WritableImage imageSelected;
	private double initialX;
	private double initialY;

    public boolean containsPoint(final double localX, final double localY) {
        return getArea().getLayoutX() < localX && localX < getArea().getLayoutX() + getArea().getWidth()
                && getArea().getLayoutY() < localY && localY < getArea().getLayoutY() + getArea().getHeight();
    }

	public Rectangle getArea() {
		if (area == null) {
			area = new SimpleRectangleBuilder().fill(Color.TRANSPARENT).stroke(Color.BLACK)
					.cursor(Cursor.MOVE)
					.strokeDashArray(1, 2, 1, 2).build();
		}
		return area;
	}

	@Override
	public Node getIcon() {
		if (icon == null) {
			icon = new SimpleRectangleBuilder().width(10).height(10).fill(Color.TRANSPARENT).stroke(Color.BLACK)
					.strokeDashArray(1, 2, 1, 2).build();
		}
		return icon;
	}

	@Override
	public Cursor getMouseCursor() {
		return Cursor.CROSSHAIR;
	}

	@Override
    public void handleEvent(final MouseEvent e, final PaintModel model) {
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

	@Override
	public void handleKeyEvent(final KeyEvent e, final PaintModel model) {
		KeyCode code = e.getCode();
		switch (code) {
			case DELETE:
				Bounds bounds = getArea().getBoundsInParent();
				drawRect(model, bounds.getMinX(), bounds.getMinY(), bounds.getWidth(), bounds.getHeight());
				break;
			case V:
				if (e.isControlDown()) {
					copyFromClipboard(model);
				}
				break;
			case C:
				if (e.isControlDown()) {
					copyToClipboard(model);
				}
				break;
			case A:
				if (e.isControlDown()) {
					selectArea(0, 0, model.getImage().getWidth(), model.getImage().getHeight(), model);
				}
				break;
			case ESCAPE:
				if (imageSelected != null) {
					setIntoImage(model);
				}
				break;
			default:
				break;
		}
	}

	public void selectArea(final int x, final int y, final double w, final double h, final PaintModel model) {
		initialX = x;
		initialY = y;
		addRect(model);
		dragTo(w, h);
		onMouseReleased(model);
	}


	private void addRect(final PaintModel model) {
		ObservableList<Node> children = model.getImageStack().getChildren();
		if (!children.contains(getArea())) {
			children.add(getArea());
		}
		area.setStroke(Color.BLACK);
		getArea().setManaged(false);
		getArea().setFill(Color.TRANSPARENT);
		getArea().setLayoutX(initialX);
		getArea().setLayoutY(initialY);
		getArea().setWidth(1);
		getArea().setHeight(1);
	}

	private void copyFromClipboard(final PaintModel model) {
		Clipboard systemClipboard = Clipboard.getSystemClipboard();
		Image image = systemClipboard.getImage();
		if (image != null) {
			copyImage(model, image, imageSelected);
		} else if (systemClipboard.getFiles() != null) {
			copyFromFile(model, systemClipboard.getFiles());
		}
	}

	private void copyFromFile(final PaintModel model, final List<File> files) {
		if (!files.isEmpty()) {
			File file = files.get(0);
			try {
				Image image2 = new Image(file.toURI().toURL().toExternalForm());
				copyImage(model, image2, imageSelected);
			} catch (Exception e1) {
				LOG.error("", e1);
			}
		}
	}

	private void copyImage(final PaintModel model, final Image srcImage, final WritableImage destImage) {
		double width = srcImage.getWidth();
		double height = srcImage.getHeight();
		if (destImage == null) {
			imageSelected = new WritableImage((int) width, (int) height);
		}
		copyImagePart(srcImage, destImage != null ? destImage : imageSelected, 0, 0, width, height);
		selectArea(0, 0, srcImage.getWidth(), srcImage.getHeight(), model);
	}

	private void copyImagePart(final Image srcImage, final WritableImage destImage, final int x, final int y,
			final double width,
			final double height) {
		copyImagePart(srcImage, destImage, x, y, width, height, 0, 0);
	}


	private void copyImagePart(final Image srcImage, final WritableImage destImage, final int x, final int y,
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

	private void copyToClipboard(final PaintModel model) {
		Clipboard systemClipboard = Clipboard.getSystemClipboard();
		double width = area.getWidth();
		double height = area.getHeight();
		imageSelected = new WritableImage((int) width, (int) height);
		int layoutX = (int) area.getLayoutX();
		int layoutY = (int) area.getLayoutY();
		int maxWidth = (int) model.getImage().getWidth();
		int maxHeight = (int) model.getImage().getHeight();
		copyImagePart(model.getImage(), imageSelected, Integer.min(Integer.max(layoutX, 0), maxWidth),
				Integer.min(Integer.max(layoutY, 0), maxHeight), width, height);
		HashMap<DataFormat, Object> content = new HashMap<>();
		content.put(DataFormat.IMAGE, imageSelected);
		systemClipboard.setContent(content);
	
	}

	private void dragTo(final double x, final double y) {

		getArea().setLayoutX(Double.min(x, initialX));
		getArea().setLayoutY(Double.min(y, initialY));
		getArea().setWidth(Math.abs(x - initialX));
		getArea().setHeight(Math.abs(y - initialY));
	}

    private void onMouseDragged(final MouseEvent e, final PaintModel model) {
		double x = e.getX();
		double y = e.getY();
		double width = model.getImage().getWidth();
		double height = model.getImage().getHeight();
		ObservableList<Node> children = model.getImageStack().getChildren();
		if (children.contains(getArea()) && imageSelected != null) {
            getArea().setLayoutX(Double.max(x - initialX, -width / 4));
            getArea().setLayoutY(Double.max(y - initialY, -height / 4));
			return;
		}
        dragTo(setWithinRange(x, 0, width), setWithinRange(y, 0, height));
	}

	private void onMousePressed(final MouseEvent e, final PaintModel model) {
		ObservableList<Node> children = model.getImageStack().getChildren();

        if (children.contains(getArea())) {
            if (containsPoint(e.getX(), e.getY())) {
                if (imageSelected == null) {
                    int width = (int) area.getWidth();
                    int height = (int) area.getHeight();
                    imageSelected = new WritableImage(width, height);
                    int layoutX = (int) area.getLayoutX();
                    int layoutY = (int) area.getLayoutY();
                    copyImagePart(model.getImage(), imageSelected, layoutX, layoutY, width, height);
                    getArea().setFill(new ImagePattern(imageSelected));
                    drawRect(model, layoutX, layoutY, width, height);
                }
                return;
            }
            if (imageSelected != null) {
                setIntoImage(model);
            }
        }
        initialX = e.getX();
        initialY = e.getY();
        addRect(model);

	}

	private void onMouseReleased(final PaintModel model) {
		ObservableList<Node> children = model.getImageStack().getChildren();
		if (getArea().getWidth() < 2 && children.contains(getArea()) && imageSelected != null) {

			children.remove(getArea());
		}
		area.setStroke(Color.BLUE);
	}

    private void setIntoImage(final PaintModel model) {
		int x = (int) getArea().getLayoutX();
		int y = (int) getArea().getLayoutY();
		double width = getArea().getWidth();
		double height = getArea().getHeight();
		copyImagePart(imageSelected, model.getImage(), 0, 0, width, height, x, y);
		imageSelected = null;
		model.getImageStack().getChildren().remove(getArea());
	}

}