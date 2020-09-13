package paintexp.tool;

import javafx.event.EventType;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

@SuppressWarnings("unused")
public interface CommonTool {
    public Node createIcon();

    public default Cursor getMouseCursor() {
        return Cursor.DEFAULT;
    }
    public default void handleEvent(MouseEvent e, PaintModel model) {
        simpleHandleEvent(e, model);
        EventType<? extends MouseEvent> eventType = e.getEventType();
        if (MouseEvent.MOUSE_RELEASED.equals(eventType)) {
            model.createImageVersion();
        }
    }

    public default void handleKeyEvent(KeyEvent e, PaintModel paintModel) {
        // DOES NOTHING
    }

    public default void onDeselected(PaintModel model) {
        // DOES NOTHING
    }

    public default void onMouseDragged(MouseEvent e, PaintModel model) {
        // DOES NOTHING

    }

    public default void onMousePressed(MouseEvent e, PaintModel model) {
        // DOES NOTHING
    }

    public default void onMouseReleased(PaintModel model) {
        model.createImageVersion();
        // DOES NOTHING
    }

    public default void onSelected(PaintModel model) {
        // DOES NOTHING
    }

    public default void onSelected(PaintTool old, PaintModel model) {
        onSelected(model);
    }

    public default void simpleHandleEvent(MouseEvent e, PaintModel model) {
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