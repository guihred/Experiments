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

    protected void drawSquare(PaintModel model, int x, int y, int w) {
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < w; j++) {
                if (withinRange(x + i, y + j, model)) {
                    model.getImage().getPixelWriter().setColor(x + i, y + j, model.getBackColor());
                }
            }
        }
    }

	protected boolean within(int y, double min) {
		return 0 <= y && y < min;
	}

	protected boolean within(int y, double min, double max) {
		return min <= y && y < max;
    }

    protected boolean withinRange(int x, int y, PaintModel model) {
        return within(y, model.getImage().getHeight()) && within(x, model.getImage().getWidth());
    }

	protected boolean withinRange(int x, int y, int initialX, int initialY, double bound, PaintModel model) {
		return within(y, Double.max(initialY - bound, 0), Double.min(initialY + bound, model.getImage().getHeight()))
				&& within(x, Double.max(initialX - bound, 0),
						Double.min(initialX + bound, model.getImage().getWidth()));
	}
}