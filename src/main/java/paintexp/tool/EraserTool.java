package paintexp.tool;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.ObservableList;
import javafx.event.EventType;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Slider;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import simplebuilder.SimpleSliderBuilder;
import utils.PixelHelper;

public class EraserTool extends PaintTool {

    private Rectangle area;
    private IntegerProperty length = new SimpleIntegerProperty(10);
    private int lastX;
    private int lastY;
    private Slider lengthSlider;

    @Override
    public Node createIcon() {
        return PaintToolHelper.getIconByURL("eraser.png");
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
            getArea().setVisible(false);
        }
        if (MouseEvent.MOUSE_ENTERED.equals(eventType)) {
            getArea().setVisible(true);
        }
    }

    @Override
    public void handleKeyEvent(final KeyEvent e, final PaintModel paintModel) {
        PaintToolHelper.handleSlider(e, length, lengthSlider);
    }

    @Override
    public void onSelected(final PaintModel model) {
        model.getToolOptions().getChildren().clear();
        model.getToolOptions().setSpacing(5);

        model.getToolOptions().getChildren().add(getLengthSlider(model));
    }

    @Override
    protected void onMouseDragged(final MouseEvent e, final PaintModel model) {
        int w = (int) getArea().getWidth();
        RectBuilder.build().startX(lastX).startY(lastY).endX(e.getX() - w).endY(e.getY() - w).drawLine(model.getImage(),
            (x, y) -> {
                if (e.getButton() == MouseButton.PRIMARY) {
                    PaintToolHelper.drawSquareLine(model.getImage(), x, y, w, model.getBackColor());
                } else {
                    PaintToolHelper.drawSquareLine(model.getImage(), model.getBackColor(), x, y, w,
                        PixelHelper.toArgb(model.getFrontColor()));
                }
            });

        getArea().setLayoutX(e.getX() - w);
        getArea().setLayoutY(e.getY() - w);
        lastX = (int) e.getX() - w;
        lastY = (int) e.getY() - w;
    }

    @Override
    protected void onMousePressed(final MouseEvent e, final PaintModel model) {
        int y = (int) e.getY();
        int x = (int) e.getX();
        int w = (int) getArea().getWidth();
        RectBuilder builder = RectBuilder.build().startX(x - w).startY(y - w).width(w).height(w);
        if (e.getButton() == MouseButton.PRIMARY) {
            builder.drawRect(model.getImage(), model.getBackColor());
        } else {
            builder.drawRect(PixelHelper.toArgb(model.getFrontColor()), model.getImage(), model.getBackColor());
        }
        getArea().setLayoutX(e.getX() - w);
        getArea().setLayoutY(e.getY() - w);
        lastX = x - w;
        lastY = y - w;
    }

    private Slider getLengthSlider(final PaintModel model) {
        if (lengthSlider == null) {
            lengthSlider = new SimpleSliderBuilder(1, model.getImage().getHeight() / 2, 10).bindBidirectional(length)
                .prefWidth(50).build();
        }
        return lengthSlider;
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
        getArea().setLayoutX(e.getX() - w);
        getArea().setLayoutY(e.getY() - w);
    }

}