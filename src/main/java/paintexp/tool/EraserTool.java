package paintexp.tool;

import static utils.DrawOnPoint.getWithinRange;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.ObservableList;
import javafx.event.EventType;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Slider;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import simplebuilder.SimpleSliderBuilder;
import utils.DrawOnPoint;
import utils.PixelHelper;

public class EraserTool extends PaintTool {

    private Rectangle area;
    private IntegerProperty length = new SimpleIntegerProperty(10);
    private int lastX;
    private int lastY;
    private Slider lengthSlider;

    @Override
    public Node createIcon() {
        return PaintTool.getIconByURL("eraser.png");
    }

    public Rectangle getArea() {
        if (area == null) {
            area = new Rectangle(10, 10, Color.WHITE);
            area.setManaged(false);
            area.widthProperty().bind(length);
            area.heightProperty().bind(length);
            area.setStroke(Color.BLACK);
        }
        return area;
    }

    @Override
    public Cursor getMouseCursor() {
        return Cursor.NONE;
    }

    @Override
    public synchronized void handleEvent(final MouseEvent e, final PaintModel model) {
        EventType<? extends MouseEvent> eventType = e.getEventType();
        if (MouseEvent.MOUSE_MOVED.equals(eventType)) {
            onMouseMoved(e, model);
        }
        if (MouseEvent.MOUSE_PRESSED.equals(eventType)) {
            onMousePressed(e, model);
        }
        if (MouseEvent.MOUSE_DRAGGED.equals(eventType)) {
            onMouseDragged(e, model);
        }
        if (MouseEvent.MOUSE_RELEASED.equals(eventType)) {
            model.createImageVersion();
        }
        if (MouseEvent.MOUSE_EXITED.equals(eventType)) {
            onMouseExited(e, model);
        }
        if (MouseEvent.MOUSE_ENTERED.equals(eventType)) {
            getArea().setVisible(true);
        }
    }

    @Override
    public void handleKeyEvent(final KeyEvent e, final PaintModel paintModel) {
        PaintTool.handleSlider(e, length, lengthSlider);
    }

    @Override
    public void onSelected(final PaintModel model) {
        addSlider(model, "Length", getLengthSlider(model), length);
    }

    @Override
    protected void onMouseDragged(final MouseEvent e, final PaintModel model) {
        int w = (int) getArea().getWidth();
        WritableImage image = model.getImage();
        double x = getWithinRange(e.getX(), -w, image.getWidth() + w);
        double y = getWithinRange(e.getY(), -w, image.getHeight() + w);
        RectBuilder.build().startX(lastX).startY(lastY).endX(x - w).endY(y - w).drawLine(model.getImage(), (x0, y0) -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                RectBuilder.drawSquareLine(model.getImage(), x0, y0, w, model.getBackColor());
            } else {
                RectBuilder.drawSquareLine(model.getImage(), model.getBackColor(), x0, y0, w,
                    PixelHelper.toArgb(model.getFrontColor()));
            }
        });

        getArea().setLayoutX(x - w);
        getArea().setLayoutY(y - w);
        lastX = (int) x - w;
        lastY = (int) y - w;
    }

    @Override
    protected void onMousePressed(final MouseEvent e, final PaintModel model) {
        double w = getArea().getWidth();
        WritableImage image = model.getImage();
        int x = (int) getWithinRange(e.getX(), -w, image.getWidth() + w);
        int y = (int) getWithinRange(e.getY(), -w, image.getHeight() + w);
        RectBuilder builder = RectBuilder.build().startX(x - w).startY(y - w).width(w).height(w);
        if (e.getButton() == MouseButton.PRIMARY) {
            builder.drawRect(model.getImage(), model.getBackColor());
        } else {
            builder.drawRect(PixelHelper.toArgb(model.getFrontColor()), model.getImage(), model.getBackColor());
        }
        getArea().setLayoutX(x - w);
        getArea().setLayoutY(y - w);
        lastX = (int) (x - w);
        lastY = (int) (y - w);
    }

    private Slider getLengthSlider(final PaintModel model) {
        if (lengthSlider == null) {
            lengthSlider = new SimpleSliderBuilder(1, model.getImage().getHeight() / 2, 10).bindBidirectional(length)
                    .prefWidth(200).build();
        }
        return lengthSlider;
    }

    private void onMouseExited(final MouseEvent e, final PaintModel model) {
        int w = (int) getArea().getWidth();
        WritableImage image = model.getImage();
        int x = (int) e.getX();
        int y = (int) e.getY();
        if (DrawOnPoint.within(x, -w, image.getWidth() + w) && DrawOnPoint.within(y, -w, image.getHeight() + w)) {
            onMouseMoved(e, model);
            return;
        }
        getArea().setVisible(false);
    }

    private void onMouseMoved(final MouseEvent e, final PaintModel model) {
        ObservableList<Node> children = model.getImageStack().getChildren();
        if (!children.contains(getArea())) {
            children.add(getArea());
        }
        int w = (int) getArea().getWidth();
        getArea().setFill(model.getBackColor());
        Color invert = model.getBackColor().invert();
        Color color = new Color(invert.getRed(), invert.getGreen(), invert.getBlue(), 1);
        getArea().setStroke(color);
        WritableImage image = model.getImage();
        int x = (int) getWithinRange(e.getX(), -w, image.getWidth() + w);
        int y = (int) getWithinRange(e.getY(), -w, image.getHeight() + w);
        getArea().setLayoutX(x - w);
        getArea().setLayoutY(y - w);
    }

}