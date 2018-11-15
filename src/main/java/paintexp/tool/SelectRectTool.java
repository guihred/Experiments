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
import javafx.scene.input.Clipboard;
import javafx.scene.input.DataFormat;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.slf4j.Logger;
import paintexp.PaintModel;
import simplebuilder.SimpleRectangleBuilder;
import utils.HasLogging;

public class SelectRectTool extends PaintTool {

	private static final Logger LOG = HasLogging.log();
	private Rectangle icon;
	private Rectangle area;
	private double initialX;
	private double initialY;

	public Rectangle getArea() {
		if (area == null) {
			area = new SimpleRectangleBuilder().fill(Color.TRANSPARENT).stroke(Color.BLACK)
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
		if (MouseEvent.MOUSE_RELEASED.equals(eventType)) {
			onMouseReleased(model);
		}
		if (MouseEvent.MOUSE_PRESSED.equals(eventType)) {
			onMousePressed(e, model);
		}
		if (MouseEvent.MOUSE_DRAGGED.equals(eventType)) {
			onMouseDragged(e, model);
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
			default:
				break;
		}
	}

	public void selectArea(final int x, final int y, final double w, final double h, final PaintModel model) {
		initialX = x;
		initialY = y;
		addRect(model);
		dragTo(w, h, model);
		onMouseReleased(model);
	}

	private void addRect(final PaintModel model) {
		ObservableList<Node> children = model.getImageStack().getChildren();
		if (!children.contains(getArea())) {
			children.add(getArea());
		}
		area.setStroke(Color.BLACK);
		getArea().setManaged(false);
		getArea().setLayoutX(initialX);
		getArea().setLayoutY(initialY);
		getArea().setWidth(1);
		getArea().setHeight(1);
	}

	private void copyFromClipboard(final PaintModel model) {
		Clipboard systemClipboard = Clipboard.getSystemClipboard();
		Image image = systemClipboard.getImage();
		if (image != null) {
			copyImage(model, image, model.getImage());
		} else if (systemClipboard.getFiles() != null) {
			copyFromFile(model, systemClipboard.getFiles());
		}
	}

	private void copyFromFile(final PaintModel model, final List<File> files) {
		if (!files.isEmpty()) {
			File file = files.get(0);
			try {
				Image image2 = new Image(file.toURI().toURL().toExternalForm());
				copyImage(model, image2, model.getImage());
			} catch (Exception e1) {
				LOG.error("", e1);
			}
		}
	}

	private void copyImage(final PaintModel model, final Image srcImage, final WritableImage destImage) {
		double width = srcImage.getWidth();
		double height = srcImage.getHeight();
		copyImagePart(srcImage, destImage, 0, 0, width, height);
		selectArea(0, 0, srcImage.getWidth(), srcImage.getHeight(), model);
	}

	private void copyImagePart(final Image srcImage, final WritableImage destImage, final int x, final int y,
			final double width,
			final double height) {
		PixelReader pixelReader = srcImage.getPixelReader();
		PixelWriter pixelWriter = destImage.getPixelWriter();
		Type type = pixelReader.getPixelFormat().getType();
		double destWidth = destImage.getWidth();
		double destHeight = destImage.getHeight();
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				if (within(i, destWidth) && within(j, destHeight)) {
					Color color = pixelReader.getColor(i + x, j + y);
					if (Type.BYTE_BGRA_PRE == type) {
						color = Color.hsb(color.getHue(), color.getSaturation(), color.getBrightness());
					}
					pixelWriter.setColor(i, j, color);
				}
			}
		}
	}

	private void copyToClipboard(final PaintModel model) {
		Clipboard systemClipboard = Clipboard.getSystemClipboard();
		double width = area.getWidth();
		double height = area.getHeight();
		WritableImage writableImage = new WritableImage((int) width, (int) height);
		int layoutX = (int) area.getLayoutX();
		int layoutY = (int) area.getLayoutY();
		int maxWidth = (int) model.getImage().getWidth();
		int maxHeight = (int) model.getImage().getHeight();
		copyImagePart(model.getImage(), writableImage, Integer.min(Integer.max(layoutX, 0), maxWidth),
				Integer.min(Integer.max(layoutY, 0), maxHeight), width, height);
		HashMap<DataFormat, Object> content = new HashMap<>();
		content.put(DataFormat.IMAGE, writableImage);
		systemClipboard.setContent(content);
	
	}

	private void dragTo(final double x, final double y, final PaintModel model) {
		double layoutX = initialX;
		double layoutY = initialY;
		double min = Double.max(Double.min(x, layoutX), 0);
		getArea().setLayoutX(min);
		double min2 = Double.max(Double.min(y, layoutY), 0);
		getArea().setLayoutY(min2);
		double width = model.getImage().getWidth();
		getArea().setWidth(Double.min(Math.abs(x - layoutX), Math.abs(width - layoutX)));
		double height = model.getImage().getHeight();
		getArea().setHeight(Double.min(Math.abs(y - layoutY), Math.abs(height - layoutY)));
	}

	private void onMouseDragged(final MouseEvent e, final PaintModel model) {
		double x = e.getX();
		double y = e.getY();
		dragTo(Double.max(x, 0), Double.max(y, 0), model);
	}

	private void onMousePressed(final MouseEvent e, final PaintModel model) {
		initialX = e.getX();
		initialY = e.getY();
		addRect(model);
	}

	private void onMouseReleased(final PaintModel model) {
		ObservableList<Node> children = model.getImageStack().getChildren();
		if (getArea().getWidth() < 2 && children.contains(getArea())) {
			children.remove(getArea());
		}
		area.setStroke(Color.BLUE);
	}

}