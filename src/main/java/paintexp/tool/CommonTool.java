package paintexp.tool;

import javafx.event.EventType;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

@SuppressWarnings("unused")
public interface CommonTool {
    Node createIcon();

    default Cursor getMouseCursor() {
        return Cursor.DEFAULT;
    }

    default void handleEvent(MouseEvent e, PaintModel model) {
        simpleHandleEvent(e, model);
        EventType<? extends MouseEvent> eventType = e.getEventType();
        if (MouseEvent.MOUSE_RELEASED.equals(eventType)) {
            model.createImageVersion();
        }
    }

    default void handleKeyEvent(KeyEvent e, PaintModel paintModel) {
        // DOES NOTHING
    }

    default void onDeselected(PaintModel model) {
        // DOES NOTHING
    }

    default void onMouseDragged(MouseEvent e, PaintModel model) {
        // DOES NOTHING

    }

    default void onMousePressed(MouseEvent e, PaintModel model) {
        // DOES NOTHING
    }

    default void onMouseReleased(PaintModel model) {
        model.createImageVersion();
        // DOES NOTHING
    }

    default void onSelected(PaintModel model) {
        // DOES NOTHING
    }

    default <T extends CommonTool> void onSelected(T old, PaintModel model) {
        onSelected(model);
    }

    default void simpleHandleEvent(MouseEvent e, PaintModel model) {
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