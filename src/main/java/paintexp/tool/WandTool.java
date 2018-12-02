package paintexp.tool;
import static paintexp.tool.DrawOnPoint.withinRange;

import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.ObservableList;
import javafx.event.EventType;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import paintexp.PaintModel;
import paintexp.PaintTools;
import simplebuilder.SimpleSliderBuilder;

public class WandTool extends PaintTool {

	private ImageView icon;
    private Rectangle area;
	private int width;
	private int height;
    private IntegerProperty threshold = new SimpleIntegerProperty(20);

	public Rectangle getArea() {
		if (area == null) {
            area = new Rectangle(10, 10, Color.TRANSPARENT);
            area.setStroke(Color.BLUE);
		}
		return area;
	}

	@Override
	public Node getIcon() {
		if (icon == null) {
            icon = getIconByURL("wand.png");
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

	@Override
	public void onSelected(PaintModel model) {
	    model.getToolOptions().getChildren().clear();
        model.getToolOptions().getChildren()
                .add(new SimpleSliderBuilder(0, 255*3, 0).bindBidirectional(threshold).maxWidth(60)
                        .build());
	
	}

    public void setColor(final int initX, final int initY, final int originalColor, final PixelReader pixelReader,
            final PaintModel model) {
        WritableImage selectedImage = new WritableImage(width, height);
        int backColor = PixelHelper.toArgb(model.getBackColor());
		List<Integer> toGo = new ArrayList<>();
		toGo.add(index(initX, initY));
        PixelHelper pixel = new PixelHelper();
        pixel.reset(originalColor);
        while (!toGo.isEmpty()) {
			Integer next = toGo.remove(0);
			int x = x(next);
			int y = y(next);

			if (withinRange(x, y, model)) {
				int color = pixelReader.getArgb(x, y);
                pixel.reset(originalColor);
                if (closeColor(pixel, color) && selectedImage.getPixelReader().getArgb(x, y) == 0) {
					if (y != 0 && y != height - 1) {
                        addIfNotIn(toGo, next + 1);
                        addIfNotIn(toGo, next - 1);
                        addIfNotIn(toGo, next + width);
                        addIfNotIn(toGo, next - width);
					}
                    selectedImage.getPixelWriter().setArgb(x, y, color);
                    model.getImage().getPixelWriter().setArgb(x, y, backColor);
                    double x2 = area.getX();
                    double y2 = area.getY();
                    area.setX(Math.min(x, x2));
                    area.setY(Math.min(y, y2));
                    area.setWidth(Math.max(x, x2 + area.getWidth()) - area.getX());
                    area.setHeight(Math.max(y, y2 + area.getHeight()) - area.getY());
				}
			}
		}
        int width2 = (int) area.getWidth();
        int height2 = (int) area.getHeight();
        WritableImage writableImage = new WritableImage(width2, height2);
        int x = (int) area.getX();
        int y = (int) area.getY();
        copyImagePart(selectedImage, writableImage, x, y, width2, height2, 0, 0,
                Color.TRANSPARENT);
            SelectRectTool tool = (SelectRectTool) PaintTools.SELECT_RECT.getTool();
            model.changeTool(tool);
            tool.setImageSelected(writableImage);
            tool.selectArea(x, y, width2 + x, height2 + y, model);
            tool.getArea().setFill(new ImagePattern(writableImage));
	}

    private void addIfNotIn(final List<Integer> toGo, final Integer e) {
        if (!toGo.contains(e) && e < width * height && e >= 0) {
			toGo.add(e);
		}
	}

	private boolean closeColor(PixelHelper pixel, int color) {
        pixel.add(color, -1);
        return pixel.modulus() < threshold.get();
    }

	private Integer index(final int initialX2, final int initialY2) {
        return initialX2 * height + initialY2;
	}

    private void onMouseClicked(final MouseEvent e, final PaintModel model) {
		int initialX = (int) e.getX();
		int initialY = (int) e.getY();
		width = (int) model.getImage().getWidth();
		height = (int) model.getImage().getHeight();
        getArea().setX(initialX);
        getArea().setY(initialY);
        getArea().setWidth(0);
        getArea().setHeight(0);
        ObservableList<Node> children = model.getImageStack().getChildren();
        if (!children.contains(getArea())) {
            children.add(getArea());
        }
		PixelReader pixelReader = model.getImage().getPixelReader();
		int originalColor = pixelReader.getArgb(initialX, initialY);
        Platform.runLater(() -> setColor(initialX, initialY, originalColor, pixelReader, model));
	}

	private int x(final int m) {
        return m / height;
	}

	private int y(final int m) {

        return m % height;
	}


}