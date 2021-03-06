package paintexp.tool;
import static utils.DrawOnPoint.withinImage;

import javafx.event.EventType;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import utils.ResourceFXUtils;

public class EyedropTool extends PaintTool {


    private Rectangle area;
    private PaintTool old;

    @Override
    public Node createIcon() {
        return PaintTool.getIconByURL("eyedrop.png");
    }

    public Rectangle getArea() {
        if (area == null) {
            area = new Rectangle(50, 50, Color.TRANSPARENT);
            area.setStroke(Color.GRAY);
        }
        return area;
    }

    @Override
    public Cursor getMouseCursor() {
        return Cursor.cursor(ResourceFXUtils.toExternalForm("paint/eyedrop.png"));
    }

    @Override
    public synchronized void handleEvent(final MouseEvent e, final PaintModel model) {
        EventType<? extends MouseEvent> eventType = e.getEventType();
        if (MouseEvent.MOUSE_MOVED.equals(eventType) || MouseEvent.MOUSE_DRAGGED.equals(eventType)) {
            onMouseMoved(e, model);
        }
        if (MouseEvent.MOUSE_PRESSED.equals(eventType) || MouseEvent.MOUSE_RELEASED.equals(eventType)) {
            onMousePressed(e, model);
        }
    }

    @Override
    public void onMousePressed(final MouseEvent e, final PaintModel model) {
        int y = (int) e.getY();
        int x = (int) e.getX();
        if (withinImage(x, y, model.getImage())) {
            WritableImage image = model.getImage();
            Color color = image.getPixelReader().getColor(x, y);
            if (e.getButton() == MouseButton.SECONDARY) {
                model.setBackColor(color);
            } else {
                model.setFrontColor(color);
            }
            model.toolProperty().set(old);
        }
    }

    @Override
    public void onSelected(CommonTool oldTool, PaintModel model) {
        old = (PaintTool) oldTool;
        model.getToolOptions().getChildren().add(getArea());
    }

    private void onMouseMoved(final MouseEvent e, final PaintModel model) {
        int y = (int) e.getY();
        int x = (int) e.getX();
        if (withinImage(x, y, model.getImage())) {
            WritableImage image = model.getImage();
            Color color = image.getPixelReader().getColor(x, y);
            getArea().setFill(color);
        }
    }


}