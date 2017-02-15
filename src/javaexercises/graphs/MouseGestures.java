package javaexercises.graphs;

import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;

public class MouseGestures {

	private static class DragContext {
		private boolean dragged;
		private double x;
		private double y;
	}

	private final DragContext dragContext = new DragContext();

	private Graph graph;

	private EventHandler<MouseEvent> onMouseDraggedEventHandler = event -> {

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

	private EventHandler<MouseEvent> onMousePressedEventHandler = event -> {

		Node node = (Node) event.getSource();

		double scale = graph.getScale();
		if (node instanceof Cell) {
			dragContext.dragged = false;
		}
		dragContext.x = node.getBoundsInParent().getMinX() * scale - event.getScreenX();
		dragContext.y = node.getBoundsInParent().getMinY() * scale - event.getScreenY();

	};

	private EventHandler<MouseEvent> onMouseReleasedEventHandler = event -> {
		Object source = event.getSource();
		if (source instanceof Cell && !dragContext.dragged) {
			Cell cell = (Cell) source;
			cell.setSelected(!cell.isSelected());
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