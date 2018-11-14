package paintexp;

import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;

public abstract class PaintTool extends Group {
    public PaintTool() {
        getChildren().add(getIcon());
    }

    public abstract Node getIcon();

    public abstract Cursor getMouseCursor();

	@SuppressWarnings("unused")
    public void handleEvent(MouseEvent e, PaintModel model) {
		// DOES NOTHING
	}

    protected void drawLine(PaintModel model, double startX, double startY, double endX, double endY) {
        double d = endX - startX;
        double a = d == 0 ? Double.NaN : (endY - startY) / d;
        double b = Double.isNaN(a) ? Double.NaN : endY - a * endX;
        double minX = Double.min(startX, endX);
        double maxX = Double.max(startX, endX);
        double minY = Double.min(startY, endY);
        double maxY = Double.max(startY, endY);

        for (int x = (int) minX; x < maxX; x++) {
            int y = (int) (!Double.isNaN(a) ? Math.round(a * x + b) : endY);
            if (withinRange(x, y, model)) {
                model.getImage().getPixelWriter().setColor(x, y, model.getFrontColor());
            }
        }

        for (int y = (int) minY; y < maxY; y++) {
            if (a != 0) {
                int x = (int) (Double.isNaN(a) ? startX : Math.round((y - b) / a));
                if (withinRange(x, y, model)) {
                    model.getImage().getPixelWriter().setColor(x, y, model.getFrontColor());
                }
            }
        }
    }

    protected boolean within(int y, double height) {
        return 0 < y && y < height;
    }

    protected boolean withinRange(int x, int y, PaintModel model) {
        return within(y, model.getImage().getHeight()) && within(x, model.getImage().getWidth());
    }
}