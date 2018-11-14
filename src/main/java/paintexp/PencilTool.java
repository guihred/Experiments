package paintexp;

import javafx.event.EventType;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import utils.ResourceFXUtils;

public class PencilTool extends PaintTool {

    private ImageView icon;

    boolean pressed;

    private int y;

    private int x;


	@Override
	public Node getIcon() {
		if (icon == null) {
            icon = new ImageView(ResourceFXUtils.toExternalForm("Pencil.png"));
            icon.setPreserveRatio(true);
            icon.setFitWidth(10);
            icon.maxWidth(10);
            icon.maxHeight(10);
		}
		return icon;
	}

    @Override
	public Cursor getMouseCursor() {
        return Cursor.HAND;
	}
	@Override
    public void handleEvent(MouseEvent e, PaintModel model) {
		EventType<? extends MouseEvent> eventType = e.getEventType();
		if (MouseEvent.MOUSE_PRESSED.equals(eventType)) {
            y = (int) e.getY();
            x = (int) e.getX();
            if (withinRange(x, y, model)) {
                model.getImage().getPixelWriter().setColor(x, y, model.getFrontColor());
            }
            pressed = true;
		}
		if (MouseEvent.MOUSE_DRAGGED.equals(eventType)) {
            int y2 = (int) e.getY();
            int x2 = (int) e.getX();
            if (pressed && withinRange(x2, y2, model)) {
                model.getImage().getPixelWriter().setColor(x2, y2, model.getFrontColor());
                drawLine(model, x, y, x2, y2);
                y = (int) e.getY();
                x = (int) e.getX();
            }
		}
        if (MouseEvent.MOUSE_RELEASED.equals(eventType)) {
            pressed = false;
        }

	}


}