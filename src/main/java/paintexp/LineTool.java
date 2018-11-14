package paintexp;

import javafx.collections.ObservableList;
import javafx.event.EventType;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import simplebuilder.SimpleLineBuilder;

public class LineTool extends PaintTool {

    private Node icon;
    private Line line;

    @Override
	public Node getIcon() {
		if (icon == null) {
            icon = new SimpleLineBuilder().startX(0).startY(0).endX(10).endY(6).stroke(Color.BLACK).build();
		}
		return icon;
	}

	public Line getLine() {
		if (line == null) {
            line = new SimpleLineBuilder().layoutX(0).layoutY(0).managed(false).build();
		}
		return line;
	}

	@Override
	public Cursor getMouseCursor() {
		return Cursor.CROSSHAIR;
	}

	@Override
    public void handleEvent(MouseEvent e, PaintModel model) {
		EventType<? extends MouseEvent> eventType = e.getEventType();
		if (MouseEvent.MOUSE_RELEASED.equals(eventType)) {
            ObservableList<Node> children = model.getImageStack().getChildren();
            if (size() < 2 && children.contains(getLine())) {
                children.remove(getLine());
            } else {
                drawLine(model, line.getStartX(), line.getStartY(), line.getEndX(), line.getEndY());
			}
		}
		if (MouseEvent.MOUSE_PRESSED.equals(eventType)) {
            ObservableList<Node> children = model.getImageStack().getChildren();
            if (!children.contains(getLine())) {
                children.add(getLine());
            }
            getLine().setManaged(false);
            getLine().setStartX(e.getX());
            getLine().setStartY(e.getY());
            getLine().setEndX(e.getX());
            getLine().setEndY(e.getY());
		}
		if (MouseEvent.MOUSE_DRAGGED.equals(eventType)) {
            getLine().setEndX(e.getX());
            getLine().setEndY(e.getY());
		}

	}


    private double size() {
        double w = getLine().getLayoutBounds().getWidth();
        double h = getLine().getLayoutBounds().getHeight();
        return Math.sqrt(w * w + h * h);
    }
}