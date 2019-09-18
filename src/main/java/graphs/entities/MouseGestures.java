package graphs.entities;

import javafx.beans.property.DoubleProperty;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;

public class MouseGestures {

    private final DragContext dragContext = new DragContext();

    private DoubleProperty scale;

    private EventHandler<MouseEvent> onMouseDraggedEventHandler = event -> {

        Node node = (Node) event.getSource();

        double offsetX = event.getScreenX() + dragContext.x;
        double offsetY = event.getScreenY() + dragContext.y;
        if (node instanceof Cell) {
            dragContext.dragged = true;
        }
        // adjust the offset in case we are zoomed
        double scale1 = scale.get();

        offsetX /= scale1;
        offsetY /= scale1;

        node.relocate(offsetX, offsetY);

    };

    private EventHandler<MouseEvent> onMousePressedEventHandler = event -> {

        Node node = (Node) event.getSource();

        double scale1 = scale.get();
        if (node instanceof Cell) {
            dragContext.dragged = false;
        }
        dragContext.x = node.getBoundsInParent().getMinX() * scale1 - event.getScreenX();
        dragContext.y = node.getBoundsInParent().getMinY() * scale1 - event.getScreenY();

    };

    private EventHandler<MouseEvent> onMouseReleasedEventHandler = event -> {
        Object source = event.getSource();
        if (source instanceof Cell && !dragContext.dragged) {
            Cell cell = (Cell) source;
            cell.setSelected(!cell.isSelected());
        }
    };

    public MouseGestures(DoubleProperty graph) {
        scale = graph;
    }

    public void makeDraggable(final Node node) {

        node.setOnMousePressed(onMousePressedEventHandler);
        node.setOnMouseDragged(onMouseDraggedEventHandler);
        node.setOnMouseReleased(onMouseReleasedEventHandler);

    }


    private static class DragContext {
        protected boolean dragged;
        protected double x;
        protected double y;
    }
}