package paintexp;

import javafx.event.EventType;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import utils.ResourceFXUtils;

public class BucketTool extends PaintTool {

    private ImageView icon;
    boolean pressed;

    private Rectangle area;

    public Rectangle getArea() {
        if (area == null) {
            area = new Rectangle(10, 10, Color.WHITE);
        }
        return area;
    }

    @Override
    public Node getIcon() {
        if (icon == null) {
            icon = new ImageView(ResourceFXUtils.toExternalForm("Bucket.png"));
            icon.setPreserveRatio(true);
            icon.setFitWidth(10);
            icon.maxWidth(10);
            icon.maxHeight(10);
        }
        return icon;
    }

    @Override
    public Cursor getMouseCursor() {
        return Cursor.DISAPPEAR;
    }

    @Override
    public synchronized void handleEvent(MouseEvent e, PaintModel model) {
		EventType<? extends MouseEvent> eventType = e.getEventType();
        if (MouseEvent.MOUSE_CLICKED.equals(eventType)) {
            int x = (int) e.getX();
            int y = (int) e.getY();
            int w = (int) getArea().getWidth();
            int argb = model.getImage().getPixelReader().getArgb(x, y);

        }
	}


}