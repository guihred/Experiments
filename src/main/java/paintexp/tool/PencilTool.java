package paintexp.tool;
import static utils.DrawOnPoint.withinImage;

import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

public class PencilTool extends PaintTool {

    private boolean pressed;

    private int y;

    private int x;


	@Override
	public Node createIcon() {
	    return PaintTool.getIconByURL("Pencil.png");
	}

    @Override
	public Cursor getMouseCursor() {
        return Cursor.HAND;
	}


    @Override
    protected void onMouseDragged(final MouseEvent e, final PaintModel model) {
        int y2 = (int) e.getY();
        int x2 = (int) e.getX();
        if (pressed && withinImage(x2, y2, model.getImage())) {
			Color color = e.getButton() == MouseButton.PRIMARY ? model.getFrontColor() : model.getBackColor();
			model.getImage().getPixelWriter().setColor(x2, y2, color);
			RectBuilder.build().startX(x).startY(y).endX(x2).endY(y2).drawLine(model.getImage(), color);
            y = (int) e.getY();
            x = (int) e.getX();
        }
    }

    @Override
    protected  void onMousePressed(final MouseEvent e, final PaintModel model) {
		y = (int) e.getY();
		x = (int) e.getX();
        if (withinImage(x, y, model.getImage())) {
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