package paintexp.tool;

import javafx.event.EventType;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import paintexp.PaintModel;
import utils.ResourceFXUtils;

public class EyedropTool extends PaintTool {

    private ImageView icon;

    private Rectangle area;

    public Rectangle getArea() {
        if (area == null) {
            area = new Rectangle(50, 50, Color.TRANSPARENT);
            area.setStroke(Color.grayRgb(128));
            area.setManaged(false);
        }
        return area;
    }

    @Override
    public Node getIcon() {
        if (icon == null) {
            icon = new ImageView(ResourceFXUtils.toExternalForm("eyedrop.png"));
            icon.setPreserveRatio(true);
            icon.setFitWidth(10);
            icon.maxWidth(10);
            icon.maxHeight(10);
        }
        return icon;
    }

    @Override
    public Cursor getMouseCursor() {
        return Cursor.cursor(ResourceFXUtils.toExternalForm("eyedrop.png"));
    }

    @Override
    public synchronized void handleEvent(final MouseEvent e, final PaintModel model) {
        EventType<? extends MouseEvent> eventType = e.getEventType();
        if (MouseEvent.MOUSE_MOVED.equals(eventType)) {

            int y = (int) e.getY();
            int x = (int) e.getX();
            if (withinRange(x, y, model)) {
                WritableImage image = model.getImage();
                Color color = image.getPixelReader().getColor(x, y);
                getArea().setFill(color);
            }
        }
        if (MouseEvent.MOUSE_PRESSED.equals(eventType)) {

            int y = (int) e.getY();
            int x = (int) e.getX();
            if (withinRange(x, y, model)) {
                WritableImage image = model.getImage();
                Color color = image.getPixelReader().getColor(x, y);
                if (e.getButton() == MouseButton.SECONDARY) {
                    model.setBackColor(color);
                } else {
                    model.setFrontColor(color);
                }
            }
        }
    }

    @Override
    public void onSelected(PaintModel model) {
        model.getToolOptions().getChildren().clear();
        model.getToolOptions().getChildren().add(getArea());
    }


}