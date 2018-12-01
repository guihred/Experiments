package paintexp.tool;
import static paintexp.tool.DrawOnPoint.withinRange;

import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import paintexp.PaintModel;

public class PencilTool extends PaintTool {

    private ImageView icon;

    private boolean pressed;

    private int y;

    private int x;


	@Override
	public Node getIcon() {
		if (icon == null) {
            icon = getIconByURL("Pencil.png");
		}
		return icon;
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
		    model.getImage().getPixelWriter().setColor(x, y, model.getFrontColor());
		}
		pressed = true;
	}

    @Override
    protected void onMouseReleased(final PaintModel model) {
        pressed = false;
    }


}