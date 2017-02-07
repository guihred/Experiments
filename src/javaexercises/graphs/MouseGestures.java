package javaexercises.graphs;

import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;

public class MouseGestures {

	class DragContext {
		boolean dragged;
		double x;
		double y;
	}

	final DragContext dragContext = new DragContext();

	Graph graph;

	EventHandler<MouseEvent> onMouseDraggedEventHandler = event -> {

		Node node = (Node) event.getSource();

		double offsetX = event.getScreenX() + dragContext.x;
		double offsetY = event.getScreenY() + dragContext.y;
		if (node instanceof Cell) {
			dragContext.dragged = true;
		}
		// adjust the offset in case we are zoomed
		double scale = graph.getScale();

		offsetX /= scale;
		offsetY /= scale;

		node.relocate(offsetX, offsetY);

	};

	EventHandler<MouseEvent> onMousePressedEventHandler = event -> {

		Node node = (Node) event.getSource();

		double scale = graph.getScale();
		if (node instanceof Cell) {
			dragContext.dragged = false;
		}
		dragContext.x = node.getBoundsInParent().getMinX() * scale - event.getScreenX();
		dragContext.y = node.getBoundsInParent().getMinY() * scale - event.getScreenY();

	};

	EventHandler<MouseEvent> onMouseReleasedEventHandler = event -> {
		Object source = event.getSource();
		if (source instanceof Cell) {
			if (!dragContext.dragged) {
				Cell cell = (Cell) source;
				cell.setSelected(!cell.isSelected());
			}
		}


	};

	public MouseGestures(Graph graph) {
		this.graph = graph;
	}

	public void makeDraggable(final Node node) {

		node.setOnMousePressed(onMousePressedEventHandler);
		node.setOnMouseDragged(onMouseDraggedEventHandler);
		node.setOnMouseReleased(onMouseReleasedEventHandler);

	}
}