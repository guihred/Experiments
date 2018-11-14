package paintexp;

import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.event.EventType;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import utils.ResourceFXUtils;

public class BucketTool extends PaintTool {

	private ImageView icon;
	boolean pressed;

	private Rectangle area;
	private int initialX;
	private int initialY;
	private int width;
	private int height;

	public Rectangle getArea() {
		if (area == null) {
			area = new Rectangle(10, 10, Color.WHITE);
		}
		return area;
	}

	@Override
	public Node getIcon() {
		if (icon == null) {
			icon = new ImageView(ResourceFXUtils.toExternalForm("Bucket.png"));
			icon.setPreserveRatio(true);
			icon.setFitWidth(10);
			icon.maxWidth(10);
			icon.maxHeight(10);
		}
		return icon;
	}

	@Override
	public Cursor getMouseCursor() {
		return Cursor.DISAPPEAR;
	}

	@Override
	public synchronized void handleEvent(MouseEvent e, PaintModel model) {
		EventType<? extends MouseEvent> eventType = e.getEventType();
		if (MouseEvent.MOUSE_CLICKED.equals(eventType)) {
			initialX = (int) e.getX();
			initialY = (int) e.getY();
			width = (int) model.getImage().getWidth();
			height = (int) model.getImage().getHeight();
			PixelReader pixelReader = model.getImage().getPixelReader();
			int originalColor = pixelReader.getArgb(initialX, initialY);
			int frontColor = SimplePixelReader.toArgb(model.getFrontColor());
			if (originalColor != frontColor) {
				Platform.runLater(() -> setColor(initialX, initialY, originalColor, frontColor, pixelReader, model));
			}

		}
	}

	public void setColor(int initX, int initY, int originalColor, int frontColor, PixelReader pixelReader,
			PaintModel model) {
		List<Integer> toGo = new ArrayList<>();
		toGo.add(index(initX, initY));
		while (!toGo.isEmpty()) {
			Integer next = toGo.remove(0);
			int y = y(next);
			int x = x(next);
			if (y == 0 && next != 0) {
				if (withinRange(x, y, model)) {
					int color = pixelReader.getArgb(x, y);
					if (color == originalColor) {
						addIfNotIn(toGo, next + 1);
						addIfNotIn(toGo, next - 1);
						addIfNotIn(toGo, next + width);
						addIfNotIn(toGo, next - width);
						model.getImage().getPixelWriter().setArgb(x, y, frontColor);
					}
				}
			}
		}
	}

	private Integer index(int initialX2, int initialY2) {
		return initialX2 * width + initialY2;
	}

	private int x(int m) {
		return m / width;
	}

	private int y(int m) {

		return m % width;
	}

	private void addIfNotIn(List<Integer> toGo, Integer e) {
		if (!toGo.contains(e) && e < (width - 1) * (height - 1)) {
			toGo.add(e);
		}
	}


}