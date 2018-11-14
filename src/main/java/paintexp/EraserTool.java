package paintexp;

import javafx.collections.ObservableList;
import javafx.event.EventType;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import utils.ResourceFXUtils;

public class EraserTool extends PaintTool {

    private ImageView icon;

    private Rectangle area;

    public Rectangle getArea() {
        if (area == null) {
            area = new Rectangle(10, 10, Color.WHITE);
            area.setManaged(false);
            area.setStroke(Color.BLACK);
        }
        return area;
    }

    @Override
    public Node getIcon() {
        if (icon == null) {
            icon = new ImageView(ResourceFXUtils.toExternalForm("eraser.png"));
            icon.setPreserveRatio(true);
            icon.setFitWidth(10);
            icon.maxWidth(10);
            icon.maxHeight(10);
        }
        return icon;
    }

    @Override
    public Cursor getMouseCursor() {
        return Cursor.NONE;
    }

    @Override
    public synchronized void handleEvent(MouseEvent e, PaintModel model) {
		EventType<? extends MouseEvent> eventType = e.getEventType();
        if (MouseEvent.MOUSE_MOVED.equals(eventType)) {
            ObservableList<Node> children = model.getImageStack().getChildren();
            if (!children.contains(getArea())) {
                children.add(getArea());
            }
            getArea().setFill(model.getBackColor());
            getArea().setLayoutX(e.getX());
            getArea().setLayoutY(e.getY());
        }
        if (MouseEvent.MOUSE_PRESSED.equals(eventType)) {
            int y = (int) e.getY();
            int x = (int) e.getX();
            int w = (int) getArea().getWidth();
            drawSquare(model, x, y, w);
            getArea().setLayoutX(e.getX());
            getArea().setLayoutY(e.getY());
        }
		if (MouseEvent.MOUSE_DRAGGED.equals(eventType)) {
            int x = (int) e.getX();
            int y = (int) e.getY();
            int w = (int) getArea().getWidth();
                drawSquare(model, x, y, w);
            getArea().setLayoutX(e.getX());
            getArea().setLayoutY(e.getY());
		}
        if (MouseEvent.MOUSE_EXITED.equals(eventType)) {
            getArea().setVisible(false);
        }
        if (MouseEvent.MOUSE_ENTERED.equals(eventType)) {
            getArea().setVisible(true);
        }
	}



}