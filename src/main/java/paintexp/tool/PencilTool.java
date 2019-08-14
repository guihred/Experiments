package paintexp.tool;
import static utils.DrawOnPoint.withinRange;

import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import paintexp.PaintModel;

public class PencilTool extends PaintTool {

    private boolean pressed;

    private int y;

    private int x;


	@Override
	public Node createIcon() {
	    return getIconByURL("Pencil.png");
	}

    @Override
	public Cursor getMouseCursor() {
        return Cursor.HAND;
	}


    @Override
    protected void onMouseDragged(final MouseEvent e, final PaintModel model) {
        int y2 = (int) e.getY();
        int x2 = (int) e.getX();
        if (pressed && withinRange(x2, y2, model)) {
			Color color = e.getButton() == MouseButton.PRIMARY ? model.getFrontColor() : model.getBackColor();
			model.getImage().getPixelWriter().setColor(x2, y2, color);
			drawLine(model, x, y, x2, y2, color);
            y = (int) e.getY();
            x = (int) e.getX();
        }
    }

    @Override
    protected  void onMousePressed(final MouseEvent e, final PaintModel model) {
		y = (int) e.getY();
		x = (int) e.getX();
		if (withinRange(x, y, model)) {
			Color color = e.getButton() == MouseButton.PRIMARY ? model.getFrontColor() : model.getBackColor();
			model.getImage().getPixelWriter().setColor(x, y, color);
		}
		pressed = true;
	}

    @Override
    protected void onMouseReleased(final PaintModel model) {
        pressed = false;
    }


}