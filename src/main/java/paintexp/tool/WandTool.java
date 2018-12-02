package paintexp.tool;
import static paintexp.tool.DrawOnPoint.within;
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
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.text.Text;
import paintexp.PaintModel;
import simplebuilder.SimpleSliderBuilder;

public class WandTool extends SelectRectTool {

	private ImageView icon;
	private int width;
	private int height;
    private IntegerProperty threshold = new SimpleIntegerProperty(255 * 3 / 20);


	@Override
    public WritableImage createSelectedImage(final PaintModel model) {
        PixelReader pixelReader = model.getImage().getPixelReader();
        int originalColor = pixelReader.getArgb((int)initialX, (int)initialY);
        WritableImage selectedImage = new WritableImage(width, height);
        int backColor = PixelHelper.toArgb(model.getBackColor());
		List<Integer> toGo = new ArrayList<>();
		toGo.add(index((int)initialX, (int)initialY));
        PixelHelper pixel = new PixelHelper();
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
                        addIfNotIn(toGo, next + height);
                        addIfNotIn(toGo, next - height);
					}
                    selectedImage.getPixelWriter().setArgb(x, y, color);
                    model.getImage().getPixelWriter().setArgb(x, y, backColor);
                    double x2 = getArea().getLayoutX();
                    double y2 = getArea().getLayoutY();
                    getArea().setLayoutX(Math.min(x, x2));
                    getArea().setLayoutY(Math.min(y, y2));
                    getArea().setWidth(Math.abs(Math.max(x, x2 + getArea().getWidth()) - getArea().getLayoutX()));
                    getArea().setHeight(Math.abs(Math.max(y, y2 + getArea().getHeight()) - getArea().getLayoutY()));
				}
			}
		}
        int width2 = Math.max(1, (int) getArea().getWidth());
        int height2 = Math.max(1, (int) getArea().getHeight());
        WritableImage writableImage = new WritableImage(width2, height2);
        int x = (int) getArea().getLayoutX();
        int y = (int) getArea().getLayoutY();
        copyImagePart(selectedImage, writableImage, x, y, width2, height2, 0, 0,
                Color.TRANSPARENT);
        return writableImage;
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
        if (MouseEvent.MOUSE_DRAGGED.equals(eventType)) {
            onMouseDragged(e, model);
        }
        if (MouseEvent.MOUSE_PRESSED.equals(eventType)) {
            if (model.getImageStack().getChildren().contains(getArea()) && imageSelected != null) {
                onMousePressed(e, model);
            } else {
                onMouseClicked(e, model);
            }
        }
        if (MouseEvent.MOUSE_RELEASED.equals(eventType)) {
            if (model.getImageStack().getChildren().contains(getArea()) && imageSelected != null) {
                onMouseReleased(model);
            }
        }

	}

    @Override
    public void onSelected(PaintModel model) {
        model.getToolOptions().getChildren().clear();
        Slider sliders = new SimpleSliderBuilder(0, 255 * 3, 0).bindBidirectional(threshold).maxWidth(60).build();
        model.getToolOptions().getChildren().add(sliders);
        Text text = new Text();
        text.textProperty().bind(threshold.divide(sliders.getMax()).multiply(100).asString("%.0f%%"));
        model.getToolOptions().getChildren().add(text);

    }

    public void setImage(final PaintModel model) {
        WritableImage writableImage = createSelectedImage(model);
        int width3 = Math.max(1, (int) getArea().getWidth());
        int height3 = Math.max(1, (int) getArea().getHeight());
        int x2 = (int) getArea().getLayoutX();
        int y2 = (int) getArea().getLayoutY();
        selectArea(x2, y2, width3 + x2, height3 + y2, model);
        setImageSelected(writableImage);
        getArea().setFill(new ImagePattern(writableImage));
    }

    @Override
    protected void addRect(PaintModel model) {
        getArea().setManaged(false);
    }

    private void addIfNotIn(final List<Integer> toGo, final int e) {
        if (!toGo.contains(e) && within(e, width * height)) {
			toGo.add(e);
		}
	}

	private boolean closeColor(PixelHelper pixel, int color) {
        pixel.add(color, -1);
        return pixel.modulus() < threshold.get();
    }

    private int index(final int initialX2, final int initialY2) {
        return initialX2 * height + initialY2;
	}

    private void onMouseClicked(final MouseEvent e, final PaintModel model) {
        int clickedX = (int) e.getX();
        initialX = clickedX;
        int clickedY = (int) e.getY();
        initialY = clickedY;
		width = (int) model.getImage().getWidth();
		height = (int) model.getImage().getHeight();
        ObservableList<Node> children = model.getImageStack().getChildren();
        if (!children.contains(getArea())) {
            children.add(getArea());
        }

        getArea().setLayoutX(clickedX);
        getArea().setLayoutY(clickedY);
        getArea().setManaged(false);
        getArea().setWidth(1);
        getArea().setHeight(1);

        Platform.runLater(() -> setImage(model));
	}

	private int x(final int m) {
        return m / height;
	}

	private int y(final int m) {

        return m % height;
	}


}