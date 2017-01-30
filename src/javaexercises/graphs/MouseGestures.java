package javaexercises.graphs;

import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;

public class MouseGestures {

	final DragContext dragContext = new DragContext();

	Graph graph;

	public MouseGestures(Graph graph) {
		this.graph = graph;
	}

	public void makeDraggable(final Node node) {

		node.setOnMousePressed(onMousePressedEventHandler);
		node.setOnMouseDragged(onMouseDraggedEventHandler);
		node.setOnMouseReleased(onMouseReleasedEventHandler);

	}

	EventHandler<MouseEvent> onMousePressedEventHandler = event -> {

		Node node = (Node) event.getSource();

		double scale = graph.getScale();
		if (node instanceof Cell) {
			dragContext.dragged = false;
		}
		dragContext.x = node.getBoundsInParent().getMinX() * scale - event.getScreenX();
		dragContext.y = node.getBoundsInParent().getMinY() * scale - event.getScreenY();

	};

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

	EventHandler<MouseEvent> onMouseReleasedEventHandler = event -> {
		Object source = event.getSource();
		if (source instanceof Cell) {
			if (!dragContext.dragged) {
				Cell cell = (Cell) source;
				cell.setSelected(!cell.isSelected());
			}
		}


	};

	class DragContext {

		double x;
		double y;
		boolean dragged;

	}
}