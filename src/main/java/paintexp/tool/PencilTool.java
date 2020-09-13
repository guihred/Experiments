package paintexp.tool;

import static utils.DrawOnPoint.getWithinRange;
import static utils.DrawOnPoint.withinImage;

import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

public class PencilTool extends PaintTool {

    private int y;

    private int x;
    private boolean clicked;

    @Override
    public Node createIcon() {
        return PaintTool.getIconByURL("Pencil.png");
    }

    @Override
    public Cursor getMouseCursor() {
        return Cursor.HAND;
    }

    @Override
    public void onMouseDragged(final MouseEvent e, final PaintModel model) {
        WritableImage image = model.getImage();
        int x2 = (int) getWithinRange(e.getX(), 0, image.getWidth());
        int y2 = (int) getWithinRange(e.getY(), 0, image.getHeight());
        if (withinImage(x2, y2, image)) {
            Color color = e.getButton() == MouseButton.PRIMARY ? model.getFrontColor() : model.getBackColor();
            image.getPixelWriter().setColor(x2, y2, color);
            RectBuilder.build().startX(x).startY(y).endX(x2).endY(y2).drawLine(image, color);
            x = (int) getWithinRange(e.getX(), 0, image.getWidth());
            y = (int) getWithinRange(e.getY(), 0, image.getHeight());
        }
    }

    @Override
    public void onMousePressed(final MouseEvent e, final PaintModel model) {
        WritableImage image = model.getImage();
        x = (int) getWithinRange(e.getX(), 0, image.getWidth());
        y = (int) getWithinRange(e.getY(), 0, image.getHeight());
        if (withinImage(x, y, image)) {
            Color color = e.getButton() == MouseButton.PRIMARY ? model.getFrontColor() : model.getBackColor();
            image.getPixelWriter().setColor(x, y, color);
        }
        clicked = true;
    }

    @Override
    public void onMouseReleased(PaintModel model) {
        super.onMouseReleased(model);
        clicked = false;
    }

    @Override
    public void simpleHandleEvent(MouseEvent e, PaintModel model) {
        super.simpleHandleEvent(e, model);
        if (MouseEvent.MOUSE_EXITED.equals(e.getEventType()) && clicked) {
            onMouseDragged(e, model);
        }
        if (MouseEvent.MOUSE_ENTERED.equals(e.getEventType()) && clicked) {
            onMousePressed(e, model);
        }
    }

}