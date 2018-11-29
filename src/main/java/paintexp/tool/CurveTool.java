package paintexp.tool;

import javafx.collections.ObservableList;
import javafx.event.EventType;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.CubicCurve;
import paintexp.PaintModel;

public class CurveTool extends PaintTool {

    private CubicCurve icon;
    private CubicCurve line;
    private int stage;

    @Override
	public Node getIcon() {
		if (icon == null) {
            icon = new CubicCurve();
            icon.setStroke(Color.BLACK);
            icon.setFill(Color.TRANSPARENT);
            icon.setStartX(0);
            icon.setStartY(0);
            icon.setControlX1(0);
            icon.setControlY1(10);
            icon.setControlX2(10);
            icon.setControlY2(0);
            icon.setStartY(0);
            icon.setEndX(10);
            icon.setEndY(10);
		}
		return icon;
	}

    public CubicCurve getLine() {
		if (line == null) {
            line = new CubicCurve();
            line.setStroke(Color.BLACK);
            line.setFill(Color.TRANSPARENT);
		}
		return line;
	}

	@Override
	public Cursor getMouseCursor() {
		return Cursor.CROSSHAIR;
	}

    @Override
    public void handleEvent(final MouseEvent e, final PaintModel model) {
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

    @Override
	public void handleKeyEvent(final KeyEvent e, final PaintModel paintModel) {
		KeyCode code = e.getCode();
		if(code==KeyCode.ESCAPE) {
			takeSnapshotFill(paintModel, getLine());
		}
	}

	@Override
	public void onDeselected(final PaintModel model) {
		takeSnapshotFill(model, getLine());
	}

	@Override
    protected void onMouseDragged(final MouseEvent e, final PaintModel model) {
        if (stage == 0) {
            getLine().setEndX(e.getX());
            getLine().setEndY(e.getY());
        }
        if (stage == 1) {
            getLine().setControlX1(e.getX());
            getLine().setControlY1(e.getY());
        }
        if (stage == 2) {
            getLine().setControlX2(e.getX());
            getLine().setControlY2(e.getY());
        }
	}

    @Override
    protected void onMousePressed(final MouseEvent e, final PaintModel model) {
		getLine().setStroke(model.getFrontColor());
		ObservableList<Node> children = model.getImageStack().getChildren();
		if (!children.contains(getLine())) {
			children.add(getLine());
		}
        if (stage == 0) {
            getLine().setStartX(e.getX());
            getLine().setControlX1(e.getX());
            getLine().setControlX2(e.getX());
            getLine().setStartY(e.getY());
            getLine().setControlY1(e.getY());
            getLine().setControlY2(e.getY());
        }
        onMouseDragged(e, model);

	}

    @Override
    protected void onMouseReleased(final PaintModel model) {
		ObservableList<Node> children = model.getImageStack().getChildren();
        if ((size() >= 2 || !children.contains(getLine())) && stage == 2) {
            takeSnapshotFill(model, line);
            model.createImageVersion();
        }
        stage = ++stage % 3;
	}

	private double size() {
        double w = getLine().getLayoutBounds().getWidth();
        double h = getLine().getLayoutBounds().getHeight();
        return Math.sqrt(w * w + h * h);
    }


}