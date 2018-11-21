package paintexp.tool;

import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.event.EventType;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import paintexp.PaintModel;
import paintexp.SimplePixelReader;
import utils.ResourceFXUtils;

public class BucketTool extends PaintTool {

	private ImageView icon;
	boolean pressed;

	private Rectangle area;
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
	public void handleEvent(final MouseEvent e, final PaintModel model) {
		EventType<? extends MouseEvent> eventType = e.getEventType();
        if (MouseEvent.MOUSE_CLICKED.equals(eventType)) {

			onMouseClicked(e, model);
		}
	}

	public void setColor(final int initX, final int initY, final int originalColor, final int frontColor, final PixelReader pixelReader,
			final PaintModel model) {
		List<Integer> toGo = new ArrayList<>();
		toGo.add(index(initX, initY));
		while (!toGo.isEmpty()) {
			Integer next = toGo.remove(0);
			int x = x(next);
			int y = y(next);
			if (withinRange(x, y, model)) {
				int color = pixelReader.getArgb(x, y);
				if (color == originalColor) {
					if (y != 0 && y != height - 1) {
						addIfNotIn(toGo, next + 1);
						addIfNotIn(toGo, next - 1);
						addIfNotIn(toGo, next + width);
						addIfNotIn(toGo, next - width);
					}
					model.getImage().getPixelWriter().setArgb(x, y, frontColor);
				}
			}

		}
	}

	private void addIfNotIn(final List<Integer> toGo, final Integer e) {
		if (!toGo.contains(e) && e < width * height && e >= 0) {
			toGo.add(e);
		}
	}

	private Integer index(final int initialX2, final int initialY2) {
		return initialX2 * width + initialY2;
	}

	private void onMouseClicked(final MouseEvent e, final PaintModel model) {
		int initialX = (int) e.getX();
		int initialY = (int) e.getY();
		width = (int) model.getImage().getWidth();
		height = (int) model.getImage().getHeight();
		PixelReader pixelReader = model.getImage().getPixelReader();
		int originalColor = pixelReader.getArgb(initialX, initialY);
		int frontColor = SimplePixelReader
				.toArgb(e.getButton() == MouseButton.PRIMARY ? model.getFrontColor() : model.getBackColor());
		if (originalColor != frontColor) {
			Platform.runLater(() -> setColor(initialX, initialY, originalColor, frontColor, pixelReader, model));
		}
	}

	private int x(final int m) {
		return m / width;
	}

	private int y(final int m) {

		return m % width;
	}


}