package paintexp.tool;

import javafx.collections.ObservableList;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import simplebuilder.SimpleLineBuilder;

public class LineTool extends PaintTool {

    private Line line;

    @Override
	public Node createIcon() {
        return new SimpleLineBuilder().startX(0).startY(0).endX(10).endY(6).stroke(Color.BLACK).build();
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
    protected void onMouseDragged(final MouseEvent e, PaintModel model) {
		getLine().setEndX(e.getX());
		getLine().setEndY(e.getY());
	}

    @Override
    protected void onMousePressed(final MouseEvent e, final PaintModel model) {
		getLine().setStroke(model.getFrontColor());
		ObservableList<Node> children = model.getImageStack().getChildren();
		if (!children.contains(getLine())) {
			children.add(getLine());
		}

		getLine().setStartX(e.getX());
		getLine().setStartY(e.getY());
		onMouseDragged(e, model);
	}

    @Override
    protected void onMouseReleased(final PaintModel model) {
		ObservableList<Node> children = model.getImageStack().getChildren();
		if (size() >= 2 || !children.contains(getLine())) {
		    drawLine(model, line.getStartX(), line.getStartY(), line.getEndX(), line.getEndY());
		}
		children.remove(getLine());
	}


    private double size() {
        double w = getLine().getLayoutBounds().getWidth();
        double h = getLine().getLayoutBounds().getHeight();
        return Math.sqrt(w * w + h * h);
    }
}