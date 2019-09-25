package paintexp.tool;

import javafx.event.EventType;
import javafx.geometry.Bounds;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

@SuppressWarnings({ "unused", "static-method" })
public abstract class PaintTool extends Group {
    private Node icon;

    public PaintTool() {
        setId(getClass().getSimpleName());
        icon = createIcon();
        if (icon != null) {
            getChildren().add(icon);
			icon.setScaleX(1 / (icon.getBoundsInLocal().getWidth() / 30));
			icon.setScaleY(1 / (icon.getBoundsInLocal().getHeight() / 30));
        }
    }

    public abstract Node createIcon();

    public Node getIcon() {
        return icon;
    }

    public Cursor getMouseCursor() {
        return Cursor.DEFAULT;
    }

    public void handleEvent(MouseEvent e, PaintModel model) {
        simpleHandleEvent(e, model);
        EventType<? extends MouseEvent> eventType = e.getEventType();
        if (MouseEvent.MOUSE_RELEASED.equals(eventType)) {
            model.createImageVersion();
        }
    }

    public void handleKeyEvent(KeyEvent e, PaintModel paintModel) {
        // DOES NOTHING
    }

    public void onDeselected(PaintModel model) {
        // DOES NOTHING
    }

    public void onSelected(PaintModel model) {
        // DOES NOTHING
    }

    public void setIcon(Node icon) {
        this.icon = icon;
    }

    protected boolean containsPoint(Node area2, double localX, double localY) {
        Bounds bounds = area2.getBoundsInParent();
        return area2.getLayoutX() < localX && localX < area2.getLayoutX() + bounds.getWidth()
            && area2.getLayoutY() < localY && localY < area2.getLayoutY() + bounds.getHeight();
    }

    protected void onMouseDragged(MouseEvent e, PaintModel model) {
        // DOES NOTHING

    }

    protected void onMousePressed(MouseEvent e, PaintModel model) {
        // DOES NOTHING
    }

    protected void onMouseReleased(PaintModel model) {

        // DOES NOTHING
    }

    protected void simpleHandleEvent(MouseEvent e, PaintModel model) {
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

}